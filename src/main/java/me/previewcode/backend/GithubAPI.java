package me.previewcode.backend;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PrNumber;
import me.previewcode.backend.DTO.StatusBody;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

@Path("/project/{owner}/{name}")
public class GithubAPI extends GithubConnection {
    GHRepository repo;

    public GithubAPI() throws IOException {
        super();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createPR/{head}/{base}")
    public PrNumber createPR(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("head") String head,
            @PathParam("base") String base, PRbody body) throws IOException {
        PrNumber number = new PrNumber();
        try {
            repo = github.getRepository(owner.toLowerCase() + "/"
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
            number.number = 0;
            return number;
        }
    }
}
