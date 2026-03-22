package de.caluga.morphium.spring.autoconfigure;

import de.caluga.morphium.AnnotationAndReflectionHelper;
import de.caluga.morphium.Morphium;
import de.caluga.morphium.MorphiumConfig;
import de.caluga.morphium.annotations.Embedded;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.config.CollectionCheckSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(Morphium.class)
@EnableConfigurationProperties(MorphiumProperties.class)
public class MorphiumAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MorphiumAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public Morphium morphium(MorphiumProperties properties) {
        // Pre-register entity type IDs from classpath scan to skip Morphium's internal ClassGraph scan
        preRegisterEntities();

        MorphiumConfig cfg = buildConfig(properties);
        Morphium m = connectWithRetry(cfg, properties.getConnectRetries());

        if (properties.getReplicaSetName() != null && !m.getDriver().isReplicaSet()) {
            log.debug("Forcing replicaSet=true on driver (single-node replica set workaround)");
            m.getDriver().setReplicaSet(true);
        }

        log.info("Morphium connected to '{}' (driver: {}, replicaSet: {})",
                properties.getDatabase(), properties.getDriverName(),
                m.getDriver().isReplicaSet());

        return m;
    }

    /**
     * Scans the classpath for @Entity/@Embedded classes and pre-registers their type IDs.
     * This skips Morphium's internal ClassGraph scan at startup.
     * Best-effort: if scanning fails, Morphium falls back to its own ClassGraph scan.
     */
    private void preRegisterEntities() {
        try {
            io.github.classgraph.ScanResult scanResult = new io.github.classgraph.ClassGraph()
                    .enableAnnotationInfo()
                    .scan();
            Map<String, String> typeIds = new HashMap<>();
            try (scanResult) {
                for (String annotationName : new String[]{Entity.class.getName(), Embedded.class.getName()}) {
                    for (var ci : scanResult.getClassesWithAnnotation(annotationName)) {
                        String cn = ci.getName();
                        typeIds.put(cn, cn);
                        var ai = ci.getAnnotationInfo(annotationName);
                        if (ai != null) {
                            var typeIdParam = ai.getParameterValues().getValue("typeId");
                            if (typeIdParam instanceof String tid && !".".equals(tid)) {
                                typeIds.put(tid, cn);
                            }
                        }
                    }
                }
            }
            if (!typeIds.isEmpty()) {
                AnnotationAndReflectionHelper.registerTypeIds(typeIds);
                log.info("Pre-registered {} entity type IDs, Morphium will skip ClassGraph scan", typeIds.size());
            }
        } catch (Exception e) {
            log.debug("Entity pre-registration skipped, Morphium will use its own ClassGraph scan: {}", e.getMessage());
        }
    }

    private MorphiumConfig buildConfig(MorphiumProperties properties) {
        MorphiumConfig cfg = new MorphiumConfig();

        cfg.connectionSettings().setDatabase(properties.getDatabase());
        cfg.driverSettings().setDriverName(properties.getDriverName());
        cfg.connectionSettings().setMaxConnections(properties.getMaxConnections());
        cfg.driverSettings().setDefaultReadPreferenceType(properties.getReadPreference());

        // Index check mode
        switch (properties.getIndexCheck()) {
            case "CREATE_ON_STARTUP":
                cfg.collectionCheckSettings().setIndexCheck(CollectionCheckSettings.IndexCheck.CREATE_ON_STARTUP);
                break;
            case "WARN_ON_STARTUP":
                cfg.collectionCheckSettings().setIndexCheck(CollectionCheckSettings.IndexCheck.WARN_ON_STARTUP);
                break;
            case "CREATE_ON_WRITE_NEW_COL":
                cfg.setAutoIndexAndCappedCreationOnWrite(true);
                break;
            case "NO_CHECK":
                cfg.collectionCheckSettings().setIndexCheck(CollectionCheckSettings.IndexCheck.NO_CHECK);
                break;
        }

        // Host configuration
        if (properties.getAtlasUrl() != null && !properties.getAtlasUrl().isBlank()) {
            cfg.clusterSettings().setAtlasUrl(properties.getAtlasUrl());
        } else {
            for (String host : properties.getHosts()) {
                String trimmed = host.trim();
                if (!trimmed.isEmpty()) {
                    cfg.clusterSettings().addHostToSeed(trimmed);
                }
            }
        }

        // Replica set name
        if (properties.getReplicaSetName() != null && !properties.getReplicaSetName().isBlank()) {
            cfg.clusterSettings().setRequiredReplicaSetName(properties.getReplicaSetName());
        }

        // Credentials
        if (properties.getUsername() != null && properties.getPassword() != null) {
            cfg.authSettings().setMongoLogin(properties.getUsername());
            cfg.authSettings().setMongoPassword(properties.getPassword());
            cfg.authSettings().setMongoAuthDb(properties.getAuthDatabase());
        }

        // Cache
        cfg.cacheSettings().setGlobalCacheValidTime(properties.getCache().getGlobalValidTime());
        cfg.cacheSettings().setReadCacheEnabled(properties.getCache().isReadCacheEnabled());

        // SSL
        if (properties.getSsl().isEnabled()) {
            cfg.setUseSSL(true);
            String keystorePath = properties.getSsl().getKeystorePath();
            String keystorePassword = properties.getSsl().getKeystorePassword();
            if (keystorePath != null) {
                try {
                    var sslContext = de.caluga.morphium.driver.wire.SslHelper.createSslContext(
                            keystorePath, keystorePassword, null, null);
                    cfg.setSslContext(sslContext);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to build SSLContext: " + e.getMessage(), e);
                }
            }
        }

        return cfg;
    }

    private Morphium connectWithRetry(MorphiumConfig cfg, int maxRetries) {
        int maxAttempts = Math.max(1, maxRetries);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return new Morphium(cfg);
            } catch (Exception e) {
                if (!isTransient(e) || attempt == maxAttempts) {
                    throw e;
                }
                long delayMs = attempt * 2000L;
                log.warn("Morphium connection attempt {}/{} failed: {}. Retrying in {}ms...",
                        attempt, maxAttempts, e.getMessage(), delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying Morphium connection", ie);
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private static boolean isTransient(Throwable t) {
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null && (msg.contains("No primary node found") || msg.contains("not connected yet"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
