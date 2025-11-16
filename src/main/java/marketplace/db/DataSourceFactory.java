package marketplace.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    public static DataSource create(String jdbcUrl, String user, String pass, int maxPool) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(user);
        cfg.setPassword(pass);
        cfg.setMaximumPoolSize(maxPool);
        cfg.setPoolName("hikari-pool");
        return new HikariDataSource(cfg);
    }
}
