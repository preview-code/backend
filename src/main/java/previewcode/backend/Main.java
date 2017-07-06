package previewcode.backend;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        Config config = null;
        try {
            config = Config.loadConfiguration();
        } catch (Exception e) {
            logger.error("Unable to load config file: ", e);
            System.exit(-1);
        }

        // jetty.xml config
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setIdleTimeout(60000);
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(500);
        threadPool.setDetailedDump(false);

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setHeaderCacheSize(1024);
        http_config.setSendServerVersion(false);
        http_config.setSendDateHeader(false);

        Server server = new Server(threadPool);

        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);
        server.setStopTimeout(5000);


        // jetty-http.xml config
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setHost("localhost");

        http.setPort(config.port);
        http.setIdleTimeout(30000);
        server.addConnector(http);


        // Configure RESTEasy and Guice
        ServletContextHandler servletHandler = new ServletContextHandler();
        Injector injector = Guice.createInjector(new MainModule(config));
        servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        servletHandler.addFilter(new FilterHolder(injector.getInstance(GuiceFilter.class)), "/*", null);
        servletHandler.addServlet(sh, "/*");

        server.setHandler(servletHandler);
        server.start();
        server.join();
    }
}
