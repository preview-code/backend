package me.previewcode.backend.api.v1;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PrNumber;
import me.previewcode.backend.DTO.StatusBody;

import me.previewcode.backend.GithubConnection;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

@Path("/project/{owner}/{name}")
public class GithubAPI extends GithubConnection {
    private GHRepository repo;

    @Inject
    public GithubAPI(@Named("github.user") Provider<GitHub> gitHubProvider) throws IOException {
        super(gitHubProvider);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createPR/{head}/{base}")
    public PrNumber createPR(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("head") String head,
            @PathParam("base") String base, PRbody body) throws IOException {
        PrNumber number = new PrNumber();
        try {
            repo = this.githubProvider.get().getRepository(owner.toLowerCase() + "/"
                    + name.toLowerCase());
            GHPullRequest pr = repo.createPullRequest(body.title, head, base,
                    body.description);
            PullRequestAPI fb = new PullRequestAPI();
            StatusBody sBody = new StatusBody();
            sBody.status = "No reviewer assigned";
            fb.getStatus(repo.getOwnerName(), repo.getName(),
                    Integer.toString(pr.getNumber()), sBody);
            number.number = pr.getNumber();
            return number;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
