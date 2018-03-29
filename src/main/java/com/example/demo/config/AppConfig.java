package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@PropertySource(value = { "classpath:application.properties" })
@EnableCassandraRepositories(basePackages = "com.example.demo.repo")
public class AppConfig extends AbstractCassandraConfiguration {
    /**
     * Constant String for Keyspace
     */
    public static final String KEYSPACE = "cassandra.keyspace";
    /**
     * Constant String for ContactPoints
     */
    public static final String CONTACTPOINTS = "cassandra.contactpoints";
    /**
     * Constant String for Port
     */
    private static final String PORT = "cassandra.port";

    @Autowired
    private Environment environment;

    @Override
    protected String getKeyspaceName() {
        return environment.getProperty(KEYSPACE);
    }

    @Override
    protected String getContactPoints() {
        return environment.getProperty(CONTACTPOINTS);
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[] { "com.example.demo.entity" };
    }

}
