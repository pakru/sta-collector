package org.eltex.softwlc.stacollector.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@PropertySource({/* "classpath:application.properties", */ "file:${app.config.path}/macs.properties" })
@EnableCassandraRepositories(basePackages = "org.eltex.softwlc.stacollector.repo")
public class AppConfig extends AbstractCassandraConfiguration {

    public static final String KEYSPACE = "cassandra.keyspace";
    public static final String CONTACTPOINTS = "cassandra.contactpoints";
    private static final String PORT = "cassandra.port";
    public static final String SPARKHOST = "spark.host";

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
        return new String[] { "org.eltex.softwlc.stacollector.entity" };
    }

}
