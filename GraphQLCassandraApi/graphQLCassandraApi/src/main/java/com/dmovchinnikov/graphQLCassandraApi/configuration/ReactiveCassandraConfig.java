package com.dmovchinnikov.graphQLCassandraApi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableReactiveCassandraRepositories("com.practice.ReactiveCassandraDemo.model")
public class ReactiveCassandraConfig extends AbstractReactiveCassandraConfiguration {
    @Value("${spring.data.cassandra.contact-points:placeholder}")
    private String contactPoints;

    @Value("${spring.data.cassandra.local-datacenter}")
    private String localDatacenter;
    @Value("${spring.data.cassandra.port:0000}")
    private int port;
    @Value("${spring.data.cassandra.keyspace-name:placeholder}")
    private String keySpace;
    @Value("${spring.data.cassandra.username:test}")
    private String username;
    @Value("${spring.data.cassandra.password:test}")
    private String password;
    @Value("${spring.data.cassandra.schema-action}")
    private SchemaAction schemaAction;

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDatacenter;
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return schemaAction;
    }

    @Override
    protected KeyspacePopulator keyspaceCleaner() {
        return super.keyspaceCleaner();
    }

    @Override
    protected KeyspacePopulator keyspacePopulator() {
        return super.keyspacePopulator();
    }

    @Override
    protected String getKeyspaceName() {
        return keySpace;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        final CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
                .ifNotExists().with(KeyspaceOption.DURABLE_WRITES, true).withSimpleReplication();
        List<CreateKeyspaceSpecification> list = new ArrayList<>();
        list.add(specification);
        return list;
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        List<DropKeyspaceSpecification> list = new ArrayList<>();
        list.add(DropKeyspaceSpecification.dropKeyspace(getKeyspaceName()));
        return list;
    }
}