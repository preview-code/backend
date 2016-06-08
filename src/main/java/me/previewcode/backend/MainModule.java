package me.previewcode.backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import me.previewcode.backend.api.filter.GitHubAccessTokenFilter;
import me.previewcode.backend.api.v1.GithubAPI;
import me.previewcode.backend.api.v1.PullRequestAPI;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import org.kohsuke.github.GitHub;

import javax.ws.rs.NotAuthorizedException;

public class MainModule extends ServletModule {

    @Override
    public void configureServlets() {
        this.install(new JaxrsModule());
        this.bind(GitHubAccessTokenFilter.class);
        this.bind(GithubAPI.class);
        this.bind(PullRequestAPI.class);

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

    @Provides
    @Named("github.user")
    @RequestScoped
    public GitHub provideGitHubConnection() {
        throw new NotAuthorizedException("user id must be manually seeded");
    }
}
