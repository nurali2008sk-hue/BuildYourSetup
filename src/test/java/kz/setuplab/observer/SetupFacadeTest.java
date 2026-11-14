
package kz.setuplab.facade;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.observer.BuildEvent;
import kz.setuplab.observer.BuildEventType;
import kz.setuplab.repository.JsonBuildRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SetupFacadeTest {

    @TempDir
    Path tempDir;

    private SetupFacade facade;
    private ComponentCatalog catalog;
    private List<BuildEvent> events;

    @BeforeEach
    void setUp() {
        catalog = new ComponentCatalog();

        JsonBuildRepository repository =
                new JsonBuildRepository(
                        tempDir.resolve("test-builds.json")
                );

        facade = new SetupFacade(catalog, repository);

        events = new ArrayList<>();
        facade.addObserver(events::add);
    }

    private PcBuild createValidBuild() {
        return new PcBuild.Builder()
                .name("Gaming PC")
                .component(find("cpu-1"))
                .component(find("mb-1"))
                .component(find("gpu-1"))
                .component(find("ram-1"))
                .component(find("ssd-1"))
                .component(find("psu-2"))
                .component(find("case-1"))
                .build();
    }

    private PcComponent find(String id) {
        return catalog.getAll().stream()
                .filter(component -> component.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void facadeSavesBuildAndNotifiesObserver()
            throws IOException {

        PcBuild build = createValidBuild();

        facade.saveBuild(build);

        assertEquals(1, facade.getSavedBuilds().size());
        assertEquals(1, events.size());
        assertEquals(
                BuildEventType.CREATED,
                events.get(0).type()
        );
        assertEquals(
                build.getId(),
                events.get(0).buildId()
        );
    }

    @Test
    void facadeUpdatesBuildWithoutChangingId()
            throws IOException {

        PcBuild original = createValidBuild();
        facade.saveBuild(original);

        events.clear();

        PcComponent newGpu = find("gpu-2");

        PcBuild updated = facade.updateBuild(
                original.getId(),
                newGpu
        );

        assertEquals(original.getId(), updated.getId());

        assertEquals(
                "gpu-2",
                updated.getComponent(ComponentType.GPU).id()
        );

        assertEquals(1, facade.getSavedBuilds().size());

        assertEquals(1, events.size());
        assertEquals(
                BuildEventType.UPDATED,
                events.get(0).type()
        );
    }

    @Test
    void facadeDeletesBuildAndNotifiesObserver()
            throws IOException {

        PcBuild build = createValidBuild();
        facade.saveBuild(build);

        events.clear();

        boolean deleted = facade.deleteBuild(build.getId());

        assertTrue(deleted);
        assertTrue(facade.getSavedBuilds().isEmpty());

        assertEquals(1, events.size());
        assertEquals(
                BuildEventType.DELETED,
                events.get(0).type()
        );
    }

    @Test
    void facadeRejectsInvalidBudget() {

        assertThrows(
                IllegalArgumentException.class,
                () -> facade.createBuild(
                        "Invalid PC",
                        0,
                        createValidBuild().getComponents()
                )
        );
    }

    @Test
    void facadeDoesNotNotifyWhenBuildNotFound()
            throws IOException {

        boolean deleted = facade.deleteBuild("unknown-id");

        assertFalse(deleted);
        assertTrue(events.isEmpty());
    }
}
