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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class DataConfig {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.maximum-pool-size}")
    private int maxPool;
    @Value("${spring.liquibase.db.liquibaseSchema}")
    private String liquibaseSchema;
    @Value("${spring.liquibase.db.appSchema}")
    private String appSchema;
    @Value("${spring.liquibase.change-log}")
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
    public SpringLiquibase liquibase(DataSource ds,
                                     @Value("${spring.liquibase.change-log}") String changelog,
                                     @Value("${spring.liquibase.db.liquibaseSchema}") String liquibaseSchema,
                                     @Value("${spring.liquibase.db.appSchema}") String appSchema) {
        SpringLiquibase lb = new SpringLiquibase();
        lb.setDataSource(ds);
        lb.setChangeLog(changelog);
        lb.setDefaultSchema(appSchema);
        lb.setLiquibaseSchema(liquibaseSchema);

        Map<String, String> params = new HashMap<>();
        params.put("db.appSchema", appSchema);
        params.put("db.liquibaseSchema", liquibaseSchema);
        lb.setChangeLogParameters(params);
        return lb;
    }

    @Bean
    public LruCache<String, List<Product>> productCache() {
        return new LruCache<>(100);
    }

}
