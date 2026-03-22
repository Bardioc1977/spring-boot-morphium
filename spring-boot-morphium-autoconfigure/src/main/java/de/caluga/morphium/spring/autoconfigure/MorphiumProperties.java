package de.caluga.morphium.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.morphium")
public class MorphiumProperties {

    private List<String> hosts = List.of("localhost:27017");
    private String database;
    private String username;
    private String password;
    private String authDatabase = "admin";
    private String driverName = "PooledDriver";
    private String readPreference = "primary";
    private int maxConnections = 250;
    private String atlasUrl;
    private String replicaSetName;
    private int connectRetries = 5;
    private String indexCheck = "CREATE_ON_STARTUP";

    private CacheProperties cache = new CacheProperties();
    private SslProperties ssl = new SslProperties();

    public List<String> getHosts() { return hosts; }
    public void setHosts(List<String> hosts) { this.hosts = hosts; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAuthDatabase() { return authDatabase; }
    public void setAuthDatabase(String authDatabase) { this.authDatabase = authDatabase; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getReadPreference() { return readPreference; }
    public void setReadPreference(String readPreference) { this.readPreference = readPreference; }
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    public String getAtlasUrl() { return atlasUrl; }
    public void setAtlasUrl(String atlasUrl) { this.atlasUrl = atlasUrl; }
    public String getReplicaSetName() { return replicaSetName; }
    public void setReplicaSetName(String replicaSetName) { this.replicaSetName = replicaSetName; }
    public int getConnectRetries() { return connectRetries; }
    public void setConnectRetries(int connectRetries) { this.connectRetries = connectRetries; }
    public String getIndexCheck() { return indexCheck; }
    public void setIndexCheck(String indexCheck) { this.indexCheck = indexCheck; }
    public CacheProperties getCache() { return cache; }
    public void setCache(CacheProperties cache) { this.cache = cache; }
    public SslProperties getSsl() { return ssl; }
    public void setSsl(SslProperties ssl) { this.ssl = ssl; }

    public static class CacheProperties {
        private int globalValidTime = 5000;
        private boolean readCacheEnabled = true;

        public int getGlobalValidTime() { return globalValidTime; }
        public void setGlobalValidTime(int globalValidTime) { this.globalValidTime = globalValidTime; }
        public boolean isReadCacheEnabled() { return readCacheEnabled; }
        public void setReadCacheEnabled(boolean readCacheEnabled) { this.readCacheEnabled = readCacheEnabled; }
    }

    public static class SslProperties {
        private boolean enabled = false;
        private String keystorePath;
        private String keystorePassword;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getKeystorePath() { return keystorePath; }
        public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
        public String getKeystorePassword() { return keystorePassword; }
        public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
    }
}
