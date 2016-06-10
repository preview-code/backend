package me.previewcode.backend;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.kohsuke.github.GitHub;

public abstract class GithubConnection {

    protected Provider<GitHub> githubProvider;

    @Inject
    protected GithubConnection(@Named("github.user") final Provider<GitHub> gitHubProvider) throws IOException {
        githubProvider = gitHubProvider;
    }
}
