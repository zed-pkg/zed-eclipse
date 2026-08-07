package tech.zpkg.eclipse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ZedWorkspaceModelTest {
    @Test
    void projectsPackagesAndProblemsMarkersAcrossWorkspaceRoots() {
        var warning = new ZedInspector.Issue(
                "lock.stale", "warning", "Stale lock", "lock is stale", List.of(".zpkg.lock"),
                List.of(new ZedInspector.Action("install", "Install", "command", "zed", List.of("install"), true))
        );
        var error = new ZedInspector.Issue("manifest.invalid", "error", "Invalid manifest", "bad toml", List.of(), List.of());
        var snapshot = ZedWorkspaceModel.project(List.of(
                new ZedInspector.Report(1, "zeta", null, List.of(warning)),
                new ZedInspector.Report(1, "alpha", null, List.of(error))
        ));
        assertEquals(2, snapshot.packages().size());
        assertTrue(snapshot.packages().get(0).root().endsWith("alpha"));
        assertEquals(1, snapshot.packages().get(0).errors());
        assertEquals(1, snapshot.packages().get(1).warnings());
        assertEquals(2, snapshot.markers().size());
        assertTrue(snapshot.markers().stream().anyMatch(marker -> marker.resource().endsWith(".zpkg.toml")));
        assertTrue(snapshot.markers().stream().anyMatch(marker -> marker.resource().endsWith(".zpkg.lock")));
    }

    @Test
    void previewsOnlyConfirmationGatedQuickFixCommands() {
        var action = new ZedInspector.Action("install", "Install", "command", "zed", List.of("install"), true);
        var preview = ZedWorkspaceModel.preview(action, Path.of("workspace"));
        assertEquals("zed", preview.executable());
        assertEquals(List.of("install"), preview.arguments());
        assertTrue(preview.requiresConfirmation());
        assertThrows(IllegalArgumentException.class, () -> ZedWorkspaceModel.preview(
                new ZedInspector.Action("bad", "Bad", "command", "zed", List.of("install"), false), Path.of("workspace")
        ));
    }
}
