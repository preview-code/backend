package previewcode.backend.test.helpers;

import com.google.inject.servlet.ServletModule;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Helper annotation for tests that wish to run a Jetty Embedded instance and call it's endpoints.
 *
 * Runs the {@link GuiceResteasyExtension} jUnit extension, which:
 * <br>
 * <br>
 * - Starts a new Embedded Jetty instance once for before all tests
 * <br>
 * - Uses the provided {@link ServletModule} to configure endpoint and dependency bindings.
 * <br>
 * - Stops the server after all tests have been run.
 * <br>
 * - Automatically inject a {@link javax.ws.rs.client.WebTarget} instance configured with the embedded server address/port:
 * <pre>
 * {@code
 *      @Test
 *      public void testApiIsReachable(WebTarget target) {
 *          // WebTarget is automatically provided
 *      }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(GuiceResteasyExtension.class)
public @interface ApiEndPointTest {
    Class<? extends ServletModule> value();
}
