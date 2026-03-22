package de.caluga.morphium.spring.autoconfigure;

import de.caluga.morphium.Morphium;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = MorphiumAutoConfiguration.class)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnBean(Morphium.class)
public class MorphiumHealthAutoConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("morphium")
    public HealthIndicator morphiumHealthIndicator(Morphium morphium) {
        return () -> {
            try {
                var driver = morphium.getDriver();
                boolean connected = driver.isConnected();
                var builder = connected ? Health.up() : Health.down();
                builder.withDetail("database", morphium.getConfig().connectionSettings().getDatabase());
                builder.withDetail("driver", morphium.getConfig().driverSettings().getDriverName());
                builder.withDetail("replicaSet", driver.isReplicaSet());
                if (driver.getReplicaSetName() != null) {
                    builder.withDetail("replicaSetName", driver.getReplicaSetName());
                }
                return builder.build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}
