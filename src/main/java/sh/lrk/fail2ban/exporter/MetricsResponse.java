package sh.lrk.fail2ban.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahst.IResponse;
import sh.lrk.yahst.Request;
import sh.lrk.yahst.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricsResponse implements IResponse {

    private static final Logger log = LoggerFactory.getLogger(MetricsResponse.class);

    private static final Pattern jailPattern = Pattern.compile(".+Jail list:\\s+(.+)");

    private static final Pattern currentlyFailedPattern = Pattern.compile(".+(Currently failed:)\\s+(\\d*)");
    private static final Pattern totalFailedPattern = Pattern.compile(".+(Total failed:)\\s+(\\d*)");
    private static final Pattern currentlyBannedPattern = Pattern.compile(".+(Currently banned:)\\s+(\\d*)");
    private static final Pattern totalBannedPattern = Pattern.compile(".+(Total banned:)\\s+(\\d*)");
    private final Config config;

    private long startTime;
    private boolean scrapeError = false;

    MetricsResponse(Config config) {
        this.config = config;
    }

    @Override
    public Response getResponse(Request req) {
        startTime = System.currentTimeMillis();
        log.info("Starting scrape...");
        ArrayList<Metric> metrics = new ArrayList<>();
        try {
            List<String> jails = getJails();

            metrics.add(new Metric<>("fail2ban_failed_current", "Number of currently failed connections.", "gauge", "jail", jails, this::getCurrentlyFailed));
            metrics.add(new Metric<>("fail2ban_failed_current", "Total number of failed connections.", "gauge", "jail", jails, this::getTotalFailed));
            metrics.add(new Metric<>("fail2ban_banned_current", "Number of currently banned IP addresses.", "gauge", "jail", jails, this::getCurrentlyBanned));
            metrics.add(new Metric<>("fail2ban_banned_total", "Total number of banned IP addresses.", "gauge", "jail", jails, this::getTotalBanned));
        } catch (IOException | InterruptedException e) {
            log.error("Error while getting jails!", e);
            scrapeError = true;
        }
        metrics.add(new Metric<>("fail2ban_scrape_error", "One if there was an error while scraping.", "gauge", (scrapeError) ? 1 : 0));
        metrics.add(new Metric<>("fail2ban_scrape_duration", "Scrape duration in milliseconds.", "gauge", System.currentTimeMillis() - startTime));

        StringBuilder responseBuilder = new StringBuilder();
        metrics.forEach(metric -> responseBuilder.append(metric.toString()));
        return new Response(responseBuilder.toString(), Response.Status.OK);
    }

    private List<String> getJails() throws IOException, InterruptedException {
        String out = runCommand("fail2ban-client status");
        Matcher matcher = jailPattern.matcher(out);
        if (matcher.find()) {
            return Arrays.asList(matcher.group(1).split(", "));
        } else {
            return Collections.emptyList();
        }
    }

    private Integer getTotalBanned(String jail) {
        return fetchIntegerMetric(totalBannedPattern, jail);
    }

    private Integer getCurrentlyBanned(String jail) {
        return fetchIntegerMetric(currentlyBannedPattern, jail);
    }

    private Integer getTotalFailed(String jail) {
        return fetchIntegerMetric(totalFailedPattern, jail);
    }

    private Integer getCurrentlyFailed(String jail) {
        return fetchIntegerMetric(currentlyFailedPattern, jail);
    }

    private Integer fetchIntegerMetric(final Pattern pattern, String jail) {
        try {
            String out = runCommand("fail2ban-client status " + jail);
            final Matcher matcher = pattern.matcher(out);
            if(matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            } else {
                return -1;
            }
        } catch (IOException | InterruptedException | NumberFormatException | IllegalStateException e) {
            log.error("Error while getting metrics!", e);
            scrapeError = true;
            return null;
        }
    }

    private String runCommand(String command) throws InterruptedException, IOException {
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(config.isUseSudo() ? "sudo " + command : command);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

        StringBuilder out = new StringBuilder();

        String line;
        while ((line = buf.readLine()) != null) {
            out.append(line).append("\n");
        }
        return out.toString();
    }
}
