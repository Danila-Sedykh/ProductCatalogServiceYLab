package marketplace.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import marketplace.domain.Product;
import marketplace.out.cache.LruCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableTransactionManagement
public class DataConfig {
    @Value("${db.url}")
    private String url;
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String password;
    @Value("${db.maximumPoolSize}")
    private int maxPool;
    @Value("${db.liquibaseSchema}")
    private String liquibaseSchema;
    @Value("${db.appSchema}")
    private String appSchema;
    @Value("${liquibase.changelog}")
    private String changelog;

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(maxPool);
        return new HikariDataSource(cfg);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource ds) {
        SpringLiquibase lb = new SpringLiquibase();
        lb.setDataSource(ds);
        lb.setChangeLog(changelog);
        lb.setDefaultSchema(appSchema);
        lb.setLiquibaseSchema(liquibaseSchema);
        return lb;
    }

    @Bean
    public LruCache<String, List<Product>> productCache() {
        return new LruCache<>(100);
    }

}
