package previewcode.backend;

import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.jolbox.bonecp.BoneCPDataSource;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.util.Base64;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import previewcode.backend.api.filter.GitHubAccessTokenFilter;
import previewcode.backend.api.filter.IJWTTokenCreator;
import previewcode.backend.api.filter.JWTTokenCreator;
import previewcode.backend.api.v1.*;
import previewcode.backend.services.interpreters.GitHubAuthInterpreter;
import previewcode.backend.services.http.HttpRequestExecutor;
import previewcode.backend.services.http.IHttpRequestExecutor;
import previewcode.backend.services.DatabaseService;
import previewcode.backend.services.GithubService;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.ActionCache;

import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import javax.ws.rs.NotAuthorizedException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * The main module of the backend
 * @author PReview-Code
 *
 */
public class MainModule extends APIModule {

    private static final Logger logger = LoggerFactory.getLogger(MainModule.class);
    private static final Algorithm RSA_PRIVATE_KEY = initPrivateRSAKey();
    private static final SecretKeySpec GITHUB_WEBHOOK_SECRET = initGitHubWebhookSecret();
    private static final String INTEGRATION_ID = initIntegrationId();
    private static final DataSource DATA_SOURCE = initConnectionPool();

    @SuppressWarnings("PointlessBinding")
    @Override
    public void configureServlets() {
        super.configureServlets();

        // v1
        this.bind(StatusAPI.class);
        this.bind(PullRequestAPI.class);
        this.bind(CommentsAPI.class);
        this.bind(AssigneesAPI.class);
        this.bind(TrackerAPI.class);
        this.bind(WebhookAPI.class);

        this.bind(GitHubAccessTokenFilter.class);
        this.bind(ResteasyJackson2Provider.class);

        this.bind(IDatabaseService.class).to(DatabaseService.class);

        try {
            HttpRequestExecutor http = new HttpRequestExecutor();
            this.bind(IHttpRequestExecutor.class).toInstance(http);
        } catch (IOException e) {
            logger.error("Failed to instantiate HTTP Cache!", e);
            System.exit(-1);
        }
        this.bind(IJWTTokenCreator.class).to(JWTTokenCreator.class);

        ActionCache.Builder b = new ActionCache.Builder();
        ActionCache cache = GitHubAuthInterpreter
                .configure(b).maximumEntries(10000).build();

        this.bind(ActionCache.class).toInstance(cache);

        initializeFireBase();
    }

    private void initializeFireBase() {
        try {
            logger.info("Loading Firebase auth...");
            FileInputStream file = new FileInputStream(System.getenv("FIREBASE_AUTH"));
            // Initialize the app with a service account, granting admin privileges
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(file)
                    .setDatabaseUrl("https://preview-code.firebaseio.com/").build();
            FirebaseApp.initializeApp(options);
        } catch (NullPointerException e) {
            logger.error("FIREBASE_AUTH environmental variable was not set");
            System.exit(-1);
        } catch (FileNotFoundException e) {
            logger.error("Failed to load Firebase config", e);
            System.exit(-1);
        }
    }

    @Provides
    private static DSLContext provideJooqDSL() {
        Settings settings = new Settings().withExecuteLogging(true);
        return DSL.using(DATA_SOURCE, SQLDialect.POSTGRES, settings);
    }

    @Provides
    public DataSource provideDataSource() {
        return DATA_SOURCE;
    }

    private static DataSource initConnectionPool() {
        try {
            logger.info("Instantiating connection pool...");
            BoneCPDataSource result = new BoneCPDataSource();
            result.setDriverClass("org.postgresql.Driver");
            result.setJdbcUrl("jdbc:postgresql://localhost:5432/preview_code");
            result.setUsername("admin");
            result.setPassword("password");
            result.setDefaultAutoCommit(true);
            result.setPartitionCount(4);
            result.setMinConnectionsPerPartition(1);
            result.setMaxConnectionsPerPartition(10);
            return result;
        } catch (Exception e) {
            logger.error("Unable to create JDBC DataSource: ", e);
        }
        System.exit(-1);
        return null;
    }

    /**
     * Provides the signing algorithm to sign JWT keys destined for authenticating
     * with GitHub Integrations.
     */
    @Provides
    public Algorithm provideJWTSigningAlgo() {
        return RSA_PRIVATE_KEY;
    }

    private static Algorithm initPrivateRSAKey() {
        try {
            logger.info("Loading GitHub Integration RSA key...");
            File file = new File(System.getenv("INTEGRATION_KEY"));
            String key = Files.toString(file, Charsets.UTF_8)
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("\n", "").trim();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(key));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return Algorithm.RSA256((RSAPrivateKey) kf.generatePrivate(keySpec));
        } catch (NullPointerException e) {
            logger.error("Environmental variable for GitHub Integration RSA not set");
        } catch (Exception e) {
            logger.error("Failed to load GitHub Integration RSA key:", e);
        }
        System.exit(-1);
        return null;
    }

    /**
     * Method to declare Named key "github.webhook.secret" to obtain the webhook secret.
     */
    @Provides
    @Named("github.webhook.secret")
    public SecretKeySpec provideGitHubWebhookSecret() {
        return GITHUB_WEBHOOK_SECRET;
    }

    private static SecretKeySpec initGitHubWebhookSecret() {
        try {
            logger.info("Loading GitHub Integration Webhook key...");
            File file = new File(System.getenv("WEBHOOK_SECRET"));
            final String secret = Files.toString(file, Charsets.UTF_8).trim();
            return new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        } catch (IOException e) {
            logger.error("Failed to load GitHub Integration webhook secret:", e);
        } catch (NullPointerException e){
            logger.error("Environmental variable for GitHub Integration webhook secret not set");
        }
        System.exit(-1);
        return null;
    }

    /**
     * Method to declare Named key "integration.id" to obtain the current GitHub Integration id.
     */
    @Provides
    @Named("integration.id")
    public String provideIntegrationId() {
        return INTEGRATION_ID;
    }

    private static String initIntegrationId() {
        try {
            logger.info("Loading GitHub Integration ID...");
            return Files.toString(new File(System.getenv("INTEGRATION_ID")), Charsets.UTF_8).trim();
        } catch (IOException e) {
            logger.error("Failed to load GitHub Integration ID:", e);
        } catch (NullPointerException e){
            logger.error("Environmental variable for the GitHub Integration ID not set");
        }
        System.exit(-1);
        return null;
    }

    /**
     * Method to declare Named key "github.user" to obtain the current GitHub instance
     * @throws Exception if key was not set
     */
    @Provides
    @Named("github.user")
    @RequestScoped
    public GitHub provideGitHubConnection() {
        throw new NotAuthorizedException("user id must be manually seeded");
    }

    /**
     * Method to declare Named key "github.token.builder" to amend a OKHTTP Request with authorization info.
     * @throws Exception if not set via GitHubAccessTokenFilter.
     */
    @Provides
    @Named("github.token.builder")
    @RequestScoped
    public GithubService.TokenBuilder provideGitHubTokenBuilder() {
        throw new NotAuthorizedException("User token must be received via request query parameter.");
    }
}
