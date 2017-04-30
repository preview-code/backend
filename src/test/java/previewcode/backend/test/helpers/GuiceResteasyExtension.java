package previewcode.backend.test.helpers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.vavr.control.Try;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.jupiter.api.extension.*;

import javax.ws.rs.client.WebTarget;

public class GuiceResteasyExtension extends AnnotatedClassInstantiator<ServletModule> implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private Server server;
    private ResteasyClient client;

    @Override
    public void beforeAll(ContainerExtensionContext containerExtensionContext) throws Exception {

        ServletModule guiceModule = getServletModule(containerExtensionContext);

        client = new ResteasyClientBuilder().build();

        server = new Server();
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory());
        httpConnector.setHost("localhost");
        httpConnector.setPort(0);
        httpConnector.setIdleTimeout(5000);
        server.addConnector(httpConnector);

        ServletContextHandler servletHandler = new ServletContextHandler();
        Injector injector = Guice.createInjector(guiceModule);
        servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        servletHandler.addFilter(new FilterHolder(injector.getInstance(GuiceFilter.class)), "/*", null);
        servletHandler.addServlet(sh, "/*");

        server.setHandler(servletHandler);
        server.start();
    }

    private ServletModule getServletModule(ContainerExtensionContext context) {
        return Try.of(() -> context.getTestClass().get())
                .flatMap(cls -> instantiateAnnotationValue(ApiEndPointTest.class, cls))
                .get();
    }

    private Try<Class<? extends ServletModule>> getAnnotatedModule(Class<?> testClass) {
        if (testClass.isAnnotationPresent(ApiEndPointTest.class))
            return Try.success(testClass.getAnnotation(ApiEndPointTest.class).value());
        else
            return Try.failure(new RuntimeException("GuiceResteasyExtension can only be used via the ApiEndPointTest annotation."));
    }


    @Override
    public void afterAll(ContainerExtensionContext containerExtensionContext) throws Exception {
        if (client != null && server != null) {
            client.close();
            server.stop();
        }
    }

    @Override
    public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return WebTarget.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return client.target(server.getURI());
    }
}

