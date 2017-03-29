package me.previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.kohsuke.github.GHIssueComment;

import me.previewcode.backend.DTO.PRComment;
import me.previewcode.backend.DTO.PRGroupComment;
import me.previewcode.backend.DTO.PRLineComment;
import me.previewcode.backend.services.FirebaseService;
import me.previewcode.backend.services.GithubService;

import com.google.inject.Inject;

/**
 * API endpoint for comments
 *
 */
@Path("{owner}/{name}/pulls/{number}/comments/")
public class CommentsAPI {

    @Inject
    private GithubService commentsService;
    
    @Inject
    private FirebaseService firebaseService;

    /**
     * Sets the standard pull request comments
     * 
     * @param owner
     *            The owner of the repository where the comment is placed
     * @param name
     *            The name of the repository where the comment is placed
     * @param number
     *            The number of the pull request
     * @param comment
     *            The comment that is placed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("pr")
    public void postPRComment(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("number") int number,
            PRComment comment) {
        commentsService.postComment(owner, name, number, comment);
    }

    /**
     * Sets the the group comments
     * 
     * @param owner
     *            The owner of the repository where the comment is placed
     * @param name
     *            The name of the repository where the comment is placed
     * @param number
     *            The number of the pull request
     * @param comment
     *            The comment that is placed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("group")
    public void postGroupComment(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("number") int number,
            PRGroupComment comment) {
        GHIssueComment newComment = commentsService.postComment(owner, name, number, comment);
        firebaseService.setComments(owner, name, number, newComment.getId(),
                comment.groupID);
    }

    /**
     * Sets the the line comments
     *
     * @param owner
     *            The owner of the repository where the comment is placed
     * @param name
     *            The name of the repository where the comment is placed
     * @param number
     *            The number of the pull request
     * @param comment
     *            The comment that is placed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("line")
    public void postLineComment(@PathParam("owner") String owner,
                                              @PathParam("name") String name, @PathParam("number") int number,
                                              PRLineComment comment) {
        commentsService.postLineComment(owner, name, number, comment);
    }
}
