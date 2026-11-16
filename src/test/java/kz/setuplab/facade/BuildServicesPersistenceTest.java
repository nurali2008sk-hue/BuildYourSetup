package kz.setuplab.facade;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.observer.BuildEventType;
import kz.setuplab.repository.BuildServiceRepository;
import kz.setuplab.repository.BuildServiceRepository.ServiceSelection;
import kz.setuplab.repository.JsonBuildRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildServicesPersistenceTest {
    @TempDir
    Path directory;

    private PcBuild sampleBuild() {
        PcBuild.Builder builder = new PcBuild.Builder().name("Test PC");
        EnumSet<ComponentType> selected = EnumSet.noneOf(ComponentType.class);
        for (var component : new ComponentCatalog().getAll()) {
            if (selected.add(component.type())) {
                builder.component(component);
            }
        }
        return builder.build();
    }

    @Test
    void observerReadsServicesAfterTheyHaveBeenSaved() throws IOException {
        SetupFacade facade = new SetupFacade(new ComponentCatalog(),
                new JsonBuildRepository(directory.resolve("builds.json")));
        ServiceSelection expected = new ServiceSelection(true, true, false);
        List<ServiceSelection> observed = new ArrayList<>();
        facade.addObserver(event -> {
            try {
                observed.add(facade.getBuildServices(event.buildId()));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });

        PcBuild build = sampleBuild();
        facade.saveBuild(build, expected);

        assertEquals(List.of(expected), observed);
        SetupFacade reloaded = new SetupFacade(new ComponentCatalog(),
                new JsonBuildRepository(directory.resolve("builds.json")));
        assertEquals(expected, reloaded.getBuildServices(build.getId()));
        assertEquals(build.getId(), reloaded.getSavedBuilds().getFirst().getId());
    }

    @Test
    void failedNewBuildSaveRemovesNewServiceEntry() throws IOException {
        FailingRepository repository = new FailingRepository(directory.resolve("builds.json"));
        SetupFacade facade = new SetupFacade(new ComponentCatalog(), repository);
        List<BuildEventType> events = new ArrayList<>();
        facade.addObserver(event -> events.add(event.type()));
        PcBuild build = sampleBuild();
        repository.failSave = true;

        assertThrows(IOException.class, () -> facade.saveBuild(
                build, new ServiceSelection(true, false, true)));

        assertTrue(facade.getSavedBuilds().isEmpty());
        assertFalse(new BuildServiceRepository(directory.resolve("build-services.json"))
                .containsBuildId(build.getId()));
        assertTrue(events.isEmpty());
    }

    @Test
    void failedUpdateRestoresPreviousServices() throws IOException {
        FailingRepository repository = new FailingRepository(directory.resolve("builds.json"));
        SetupFacade facade = new SetupFacade(new ComponentCatalog(), repository);
        PcBuild build = sampleBuild();
        ServiceSelection original = new ServiceSelection(true, false, false);
        facade.saveBuild(build, original);
        List<BuildEventType> events = new ArrayList<>();
        facade.addObserver(event -> events.add(event.type()));
        repository.failSave = true;

        assertThrows(IOException.class, () -> facade.saveBuild(
                build, new ServiceSelection(false, true, true)));

        assertEquals(original, facade.getBuildServices(build.getId()));
        assertEquals(1, facade.getSavedBuilds().size());
        assertTrue(events.isEmpty());
    }

    @Test
    void deletionRemovesServicesBeforeNotifying() throws IOException {
        SetupFacade facade = new SetupFacade(new ComponentCatalog(),
                new JsonBuildRepository(directory.resolve("builds.json")));
        PcBuild build = sampleBuild();
        facade.saveBuild(build, new ServiceSelection(true, true, true));
        List<ServiceSelection> observed = new ArrayList<>();
        facade.addObserver(event -> {
            if (event.type() == BuildEventType.DELETED) {
                try {
                    observed.add(facade.getBuildServices(event.buildId()));
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            }
        });

        assertTrue(facade.deleteBuild(build.getId()));

        assertTrue(facade.getSavedBuilds().isEmpty());
        assertEquals(List.of(new ServiceSelection(false, false, false)), observed);
        assertFalse(new BuildServiceRepository(directory.resolve("build-services.json"))
                .containsBuildId(build.getId()));
    }

    @Test
    void invalidServiceFileDoesNotSaveBuildOrNotify() throws IOException {
        SetupFacade facade = new SetupFacade(new ComponentCatalog(),
                new JsonBuildRepository(directory.resolve("builds.json")));
        Files.writeString(directory.resolve("build-services.json"), "invalid JSON");
        List<BuildEventType> events = new ArrayList<>();
        facade.addObserver(event -> events.add(event.type()));

        assertThrows(IOException.class, () -> facade.saveBuild(
                sampleBuild(), new ServiceSelection(true, false, false)));

        assertTrue(facade.getSavedBuilds().isEmpty());
        assertTrue(events.isEmpty());
        assertEquals("invalid JSON", Files.readString(directory.resolve("build-services.json")));
    }

    private static class FailingRepository extends JsonBuildRepository {
        boolean failSave;

        FailingRepository(Path file) {
            super(file);
        }

        @Override
        public void save(PcBuild build) throws IOException {
            if (failSave) {
                throw new IOException("Simulated build write failure");
            }
            super.save(build);
        }
    }
}
