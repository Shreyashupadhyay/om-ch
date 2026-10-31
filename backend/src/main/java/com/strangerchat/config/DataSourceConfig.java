package com.strangerchat.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String dbUrl = databaseUrl;
        if (!StringUtils.hasText(dbUrl)) {
            dbUrl = System.getenv("DATABASE_URL");
        }

        if (StringUtils.hasText(dbUrl)) {
            HikariDataSource ds = new HikariDataSource();
            ds.setMaximumPoolSize(maxPoolSize);
            ds.setMinimumIdle(minIdle);
            ds.setConnectionTimeout(connectionTimeout);

            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    String parseUrl = dbUrl;
                    if (parseUrl.startsWith("postgres://")) {
                        parseUrl = "postgresql://" + parseUrl.substring("postgres://".length());
                    }
                    URI uri = new URI(parseUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null) {
                        String[] parts = userInfo.split(":", 2);
                        ds.setUsername(URLDecoder.decode(parts[0], StandardCharsets.UTF_8));
                        if (parts.length > 1) {
                            ds.setPassword(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                        }
                    }
                    int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    String query = uri.getQuery();
                    String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path + (query != null ? "?" + query : "");
                    ds.setJdbcUrl(jdbcUrl);
                    ds.setDriverClassName("org.postgresql.Driver");
                    log.info("Initialized DataSource from DATABASE_URL for host: {}", uri.getHost());
                    return ds;
                } catch (Exception e) {
                    log.error("Failed to parse DATABASE_URL: {}. Falling back to standard DataSourceProperties.", e.getMessage(), e);
                }
            } else if (dbUrl.startsWith("jdbc:")) {
                ds.setJdbcUrl(dbUrl);
                if (StringUtils.hasText(properties.getUsername())) {
                    ds.setUsername(properties.getUsername());
                }
                if (StringUtils.hasText(properties.getPassword())) {
                    ds.setPassword(properties.getPassword());
                }
                if (StringUtils.hasText(properties.getDriverClassName())) {
                    ds.setDriverClassName(properties.getDriverClassName());
                } else {
                    ds.setDriverClassName("org.postgresql.Driver");
                }
                log.info("Initialized DataSource from JDBC DATABASE_URL");
                return ds;
            }
        }

        // Standard fallback: initialize using Spring Boot's DataSourceProperties (H2 for tests, or DB_HOST config)
        HikariDataSource defaultDs = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        defaultDs.setMaximumPoolSize(maxPoolSize);
        defaultDs.setMinimumIdle(minIdle);
        defaultDs.setConnectionTimeout(connectionTimeout);
        log.info("Initialized DataSource from standard configuration properties.");
        return defaultDs;
    }
}

