package previewcode.backend.test.helpers;

import org.junit.jupiter.api.extension.ExtendWith;
import previewcode.backend.database.model.DefaultCatalog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helper annotation for test classes that wish to interact with the local database.
 *
 * Runs the {@link DatabaseTestExtension} jUnit 5 extension, which:
 * <br>
 * <br>
 *  - Automatically injects DSLContext instances in test methods:
 * <pre>
 * {@code
 *      @Test
 *      public void test(DSLContext db) {
 *          // DSLContext is automatically provided by the extension
 *      }
 *
 * }</pre>
 *
 *  - After each test, truncates all tables for the {@link DefaultCatalog} class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(DatabaseTestExtension.class)
public @interface DatabaseTests { }