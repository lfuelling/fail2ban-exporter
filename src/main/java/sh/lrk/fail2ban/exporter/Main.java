package sh.lrk.fail2ban.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahst.Method;
import sh.lrk.yahst.Response;
import sh.lrk.yahst.Routes;
import sh.lrk.yahst.Server;

public class Main {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static Config config;

    public static void main(String[] args) {

        log.info("Loading config...");
        config = new Config();

        log.info("Generating routes...");
        Routes routes = new Routes();
        if (!config.getMetricsPath().equals("/")) {
            // add index route if necessary
            routes.add(Method.GET, "/", req -> new Response(getIndexBody(), Response.Status.OK));
        }
        routes.add(Method.GET, config.getMetricsPath(), new MetricsResponse(config));

        Server.start(routes, config.getPort(), 1_000_000);

    }

    private static String getIndexBody() {
        return "<html><head><title>Fail2Ban Exporter</title></head><body><h1>Fail2Ban Exporter</h1>" +
                "<p>Metrics are available at: <a href=\"" + config.getMetricsPath() + "\">" +
                config.getMetricsPath() + "</a></body>";
    }
}
