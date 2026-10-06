package com.hms;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.net.URISyntaxException;

/**
 * cr-java-0107: WAR Packaging → Executable JAR with Embedded Tomcat
 *
 * <p>This launcher bootstraps an embedded Apache Tomcat server so the application
 * can be packaged as a self-contained executable JAR and deployed directly on
 * AWS ECS, EKS, or AWS Fargate without requiring an external application server.
 *
 * <p>The server port is read from the {@code PORT} environment variable (default 8080),
 * following 12-factor app principles for cloud-native configuration.
 *
 * <p>Usage:
 * <pre>
 *   java -jar Doctor-Patient-Portal.jar
 * </pre>
 */
public class EmbeddedTomcatLauncher {

    /** Default HTTP port if the PORT environment variable is not set. */
    private static final int DEFAULT_PORT = 8080;

    /**
     * Application entry point. Starts an embedded Tomcat instance and blocks
     * until the server is stopped.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if the server fails to start
     */
    public static void main(String[] args) throws Exception {
        // Read port from environment variable for cloud-native configuration (12-factor)
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("[EmbeddedTomcatLauncher] Invalid PORT value '" + portEnv
                        + "', falling back to default " + DEFAULT_PORT);
            }
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        // Configure the HTTP connector
        Connector connector = tomcat.getConnector();
        connector.setPort(port);

        // Resolve the webapp directory relative to the JAR location
        String webappDirLocation = resolveWebappDir();
        System.out.println("[EmbeddedTomcatLauncher] Webapp directory: " + webappDirLocation);

        // Add the web application context
        Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

        // Add compiled classes and resources to the web application's classpath
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(
                    resources,
                    "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(),
                    "/"));
            ctx.setResources(resources);
        }

        tomcat.start();
        System.out.println("[EmbeddedTomcatLauncher] Server started on port " + port);
        tomcat.getServer().await();
    }

    /**
     * Resolves the webapp directory. When running from an executable JAR the
     * webapp resources are expected to be on the classpath under {@code webapp/}.
     * During development the standard Maven layout {@code src/main/webapp} is used.
     */
    private static String resolveWebappDir() {
        // Try src/main/webapp (development / Maven build layout)
        File devWebapp = new File("src/main/webapp");
        if (devWebapp.exists()) {
            return devWebapp.getPath();
        }

        // Fallback: webapp directory next to the JAR (exploded deployment)
        File siblingWebapp = new File("webapp");
        if (siblingWebapp.exists()) {
            return siblingWebapp.getPath();
        }

        // Last resort: use the JAR's own directory (for classpath-embedded resources)
        try {
            return new File(EmbeddedTomcatLauncher.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getPath();
        } catch (URISyntaxException e) {
            return ".";
        }
    }
}
