# Fail2Ban Exporter

This is a Prometheus exporter for Fail2Ban. 

It uses `fail2ban-client` to scrape information and serves it via HTTP on port 9635.

## Usage

1. Clone this repo, `cd` into it
2. Build: `$ mvn clean install`
3. Configure: `$ vi src/main/resources/application.properties`
4. Run: `$ java -jar target/fail2ban-exporter-<version>-jar-with-dependencies.jar`

When using `server.metrics.sudo=true`, be sure to add `<user> ALL(ALL) NOPASSWD: /usr/bin/fail2ban-client` to your sudoers file.

This project is based on [this exporter](https://github.com/jangrewe/prometheus-fail2ban-exporter) which generates a `.prom` file instead of serving HTTP.