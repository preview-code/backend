package me.previewcode.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

import me.previewcode.backend.api.filter.GitHubAccessTokenFilter;
import me.previewcode.backend.api.v1.AssigneesAPI;
import me.previewcode.backend.api.v1.CommentsAPI;
import me.previewcode.backend.api.v1.StatusAPI;
import me.previewcode.backend.api.v1.PullRequestAPI;

import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.kohsuke.github.GitHub;

import javax.ws.rs.NotAuthorizedException;

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

    /**
     * Method to declare Named key "github.user" to obtain the current GitHub instance
     * @Throws an exception if key was not set
     */
    @Provides
    @Named("github.user")
    @RequestScoped
    public GitHub provideGitHubConnection() {
        throw new NotAuthorizedException("user id must be manually seeded");
    }
}
