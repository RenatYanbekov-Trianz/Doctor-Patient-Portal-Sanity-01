package com.hms.db;

import com.hms.config.AwsSecretsManagerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Cloud-native database connection manager using HikariCP connection pool
 * combined with Amazon RDS Proxy for optimized database connections.
 *
 * Replaces raw JDBC DriverManager with HikariCP for:
 * - Connection pooling and reuse
 * - Automatic connection validation and recovery
 * - Proper connection lifecycle management
 * - Integration with Amazon RDS Proxy
 *
 * Database credentials are retrieved from AWS Secrets Manager to eliminate
 * hardcoded credentials from source code.
 *
 * All connection, socket, and pool timeouts are explicitly configured to
 * prevent indefinite hangs and resource exhaustion in cloud environments.
 */
public class DBConnection {

    private static HikariDataSource dataSource;

    static {
        initializeDataSource();
    }

    /**
     * Initializes the HikariCP data source with cloud-optimized settings.
     * Credentials are retrieved from AWS Secrets Manager.
     * Connection URL supports Amazon RDS Proxy via DB_URL environment variable.
     */
    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();

        // Retrieve credentials from AWS Secrets Manager (no hardcoded values)
        String dbUrl = AwsSecretsManagerUtil.getDbUrl();
        String dbUsername = AwsSecretsManagerUtil.getDbUsername();
        String dbPassword = AwsSecretsManagerUtil.getDbPassword();

        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool sizing - optimized for cloud/RDS Proxy environments
        config.setMaximumPoolSize(
                Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MAX_SIZE", "10")));
        config.setMinimumIdle(
                Integer.parseInt(System.getenv().getOrDefault("DB_POOL_MIN_IDLE", "2")));

        // Explicit timeout configurations to prevent indefinite hangs (cr-java-0097)
        // Connection timeout: max time to wait for a connection from the pool (30 seconds)
        config.setConnectionTimeout(30_000L);
        // Idle timeout: max time a connection can sit idle in the pool (10 minutes)
        config.setIdleTimeout(600_000L);
        // Max lifetime: max lifetime of a connection in the pool (30 minutes)
        config.setMaxLifetime(1_800_000L);
        // Keepalive interval: send keepalive pings to prevent stale connections (5 minutes)
        config.setKeepaliveTime(300_000L);
        // Validation timeout: max time to validate a connection (5 seconds)
        config.setValidationTimeout(5_000L);
        // Initialization fail timeout: fail fast if pool cannot be initialized
        config.setInitializationFailTimeout(60_000L);

        // Connection test query for MySQL
        config.setConnectionTestQuery("SELECT 1");

        // Pool name for monitoring and logging
        config.setPoolName("HmsHikariPool");

        // Auto-commit behavior
        config.setAutoCommit(true);

        // MySQL-specific JDBC properties for cloud/RDS Proxy compatibility
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        // Socket and connect timeout for MySQL driver (milliseconds)
        config.addDataSourceProperty("socketTimeout", "30000");
        config.addDataSourceProperty("connectTimeout", "10000");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Returns a connection from the HikariCP connection pool.
     * Connections must be closed after use (try-with-resources recommended)
     * to return them to the pool.
     *
     * @return a pooled database Connection
     */
    public static Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Failed to obtain connection from pool: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the underlying HikariDataSource for advanced usage.
     *
     * @return the HikariDataSource instance
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Closes the connection pool gracefully on application shutdown.
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
