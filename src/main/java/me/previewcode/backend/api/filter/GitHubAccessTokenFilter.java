package me.previewcode.backend.api.filter;

import com.google.common.base.Strings;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.kohsuke.github.GitHub;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class GitHubAccessTokenFilter implements ContainerRequestFilter {

    private static final String TOKEN_PARAMETER = "access_token";

    private static final String CURRENT_USER_NAME = "github.user";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        final MultivaluedMap<String, String> parameters = containerRequestContext.getUriInfo()
                .getQueryParameters();
        final String token = parameters.getFirst(TOKEN_PARAMETER);

        if (!Strings.isNullOrEmpty(token)) {
            try {
                final GitHub user = GitHub.connectUsingOAuth(token);
                containerRequestContext.setProperty(
                        Key.get(GitHub.class, Names.named(CURRENT_USER_NAME)).toString(), user);
            } catch (final NotAuthorizedException e) {
                Response response = Response.status(Response.Status.FORBIDDEN)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(e)
                        .build();
                containerRequestContext.abortWith(response);
            }
        }
    }
}
