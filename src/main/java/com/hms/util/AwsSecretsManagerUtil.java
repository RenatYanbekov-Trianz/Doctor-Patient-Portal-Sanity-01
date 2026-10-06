package com.hms.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for retrieving secrets from AWS Secrets Manager.
 *
 * <p>This utility replaces hardcoded credentials (e.g., database passwords, API keys)
 * with secure lookups from AWS Secrets Manager, following the remediation strategy
 * for rule cr-java-0113 (Lack of Externalized Secrets).</p>
 *
 * <p>Remediation for rule <b>cr-java-0097</b> (Missing Connection Timeouts):
 * the AWS SDK {@link ClientConfiguration} is explicitly configured with
 * connection timeout, socket timeout, and request timeout values to prevent
 * the Secrets Manager API calls from hanging indefinitely in cloud environments
 * with variable network latency or transient service failures.</p>
 *
 * <p>Usage example:
 * <pre>
 *   String dbPassword = AwsSecretsManagerUtil.getSecretValue("hms/db/password", "password");
 * </pre>
 * </p>
 *
 * <p>Secrets should be stored in AWS Secrets Manager as JSON key-value pairs, e.g.:
 * <pre>
 *   Secret name : hms/db/credentials
 *   Secret value: {"username":"root","password":"&lt;actual-password&gt;"}
 * </pre>
 * </p>
 */
public class AwsSecretsManagerUtil {

    private static final Logger LOGGER = Logger.getLogger(AwsSecretsManagerUtil.class.getName());

    /**
     * AWS region is read from the environment variable AWS_REGION (or AWS_DEFAULT_REGION).
     * Falls back to "us-east-1" if neither is set.
     */
    private static final String AWS_REGION = resolveRegion();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // -----------------------------------------------------------------------
    // cr-java-0097: Missing Connection Timeouts — AWS SDK ClientConfiguration
    // All timeout values are expressed in milliseconds.
    // -----------------------------------------------------------------------

    /**
     * Maximum time (ms) to establish a TCP connection to the AWS Secrets Manager
     * endpoint. Prevents the SDK from blocking indefinitely when the endpoint is
     * unreachable.
     */
    private static final int AWS_CONNECTION_TIMEOUT_MS = 5_000;   // 5 seconds

    /**
     * Maximum time (ms) to wait for data on an open socket to the AWS Secrets
     * Manager endpoint. Guards against stalled responses after the connection
     * has been established.
     */
    private static final int AWS_SOCKET_TIMEOUT_MS = 10_000;      // 10 seconds

    /**
     * Maximum time (ms) for a complete API call (including retries) to the AWS
     * Secrets Manager endpoint. Acts as an overall deadline for the request.
     */
    private static final int AWS_REQUEST_TIMEOUT_MS = 15_000;     // 15 seconds

    /**
     * Pre-built, reusable {@link ClientConfiguration} with explicit timeouts.
     * Shared across all client instances created by this utility.
     */
    private static final ClientConfiguration CLIENT_CONFIG = buildClientConfiguration();

    // Private constructor — utility class, not meant to be instantiated.
    private AwsSecretsManagerUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Retrieves the full secret string for the given secret name from AWS Secrets Manager.
     *
     * @param secretName the name or ARN of the secret in AWS Secrets Manager
     * @return the secret string value, or {@code null} if retrieval fails
     */
    public static String getSecret(String secretName) {
        // cr-java-0097: AWS SDK client is built with explicit connection, socket,
        // and request timeouts via ClientConfiguration to prevent indefinite hangs.
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(AWS_REGION)
                .withClientConfiguration(CLIENT_CONFIG)
                .build();

        GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(secretName);

        try {
            GetSecretValueResult result = client.getSecretValue(request);
            return result.getSecretString();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to retrieve secret ''{0}'' from AWS Secrets Manager: {1}",
                    new Object[]{secretName, e.getMessage()});
            return null;
        }
    }

    /**
     * Retrieves a specific key from a JSON-formatted secret stored in AWS Secrets Manager.
     *
     * @param secretName the name or ARN of the secret in AWS Secrets Manager
     * @param key        the JSON key whose value should be returned
     * @return the value associated with {@code key}, or {@code null} if not found or on error
     */
    public static String getSecretValue(String secretName, String key) {
        String secretJson = getSecret(secretName);
        if (secretJson == null) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(secretJson);
            JsonNode valueNode = node.get(key);
            return (valueNode != null) ? valueNode.asText() : null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to parse secret ''{0}'' for key ''{1}'': {2}",
                    new Object[]{secretName, key, e.getMessage()});
            return null;
        }
    }

    /**
     * Builds a {@link ClientConfiguration} with explicit connection, socket, and
     * request timeouts (cr-java-0097) to prevent AWS SDK calls from hanging
     * indefinitely in cloud environments with variable network conditions.
     *
     * @return configured {@link ClientConfiguration} instance
     */
    private static ClientConfiguration buildClientConfiguration() {
        ClientConfiguration config = new ClientConfiguration();
        // Maximum time (ms) to establish a TCP connection to the AWS endpoint.
        config.setConnectionTimeout(AWS_CONNECTION_TIMEOUT_MS);
        // Maximum time (ms) to wait for data on an established socket.
        config.setSocketTimeout(AWS_SOCKET_TIMEOUT_MS);
        // Maximum time (ms) for a complete API call including retries.
        config.setRequestTimeout(AWS_REQUEST_TIMEOUT_MS);
        return config;
    }

    /**
     * Resolves the AWS region from environment variables.
     * Checks AWS_REGION first, then AWS_DEFAULT_REGION, then falls back to "us-east-1".
     */
    private static String resolveRegion() {
        String region = System.getenv("AWS_REGION");
        if (region == null || region.isEmpty()) {
            region = System.getenv("AWS_DEFAULT_REGION");
        }
        return (region != null && !region.isEmpty()) ? region : "us-east-1";
    }
}
