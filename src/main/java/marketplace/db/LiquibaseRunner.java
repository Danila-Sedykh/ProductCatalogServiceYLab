package marketplace.db;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;


public final class LiquibaseRunner {
    private LiquibaseRunner() {
    }

    public static void runLiquibase(DataSource ds, String changelog, String liquibaseSchema, String defaultSchema) {
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + liquibaseSchema);
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + defaultSchema);

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));

            database.setLiquibaseSchemaName(liquibaseSchema);
            database.setDefaultSchemaName(defaultSchema);

            try (Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database)) {
                liquibase.setChangeLogParameter("db.liquibaseSchema", liquibaseSchema);
                liquibase.setChangeLogParameter("db.appSchema", defaultSchema);

                liquibase.update(new Contexts(), new LabelExpression());
            }
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}

