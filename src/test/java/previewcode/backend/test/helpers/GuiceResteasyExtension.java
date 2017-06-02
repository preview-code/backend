package previewcode.backend.test.helpers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.vavr.Tuple2;
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

public class GuiceResteasyExtension extends TestStore<Tuple2<ResteasyClient, Server>> implements BeforeAllCallback, AfterAllCallback, ParameterResolver {


    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception {

        ServletModule guiceModule = getServletModule(context);

        Server server = new Server();
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

        putObjectToStore(context, new Tuple2<>(new ResteasyClientBuilder().build(), server));
    }

    private ServletModule getServletModule(ContainerExtensionContext context) {
        return Try.of(() -> context.getTestClass().get())
                .flatMap(cls -> instantiateAnnotationValue(ApiEndPointTest.class, cls))
                .get();
    }


    @Override
    public void afterAll(ContainerExtensionContext context) throws Exception {
        Tuple2<ResteasyClient, Server> t = getFromStore(context);
        ResteasyClient client = t._1;
        Server server = t._2;
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
        Tuple2<ResteasyClient, Server> t = getFromStore(extensionContext.getParent().get());
        ResteasyClient client = t._1;
        Server server = t._2;
        return client.target(server.getURI());
    }

}

