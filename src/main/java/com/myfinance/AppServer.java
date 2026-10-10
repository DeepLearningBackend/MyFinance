package com.myfinance;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Embedded Apache Tomcat 10.1 Server Launcher.
 */
public class AppServer {

    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "/myfinance";

    /**
     * Boots the embedded Apache Tomcat 10.1 server on port 8080.
     *
     * @param args command line arguments
     * @throws Exception if Tomcat fails to initialize or start
     */
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setHostname("0.0.0.0");
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
        
        org.apache.catalina.connector.Connector connector = tomcat.getConnector();
        connector.setProperty("address", "0.0.0.0");

        File webappDir = new File("src/main/webapp");
        StandardContext ctx = (StandardContext) tomcat.addWebapp(CONTEXT_PATH, webappDir.getAbsolutePath());
        ctx.setParentClassLoader(AppServer.class.getClassLoader());

        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        String localIp = "localhost";
        try {
            localIp = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {}

        System.out.println("=======================================================");
        System.out.println("🚀 MyFinance 2.0 deployed successfully on Tomcat 10.1 (Listening on 0.0.0.0)!");
        System.out.println("👉 Localhost Access:     http://localhost:" + PORT + CONTEXT_PATH);
        System.out.println("👉 Phone/Network Access: http://" + localIp + ":" + PORT + CONTEXT_PATH);
        System.out.println("👉 Mobile SMS Webhook:   http://" + localIp + ":" + PORT + CONTEXT_PATH + "/api/sms-sync");
        System.out.println("👉 Login URL:            http://localhost:" + PORT + CONTEXT_PATH + "/auth");
        System.out.println("=======================================================");
        com.myfinance.util.DBConnection.printTimezoneDiagnostics();

        tomcat.start();
        tomcat.getServer().await();
    }
}
