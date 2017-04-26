package previewcode.backend;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

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

        String portConfig = System.getenv("PORT");
        if (Strings.isNullOrEmpty(portConfig)) {
            logger.error("PORT env variable missing.");
            System.exit(-1);
        }

        http.setPort(Integer.parseInt(portConfig));
        http.setIdleTimeout(30000);
        server.addConnector(http);


        // Configure RESTEasy and Guice
        ServletContextHandler servletHandler = new ServletContextHandler();
        Injector injector = Guice.createInjector(new MainModule());
        servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

        ServletHolder sh = new ServletHolder(HttpServletDispatcher.class);
        servletHandler.addFilter(new FilterHolder(injector.getInstance(GuiceFilter.class)), "/*", null);
        servletHandler.addServlet(sh, "/*");

        server.setHandler(servletHandler);
        server.start();
        server.join();
    }
}
