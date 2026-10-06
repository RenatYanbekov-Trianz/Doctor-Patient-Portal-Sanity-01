package com.hms.db;

import com.hms.util.AwsSecretsManagerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cloud-ready database connection manager using HikariCP connection pooling
 * combined with Amazon RDS Proxy for optimized database connections.
 *
 * <p>Replaces raw JDBC {@code DriverManager.getConnection()} with a HikariCP
 * {@link HikariDataSource} to provide efficient connection pooling, proper
 * connection lifecycle management, and seamless integration with Amazon RDS Proxy.</p>
 *
 * <p>Configuration is driven entirely by environment variables and AWS Secrets Manager,
 * following 12-factor app principles (no hardcoded credentials or hostnames):</p>
 * <ul>
 *   <li>{@code DB_HOST}        – RDS Proxy endpoint (or RDS instance hostname).
 *                                Defaults to {@code localhost} if not set.</li>
 *   <li>{@code DB_PORT}        – Database port. Defaults to {@code 3306}.</li>
 *   <li>{@code DB_NAME}        – Database / schema name. Defaults to {@code hospital}.</li>
 *   <li>{@code DB_SECRET_NAME} – AWS Secrets Manager secret name that holds a JSON
 *                                object with {@code username} and {@code password} keys.
 *                                Defaults to {@code hms/db/credentials}.</li>
 * </ul>
 *
 * <p>Remediation for rule <b>cr-java-0073</b> (Direct JDBC Connections):
 * eliminates manual connection management by implementing HikariCP connection
 * pooling library combined with Amazon RDS Proxy.</p>
 *
 * <p>Remediation for rule <b>cr-java-0097</b> (Missing Connection Timeouts):
 * adds explicit connection timeout, socket timeout, and validation timeout to
 * the HikariCP pool configuration and the underlying JDBC URL to prevent
 * indefinite hangs and resource exhaustion in cloud environments.</p>
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    /** Environment variable for the RDS Proxy / RDS instance hostname. */
    private static final String ENV_DB_HOST = "DB_HOST";

    /** Environment variable for the database port. */
    private static final String ENV_DB_PORT = "DB_PORT";

    /** Environment variable for the database / schema name. */
    private static final String ENV_DB_NAME = "DB_NAME";

    /**
     * Environment variable for the AWS Secrets Manager secret name that stores
     * the database credentials as a JSON object:
     * {@code {"username":"<user>","password":"<pass>"}}.
     */
    private static final String ENV_DB_SECRET_NAME = "DB_SECRET_NAME";

    /** Default AWS Secrets Manager secret name for database credentials. */
    private static final String DEFAULT_SECRET_NAME = "hms/db/credentials";

    // -----------------------------------------------------------------------
    // cr-java-0097: Missing Connection Timeouts
    // All timeout values are expressed in milliseconds unless noted otherwise.
    // -----------------------------------------------------------------------

    /**
     * Maximum time (ms) that HikariCP will wait for a connection from the pool
     * before throwing a {@link java.sql.SQLTimeoutException}.
     * Prevents threads from blocking indefinitely when the pool is exhausted.
     */
    private static final int HIKARI_CONNECTION_TIMEOUT_MS = 30_000;   // 30 seconds

    /**
     * Maximum time (ms) that a connection is allowed to sit idle in the pool
     * before being evicted (only applies when pool size > minimumIdle).
     */
    private static final int HIKARI_IDLE_TIMEOUT_MS = 30_000;         // 30 seconds

    /**
     * Maximum lifetime (ms) of a connection in the pool.
     * Connections older than this value are retired and replaced.
     */
    private static final int HIKARI_MAX_LIFETIME_MS = 1_800_000;      // 30 minutes

    /**
     * Maximum time (ms) that HikariCP will wait for a connection to be validated
     * (i.e., for the {@code connectionTestQuery} to return) before considering
     * the connection broken.
     */
    private static final int HIKARI_VALIDATION_TIMEOUT_MS = 5_000;    // 5 seconds

    /**
     * JDBC-level socket timeout (seconds) appended to the JDBC URL.
     * If the database server does not respond within this window the driver
     * throws a {@link java.net.SocketTimeoutException}, preventing threads
     * from hanging indefinitely on a stalled network connection.
     */
    private static final int JDBC_SOCKET_TIMEOUT_SECONDS = 30;        // 30 seconds

    /**
     * JDBC-level connect timeout (seconds) appended to the JDBC URL.
     * Limits how long the MySQL driver waits when establishing the initial
     * TCP connection to the database host.
     */
    private static final int JDBC_CONNECT_TIMEOUT_SECONDS = 10;       // 10 seconds

    /** Singleton HikariCP data source — initialised once, reused for every request. */
    private static volatile HikariDataSource dataSource;

    /** Lock object used for thread-safe lazy initialisation of the data source. */
    private static final Object LOCK = new Object();

    // Private constructor — utility / factory class, not meant to be instantiated.
    private DBConnection() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns a {@link Connection} obtained from the HikariCP connection pool.
     *
     * <p>The pool is initialised lazily on the first call using a double-checked
     * locking pattern, making it safe for concurrent servlet requests.</p>
     *
     * @return a pooled {@link Connection}, or {@code null} if the pool cannot be
     *         initialised or a connection cannot be obtained
     */
    public static Connection getConn() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    dataSource = buildDataSource();
                }
            }
        }

        if (dataSource == null) {
            LOGGER.severe("HikariCP data source could not be initialised; returning null connection.");
            return null;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to obtain a connection from the HikariCP pool: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Builds and configures a {@link HikariDataSource} using values sourced from
     * environment variables and AWS Secrets Manager.
     *
     * <p>Explicit connection, socket, and validation timeouts are configured on
     * both the HikariCP pool and the underlying JDBC URL (cr-java-0097) to
     * prevent indefinite hangs and resource exhaustion in cloud environments.</p>
     *
     * @return a fully configured {@link HikariDataSource}, or {@code null} on failure
     */
    private static HikariDataSource buildDataSource() {
        try {
            // ----------------------------------------------------------------
            // 1. Resolve connection coordinates from environment variables.
            //    Using RDS Proxy endpoint via DB_HOST allows the application to
            //    benefit from connection multiplexing and automatic failover.
            // ----------------------------------------------------------------
            String dbHost     = resolveEnv(ENV_DB_HOST, "localhost");
            String dbPort     = resolveEnv(ENV_DB_PORT, "3306");
            String dbName     = resolveEnv(ENV_DB_NAME, "hospital");
            String secretName = resolveEnv(ENV_DB_SECRET_NAME, DEFAULT_SECRET_NAME);

            // ----------------------------------------------------------------
            // 2. Retrieve credentials from AWS Secrets Manager.
            //    The secret must be a JSON object with "username" and "password".
            // ----------------------------------------------------------------
            String dbUser     = AwsSecretsManagerUtil.getSecretValue(secretName, "username");
            String dbPassword = AwsSecretsManagerUtil.getSecretValue(secretName, "password");

            if (dbUser == null || dbPassword == null) {
                LOGGER.severe(
                        "Database credentials could not be retrieved from AWS Secrets Manager "
                        + "(secret: " + secretName + "). Check that the secret exists and the "
                        + "application has the required IAM permissions.");
                return null;
            }

            // ----------------------------------------------------------------
            // 3. Build the JDBC URL.
            //    cr-java-0097: connectTimeout and socketTimeout are appended to
            //    the JDBC URL so that the MySQL JDBC driver enforces TCP-level
            //    timeouts independently of the HikariCP pool-level timeouts.
            //    - connectTimeout: max time (ms) to establish the TCP connection.
            //    - socketTimeout : max time (ms) to wait for data on an open socket.
            //    useSSL=true is recommended when connecting through RDS Proxy.
            // ----------------------------------------------------------------
            String jdbcUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                    + "?useSSL=true&requireSSL=false&serverTimezone=UTC"
                    + "&useUnicode=true&characterEncoding=UTF-8"
                    + "&connectTimeout=" + (JDBC_CONNECT_TIMEOUT_SECONDS * 1_000)
                    + "&socketTimeout="  + (JDBC_SOCKET_TIMEOUT_SECONDS  * 1_000);

            // ----------------------------------------------------------------
            // 4. Configure HikariCP with explicit timeouts (cr-java-0097).
            //    connectionTimeout : max wait for a connection from the pool.
            //    validationTimeout : max wait for connection liveness check.
            //    idleTimeout       : max idle time before eviction.
            //    maxLifetime       : absolute max age of a pooled connection.
            // ----------------------------------------------------------------
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);

            // Pool sizing — sensible defaults for a cloud-deployed web application.
            // Tune via environment variables or externalised configuration as needed.
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            // cr-java-0097: Explicit timeout configuration to prevent indefinite
            // hangs and resource exhaustion in cloud environments.
            config.setConnectionTimeout(HIKARI_CONNECTION_TIMEOUT_MS);   // 30 s — pool wait
            config.setValidationTimeout(HIKARI_VALIDATION_TIMEOUT_MS);   //  5 s — liveness check
            config.setIdleTimeout(HIKARI_IDLE_TIMEOUT_MS);               // 30 s — idle eviction
            config.setMaxLifetime(HIKARI_MAX_LIFETIME_MS);               // 30 min — max age

            // Validate connections before handing them to the application.
            config.setConnectionTestQuery("SELECT 1");

            // Pool name for easier identification in logs and JMX.
            config.setPoolName("HMS-HikariPool");

            LOGGER.info("Initialising HikariCP connection pool → " + dbHost + ":" + dbPort + "/" + dbName
                    + " [connectionTimeout=" + HIKARI_CONNECTION_TIMEOUT_MS + "ms"
                    + ", validationTimeout=" + HIKARI_VALIDATION_TIMEOUT_MS + "ms"
                    + ", socketTimeout=" + (JDBC_SOCKET_TIMEOUT_SECONDS * 1_000) + "ms]");
            return new HikariDataSource(config);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to build HikariCP data source: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Reads an environment variable, returning {@code defaultValue} when the
     * variable is absent or blank.
     *
     * @param name         environment variable name
     * @param defaultValue fallback value
     * @return resolved value
     */
    private static String resolveEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
}
