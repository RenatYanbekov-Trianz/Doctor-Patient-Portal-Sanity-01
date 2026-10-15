package com.hms.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Duration;

/**
 * AWS Secrets Manager utility for retrieving database credentials and other secrets.
 * Replaces hardcoded credentials with centralized secret management.
 * Supports automatic rotation and audit logging via AWS Secrets Manager.
 */
public class AwsSecretsManagerUtil {

    private static final String SECRET_NAME = System.getenv().getOrDefault(
            "DB_SECRET_NAME", "hms/database/credentials");
    private static final String AWS_REGION = System.getenv().getOrDefault(
            "AWS_REGION", "us-east-1");

    private static SecretsManagerClient secretsManagerClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Cached secret values to avoid repeated API calls
    private static String cachedDbUrl;
    private static String cachedDbUsername;
    private static String cachedDbPassword;
    private static long cacheExpiryTime = 0;
    private static final long CACHE_TTL_MS = 300_000L; // 5 minutes cache TTL

    private AwsSecretsManagerUtil() {
        // Utility class - no instantiation
    }

    /**
     * Returns a configured SecretsManagerClient with appropriate timeouts.
     */
    private static SecretsManagerClient getSecretsManagerClient() {
        if (secretsManagerClient == null) {
            secretsManagerClient = SecretsManagerClient.builder()
                    .region(Region.of(AWS_REGION))
                    .overrideConfiguration(config -> config
                            .apiCallTimeout(Duration.ofSeconds(10))
                            .apiCallAttemptTimeout(Duration.ofSeconds(5)))
                    .build();
        }
        return secretsManagerClient;
    }

    /**
     * Retrieves the database URL from AWS Secrets Manager or environment variables.
     * Falls back to environment variable DB_URL if secret retrieval fails.
     */
    public static String getDbUrl() {
        refreshCacheIfNeeded();
        if (cachedDbUrl != null) {
            return cachedDbUrl;
        }
        // Fallback to environment variable
        return System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/hospital");
    }

    /**
     * Retrieves the database username from AWS Secrets Manager or environment variables.
     */
    public static String getDbUsername() {
        refreshCacheIfNeeded();
        if (cachedDbUsername != null) {
            return cachedDbUsername;
        }
        // Fallback to environment variable
        return System.getenv().getOrDefault("DB_USERNAME", "root");
    }

    /**
     * Retrieves the database password from AWS Secrets Manager or environment variables.
     */
    public static String getDbPassword() {
        refreshCacheIfNeeded();
        if (cachedDbPassword != null) {
            return cachedDbPassword;
        }
        // Fallback to environment variable
        return System.getenv().getOrDefault("DB_PASSWORD", "");
    }

    /**
     * Refreshes the cached secret values if the cache has expired.
     */
    private static synchronized void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now < cacheExpiryTime && cachedDbUrl != null) {
            return; // Cache is still valid
        }

        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();

            GetSecretValueResponse response = getSecretsManagerClient()
                    .getSecretValue(request);

            String secretString = response.secretString();
            JsonNode secretJson = objectMapper.readTree(secretString);

            cachedDbUrl = secretJson.has("db_url")
                    ? secretJson.get("db_url").asText()
                    : System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/hospital");

            cachedDbUsername = secretJson.has("username")
                    ? secretJson.get("username").asText()
                    : System.getenv().getOrDefault("DB_USERNAME", "root");

            cachedDbPassword = secretJson.has("password")
                    ? secretJson.get("password").asText()
                    : System.getenv().getOrDefault("DB_PASSWORD", "");

            cacheExpiryTime = now + CACHE_TTL_MS;

        } catch (Exception e) {
            // Log the error and fall back to environment variables
            System.err.println("[AwsSecretsManagerUtil] Failed to retrieve secret from AWS Secrets Manager: "
                    + e.getMessage() + ". Falling back to environment variables.");
            cachedDbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/hospital");
            cachedDbUsername = System.getenv().getOrDefault("DB_USERNAME", "root");
            cachedDbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");
            // Short cache TTL on failure to retry sooner
            cacheExpiryTime = now + 30_000L;
        }
    }
}
