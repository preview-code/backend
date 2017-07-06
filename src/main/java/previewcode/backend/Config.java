package previewcode.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Config {
    public final Integer port;
    public final String firebaseAuthFile;
    public final String webhookSecretFile;
    public final IntegrationConfig integration;
    public final DatabaseConfig database;

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    @JsonCreator
    public Config(@JsonProperty("port")Integer port,
                  @JsonProperty("webhook-secret-file") String webhookSecretFile,
                  @JsonProperty("firebase-auth-file") String firebaseAuthFile,
                  @JsonProperty("integration") IntegrationConfig integration,
                  @JsonProperty("database") DatabaseConfig database) {
        if (port <= 0)
            throw new IllegalArgumentException("Port must be positive");

        this.port = port;
        this.firebaseAuthFile = firebaseAuthFile;
        this.webhookSecretFile = webhookSecretFile;
        this.integration = integration;
        this.database = database;
    }

    public static Config loadConfiguration(String filePath) throws Exception {
        logger.info("Reading " + filePath);
        return new ObjectMapper(new YAMLFactory())
                .readValue(new File(filePath), Config.class);
    }

    public static class IntegrationConfig {
        public final String keyFile;
        public final String id;

        @JsonCreator
        public IntegrationConfig(@JsonProperty("key-file") String keyFile,
                                 @JsonProperty("id") String id) {
            this.keyFile = keyFile;
            this.id = id;
        }
    }

    public static class DatabaseConfig {
        public final String driverClass;
        public final String jdbcUrl;
        public final String username;
        public final String password;
        public final ConnectionPoolConfig connectionPool;

        @JsonCreator
        public DatabaseConfig(@JsonProperty("driver-class") String driverClass,
                              @JsonProperty("jdbc-url") String jdbcUrl,
                              @JsonProperty("username") String username,
                              @JsonProperty("password") String password,
                              @JsonProperty("connection-pool") ConnectionPoolConfig connectionPool) {
            this.driverClass = driverClass;
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
            this.connectionPool = connectionPool;
        }

        public static class ConnectionPoolConfig {
            public final Integer partitionCount;
            public final Integer minConsPerPartition;
            public final Integer maxConsPerPartition;

            @JsonCreator
            public ConnectionPoolConfig(@JsonProperty("partition-count") Integer partitionCount,
                                        @JsonProperty("min-connections-per-partition") Integer minConsPerPartition,
                                        @JsonProperty("max-connections-per-partition") Integer maxConsPerPartition) {
                if (partitionCount <= 0)
                    throw new IllegalArgumentException("Partition count must be positive");
                if (minConsPerPartition < 0)
                    throw new IllegalArgumentException("min-connections-per-partition may not be negative");
                if (maxConsPerPartition <= 0)
                    throw new IllegalArgumentException("max-connections-per-partition must be positive");
                if (maxConsPerPartition < minConsPerPartition)
                    throw new IllegalArgumentException("max-connections-per-partition may not be smaller than min-connections-per-partition");

                this.partitionCount = partitionCount;
                this.minConsPerPartition = minConsPerPartition;
                this.maxConsPerPartition = maxConsPerPartition;
            }
        }
    }
}
