package previewcode.backend.api.filter;

import org.jboss.resteasy.core.interception.jaxrs.PreMatchContainerRequestContext;
import previewcode.backend.services.interpreters.RequestContextActionInterpreter;
import previewcode.backend.services.interpreters.GitHubAuthInterpreter;
import previewcode.backend.services.GithubService;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
@Singleton
public class GitHubAccessTokenFilter implements ContainerRequestFilter {

    @Inject
    private GitHubAuthInterpreter gitHubAuthInterpreter;

    @Inject
    private GithubService.V2 authService;

    public GitHubAccessTokenFilter() {
        super();
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        PreMatchContainerRequestContext context = (PreMatchContainerRequestContext) ctx;

        gitHubAuthInterpreter.context = context;

        RequestContextActionInterpreter contextActionInterpreter =
                new RequestContextActionInterpreter(context.getHttpRequest());

        new Interpreter(gitHubAuthInterpreter, contextActionInterpreter)
                .unsafeEvaluate(authService.authenticate());
    }
}
