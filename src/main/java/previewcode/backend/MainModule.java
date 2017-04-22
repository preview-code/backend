package previewcode.backend;

import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.jboss.resteasy.util.Base64;
import org.kohsuke.github.GitHub;
import previewcode.backend.api.exceptionmapper.IllegalArgumentExceptionMapper;
import previewcode.backend.api.filter.GitHubAccessTokenFilter;
import previewcode.backend.api.v1.AssigneesAPI;
import previewcode.backend.api.v1.CommentsAPI;
import previewcode.backend.api.v1.PullRequestAPI;
import previewcode.backend.api.v1.StatusAPI;
import previewcode.backend.api.v1.TrackerAPI;
import previewcode.backend.api.v1.WebhookAPI;
import previewcode.backend.services.GithubService;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.NotAuthorizedException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * The main module of the backend
 * @author PReview-Code
 *
 */
public class MainModule extends ServletModule {

    /**
     * The method that configures the servlets
     */
    @Override
    public void configureServlets() {
        this.install(new JaxrsModule());
        this.bind(GitHubAccessTokenFilter.class);
        this.bind(StatusAPI.class);
        this.bind(PullRequestAPI.class);
        this.bind(CommentsAPI.class);
        this.bind(AssigneesAPI.class);
        this.bind(TrackerAPI.class);
        this.bind(IllegalArgumentExceptionMapper.class);
        this.bind(WebhookAPI.class);

        try {
            FileInputStream file = new FileInputStream("src/main/resources/firebase-auth.json");
            // Initialize the app with a service account, granting admin privileges
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(file)
                    .setDatabaseUrl("https://preview-code.firebaseio.com/").build();
            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String RSA_PRIVATE_KEY;

    /**
     * Provides the signing algorithm to sign JWT keys destined for authenticating
     * with GitHub Integrations.
     */
    @Provides
    public Algorithm provideJWTSigningAlgo() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (RSA_PRIVATE_KEY == null) {
            URL url = Resources.getResource("integration.test-key.pem");
            RSA_PRIVATE_KEY = Resources.toString(url, Charsets.UTF_8)
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("\n", "");
        }

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(RSA_PRIVATE_KEY));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return Algorithm.RSA256((RSAPrivateKey) kf.generatePrivate(keySpec));
    }

    private static SecretKeySpec GITHUB_WEBHOOK_SECRET;
    @Provides
    @Named("github.webhook.secret")
    public SecretKeySpec provideGitHubWebhookSecret() throws IOException {
        if (GITHUB_WEBHOOK_SECRET == null) {
            URL url = Resources.getResource("github-webhook-test-secret.txt");
            final String secret = Resources.toString(url, Charsets.UTF_8);
            GITHUB_WEBHOOK_SECRET = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        }
        return GITHUB_WEBHOOK_SECRET;
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
     * Method to declare Named key "github.installation.token" to obtain the current GitHub Installation token
     * @throws Exception if key was not set
     */
    @Provides
    @Named("github.installation.token")
    @RequestScoped
    public String provideGitHubInstallationToken() {
        throw new NotAuthorizedException("Installation token must be received via an authorization call to the GitHub API.");
    }

    /**
     * Method to declare Named key "github.user.token" to obtain the current GitHub user OAuth token
     * @throws Exception if token was not set
     */
    @Provides
    @Named("github.user.token")
    @RequestScoped
    public String provideGitHubUserToken() {
        throw new NotAuthorizedException("User token must be received via request query parameter.");
    }

    /**
     * Method to declare Named key "github.token.builder" to ammend a OKHTTP Request with authorization info.
     * @throws Exception if not set via GitHubAccessTokenFilter.
     */
    @Provides
    @Named("github.token.builder")
    @RequestScoped
    public GithubService.TokenBuilder provideGitHubTokenBuilder() {
        throw new NotAuthorizedException("User token must be received via request query parameter.");
    }
}
