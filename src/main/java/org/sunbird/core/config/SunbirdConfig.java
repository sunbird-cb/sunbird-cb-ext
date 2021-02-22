package org.sunbird.core.config;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@ConfigurationProperties("spring.data.cassandra.sb")
@EnableCassandraRepositories(basePackages = "org.sunbird.progress.cassandraRepo", cassandraTemplateRef = "sunbirdTemplate")
public class SunbirdConfig extends CassandraConfig {

    private Logger logger = LoggerFactory.getLogger(SunbirdConfig.class);

    @Value("${spring.data.cassandra.sb.username}")
    private String sunbirdUser;

    @Value("${spring.data.cassandra.sb.password}")
    private String sunbirdPassword;

    @Override
    @Primary
    @Bean(name = "sunbirdTemplate")
    public CassandraAdminTemplate cassandraTemplate() throws Exception {
        return new CassandraAdminTemplate(session().getObject(), cassandraConverter());
    }

    @Override
    @Bean(name = "sunbirdSession")
    public CassandraSessionFactoryBean session() {

        AuthProvider authProvider = new PlainTextAuthProvider(sunbirdUser, sunbirdPassword);

        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(Cluster.builder().addContactPoint(getContactPoints()).withPort(getPort())
                .withAuthProvider(authProvider).withoutJMXReporting().build());
        session.setConverter(cassandraConverter());
        session.setKeyspaceName(getKeyspaceName());
        session.setSchemaAction(getSchemaAction());
        session.setStartupScripts(getStartupScripts());
        session.setShutdownScripts(getShutdownScripts());
        logger.info("Cassandra session created for " + getKeyspaceName() + "keyspace with IP : " + getContactPoints()
                + ":" + getPort());
        return session;
    }
}