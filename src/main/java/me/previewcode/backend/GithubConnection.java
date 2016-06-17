package me.previewcode.backend;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.kohsuke.github.GitHub;

/**
 * An abstract class that connects with github
 *
 */
public abstract class GithubConnection {

    /**
     * The GitHub provider.
     */
    protected Provider<GitHub> githubProvider;

    /**
     * Making a connection with GitHub
     * 
     * @param gitHubProvider
     *            The provider for GitHub data
     * @throws IOException
     * 
     */
    @Inject
    protected GithubConnection(@Named("github.user") final Provider<GitHub> gitHubProvider)
            throws IOException {
        githubProvider = gitHubProvider;
    }
}
