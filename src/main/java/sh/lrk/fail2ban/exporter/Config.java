package sh.lrk.fail2ban.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to read the configuration file.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
class Config {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private int port = 8080; // default value
    private String metricsPath = "/metrics"; // default value
    private boolean useSudo = false;

    Config() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            Properties properties = new Properties();
            properties.load(in);

            port = Integer.parseInt(properties.getProperty("server.port"));
            metricsPath = properties.getProperty("server.metrics.path");
            useSudo = Boolean.parseBoolean(properties.getProperty("server.metrics.sudo"));

        } catch (NumberFormatException e) {
            log.warn("Unable to parse config, using defaults!", e);
        } catch (FileNotFoundException e) {
            log.warn("Unable to find config, using defaults!", e);
        } catch (IOException e) {
            log.warn("Unable to read config, using defaults!", e);
        }
    }

    int getPort() {
        return port;
    }

    public String getMetricsPath() {
        return metricsPath;
    }

    public boolean isUseSudo() {
        return useSudo;
    }
}

