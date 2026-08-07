package tech.zpkg.eclipse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ZedWorkspaceModel {
    public record PackageNode(String root, long errors, long warnings, List<ZedInspector.Issue> issues) {}
    public record Marker(String workspaceRoot, String resource, String severity, String issueId, String message) {}
    public record ActionPreview(String executable, List<String> arguments, String workingDirectory, boolean requiresConfirmation) {}
    public record Snapshot(List<PackageNode> packages, List<Marker> markers) {}

    private ZedWorkspaceModel() {}

    public static Snapshot project(List<ZedInspector.Report> reports) {
        var packages = new ArrayList<PackageNode>();
        var markers = new ArrayList<Marker>();
        for (var report : reports == null ? List.<ZedInspector.Report>of() : reports) {
            var root = Path.of(report.workspaceRoot()).toAbsolutePath().normalize();
            var issues = report.issues() == null ? List.<ZedInspector.Issue>of() : report.issues();
            long errors = issues.stream().filter(issue -> "error".equalsIgnoreCase(issue.severity())).count();
            long warnings = issues.stream().filter(issue -> "warning".equalsIgnoreCase(issue.severity())).count();
            packages.add(new PackageNode(root.toString(), errors, warnings, List.copyOf(issues)));
            for (var issue : issues) {
                var files = issue.files() == null || issue.files().isEmpty() ? List.of(".zpkg.toml") : issue.files();
                for (var file : files) {
                    var resourcePath = Path.of(file);
                    var resource = resourcePath.isAbsolute() ? resourcePath.normalize() : root.resolve(resourcePath).normalize();
                    markers.add(new Marker(root.toString(), resource.toString(), issue.severity(), issue.id(), issue.title() + ": " + issue.detail()));
                }
            }
        }
        packages.sort(Comparator.comparing(PackageNode::root));
        markers.sort(Comparator.comparing(Marker::resource).thenComparing(Marker::issueId));
        return new Snapshot(List.copyOf(packages), List.copyOf(markers));
    }

    public static ActionPreview preview(ZedInspector.Action action, Path root) {
        if (!"command".equals(action.kind())) throw new IllegalArgumentException("only command actions have an execution preview");
        if (!action.requiresConfirmation()) throw new IllegalArgumentException("command actions must require explicit confirmation");
        if (action.command() == null || action.command().isBlank()) throw new IllegalArgumentException("command executable must not be blank");
        return new ActionPreview(
                action.command(),
                List.copyOf(action.arguments() == null ? List.of() : action.arguments()),
                root.toAbsolutePath().normalize().toString(),
                true
        );
    }
}
