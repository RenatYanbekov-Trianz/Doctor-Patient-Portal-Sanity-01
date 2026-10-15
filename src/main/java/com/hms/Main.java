package com.hms;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Main entry point for the Doctor-Patient Portal application.
 *
 * This class bootstraps an embedded Apache Tomcat server, enabling the
 * application to run as a self-contained executable JAR without requiring
 * an external application server.
 *
 * This replaces WAR packaging (cr-java-0107 remediation) to enable:
 * - Container deployment on Amazon ECS, EKS, or AWS Fargate
 * - Simplified containerization with smaller container images
 * - Faster startup times
 * - No dependency on external application servers
 *
 * Configuration via environment variables (12-factor app principles):
 * - PORT: HTTP port to listen on (default: 8080)
 * - CONTEXT_PATH: Application context path (default: "")
 */
public class Main {

    public static void main(String[] args) throws LifecycleException {

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String contextPath = System.getenv().getOrDefault("CONTEXT_PATH", "");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Trigger connector creation

        // Locate the webapp directory
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            // When running from JAR, use the classpath webapp resources
            webappDirLocation = ".";
        }

        Context ctx = tomcat.addWebapp(contextPath, new File(webappDirLocation).getAbsolutePath());

        // Add compiled classes to the context
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            StandardRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        System.out.println("[Main] Starting Doctor-Patient Portal on port " + port);
        tomcat.start();
        tomcat.getServer().await();
    }
}
