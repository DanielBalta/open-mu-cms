package io.github.felipeemerson.openmuapi.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;

public class PreDBConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        logger.info("=================================================================================================");
        logger.info("Application Configuration (before DB initialization):");
        logger.info("Active Profiles: {}", Arrays.toString(env.getActiveProfiles()));
        logger.info("Server Port: {}", env.getProperty("server.port"));
        logger.info("Application Name: {}", env.getProperty("spring.application.name"));

        logger.info("Database Configuration:");
        logger.info("Driver Class Name: {}", env.getProperty("spring.datasource.driver-class-name"));
        logger.info("URL: {}", env.getProperty("spring.datasource.url"));
        logger.info("Username: {}", env.getProperty("spring.datasource.username"));
        // Password should generally be masked or not logged for security reasons
        logger.info("Password: {}", "*****");

        logger.info("JPA Configuration:");
        logger.info("DDL Auto: {}", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        logger.info("Physical Naming Strategy: {}", env.getProperty("spring.jpa.hibernate.naming.physical-strategy"));
        logger.info("Globally Quoted Identifiers: {}", env.getProperty("spring.jpa.properties.hibernate.globally_quoted_identifiers"));

        logger.info("SQL Init Configuration:");
        logger.info("Mode: {}", env.getProperty("spring.sql.init.mode"));
        logger.info("Continue on Error: {}", env.getProperty("spring.sql.init.continue-on-error"));
        logger.info("Schema Locations: {}", env.getProperty("spring.sql.init.schema-locations"));

        logger.info("JWT Configuration:");
        logger.info("Private Key: {}", env.getProperty("jwt.private.key"));
        logger.info("Public Key: {}", env.getProperty("jwt.public.key"));

        logger.info("Admin Panel Configuration:");
        logger.info("Username: {}", env.getProperty("admin.panel.username"));
        // Password should generally be masked or not logged for security reasons
        logger.info("Password: {}", "*****");
        logger.info("URL: {}", env.getProperty("admin.panel.url"));
        logger.info("=================================================================================================");
    }
}
