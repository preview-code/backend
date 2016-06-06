package me.previewcode.backend;

import java.io.IOException;

import org.kohsuke.github.GitHub;

public abstract class GithubConnection {

    protected GitHub github;

    public GithubConnection() throws IOException {

        // this needs to be GitHub.connect(OATHToken);
        github = GitHub.connect();
    }
}
