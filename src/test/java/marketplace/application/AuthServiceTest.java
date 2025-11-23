package marketplace.application;

import marketplace.config.*;
import marketplace.db.DataSourceFactory;
import marketplace.db.LiquibaseRunner;
import marketplace.out.repository.UserRepositoryJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthServiceTest {
    private Path tmp;
    private UserRepositoryJdbc repo;
    private AuthService auth;
    private static final String APP_SCHEMA = "marketplace";
    private DataSource dataSource = DataSourceFactory.create(ConfigLoader.get("db.url"),
            ConfigLoader.get("db.username"),
            ConfigLoader.get("db.password"),
            Integer.parseInt(ConfigLoader.get("db.maximumPoolSize"))
    );

    @BeforeEach
    void init() throws Exception {
        LiquibaseRunner.runLiquibase(
                dataSource,
                ConfigLoader.get("liquibase.changelog"),
                ConfigLoader.get("db.liquibaseSchema"),
                APP_SCHEMA
        );
        tmp = Files.createTempFile("users",".db");
        repo = new UserRepositoryJdbc(dataSource, APP_SCHEMA);
        auth = new AuthService(repo);
    }

    @AfterEach
    void cleanup() throws Exception { Files.deleteIfExists(tmp); }

    @Test
    void registerAndAuthenticate() {
        boolean r = auth.register("testuser","pass","USER");
        assertThat(r).isTrue();
        assertThat(auth.authenticate("testuser","pass").isPresent()).isTrue();
    }

}
