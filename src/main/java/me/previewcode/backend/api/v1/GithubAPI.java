package me.previewcode.backend.api.v1;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.GithubConnection;
import me.previewcode.backend.DTO.PRComment;
import me.previewcode.backend.DTO.PRGroupComment;
import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PRresponseComment;
import me.previewcode.backend.DTO.PrNumber;
import me.previewcode.backend.DTO.StatusBody;
import me.previewcode.backend.DTO.User;

import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

@Path("{owner}/{name}/pulls/")
public class GithubAPI extends GithubConnection {
    private GHRepository repo;

    @Inject
    public GithubAPI(@Named("github.user") Provider<GitHub> gitHubProvider)
            throws IOException {
        super(gitHubProvider);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PrNumber createPR(@PathParam("owner") String owner,
            @PathParam("name") String name, PRbody body) throws IOException {
        PrNumber number = new PrNumber();
        PullRequestAPI prAPI = new PullRequestAPI();
        try {
            repo = this.githubProvider.get().getRepository(owner.toLowerCase() + "/"
                    + name.toLowerCase());
            GHPullRequest pr = repo.createPullRequest(body.title, body.head,
                    body.base, body.description);
            StatusBody sBody = new StatusBody();
            sBody.status = "No reviewer assigned";
            prAPI.setStatus(repo.getOwnerName(), repo.getName(),
             Integer.toString(pr.getNumber()), sBody);
            number.number = pr.getNumber();
            return number;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{number}/comments/pr")
    public PRresponseComment postPRComment(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("number") int number,
            PRComment comment) {
        return setResponseComment(postComment(owner, name, number, comment));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{number}/comments/group")
    public PRresponseComment postGroupComment(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("number") int number,
            PRGroupComment comment) {

        GHIssueComment newComment = postComment(owner, name, number, comment);
        PullRequestAPI pAPI = new PullRequestAPI();
        pAPI.setComments(owner, name, number, newComment.getId(), comment.id);

        return setResponseComment(newComment);
    }

    private GHIssueComment postComment(String owner, String name, int number,
            PRComment comment) {
        try {
            repo = this.githubProvider.get().getRepository(owner.toLowerCase() + "/"
                    + name.toLowerCase()); 
            GHPullRequest pr = repo.getPullRequest(number);
            return pr.comment(comment.body);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PRresponseComment setResponseComment(GHIssueComment comment) {
        PRresponseComment responseComment = new PRresponseComment();
        try {
            responseComment.body = comment.getBody();
            responseComment.created_at = comment.getCreatedAt();
            User user = new User();
            user.avatar_url = comment.getUser().getAvatarUrl();
            user.html_url = comment.getUser().getHtmlUrl();
            user.login = comment.getUser().getLogin();
            responseComment.user = user;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseComment;

    }
}
