package previewcode.backend.test.helpers;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import previewcode.backend.database.model.DefaultCatalog;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTestExtension extends TestStore<DSLContext> implements ParameterResolver, AfterEachCallback {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestExtension.class);

    private static final String userName = "admin";
    private static final String password = "password";
    private static final String url = "jdbc:postgresql://localhost:5432/preview_code";


    @Override
    public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return DSLContext.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Settings settings = new Settings()
                .withExecuteLogging(true);

        logger.debug("Obtaining database connection from DriverManager...");
        try {
            DSLContext dslContext = DSL.using(DriverManager.getConnection(url, userName, password), SQLDialect.POSTGRES_9_5, settings);
            putObjectToStore(extensionContext, dslContext);
            return dslContext;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        DSLContext db = getFromStore(context);
        if (db != null) {
            logger.debug("Commence database cleanup.");
            DefaultCatalog.DEFAULT_CATALOG.getSchemas().forEach(schema -> {
                logger.debug("\tCleaning schema: " + schema.getName());
                schema.getTables().forEach(t -> {
                    logger.debug("\t\tTruncating table: " + t.getName());
                    db.truncate(t).restartIdentity().cascade().execute();
                });
                schema.getSequences().forEach(s -> {
                    logger.debug("\t\tRestarting sequence: " + s.getName());
                    db.alterSequence(s).restart().execute();
                });
            });

            logger.debug("Database truncated.");
        }
    }
}
