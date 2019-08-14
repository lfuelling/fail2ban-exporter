package sh.lrk.fail2ban.exporter;

import java.util.List;

public class Metric<T> {
    private final String name;
    private final String help;
    private final String type;
    private final T value;
    private final String labelName;
    private final List<String> labels;
    private final Resolver<T> resolver;

    Metric(String name, String help, String type, String labelName, List<String> labels, Resolver<T> resolver) {
        this.name = name;
        this.help = help;
        this.type = type;
        this.labelName = labelName;
        this.labels = labels;
        this.resolver = resolver;
        this.value = null;
    }

    Metric(String name, String help, String type, T value) {
        this.name = name;
        this.help = help;
        this.type = type;
        this.value = value;
        this.labelName = null;
        this.labels = null;
        this.resolver = null;
    }

    @Override
    public String toString() {
        String header = "# HELP " + name + " " + help + "\n" +
                "# TYPE " + name + " " + type + "\n";

        if (value == null && labelName != null && labels != null && resolver != null) {
            StringBuilder metricsBuilder = new StringBuilder();
            labels.forEach(label -> metricsBuilder.append(name)
                    .append("{").append(labelName)
                    .append("=\"").append(label).append("\"}")
                    .append(" ").append(resolver.resolve(label))
                    .append("\n"));

            return header + metricsBuilder.toString();
        } else if (value != null) {
            return header + name + " " + value + "\n";
        } else {
            throw new IllegalStateException("No metric value or resolver was supplied!");
        }
    }

    interface Resolver<T> {
        T resolve(String label);
    }
}
