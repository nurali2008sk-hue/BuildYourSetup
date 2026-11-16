
package kz.setuplab.facade;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.repository.JsonBuildRepository;
import kz.setuplab.repository.BuildServiceRepository;
import kz.setuplab.repository.BuildServiceRepository.ServiceSelection;
import kz.setuplab.service.BuildRecommendationService;
import kz.setuplab.service.CompatibilityService;
import kz.setuplab.strategy.BuildSelectionStrategy;

import kz.setuplab.observer.BuildEvent;
import kz.setuplab.observer.BuildEventType;
import kz.setuplab.observer.BuildEventPublisher;
import kz.setuplab.observer.BuildObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SetupFacade {

    private final CompatibilityService compatibilityService;
    private final BuildRecommendationService recommendationService;
    private final JsonBuildRepository repository;
    private final BuildServiceRepository serviceRepository;

    private final BuildEventPublisher publisher =
            new BuildEventPublisher();

    public SetupFacade(ComponentCatalog catalog) {
        this(catalog, new JsonBuildRepository(Path.of("data", "builds.json")));
    }

    public SetupFacade(ComponentCatalog catalog, JsonBuildRepository repository) {
        if (catalog == null || repository == null) {
            throw new IllegalArgumentException("Catalog and repository cannot be null.");
        }

        compatibilityService = new CompatibilityService();
        recommendationService = new BuildRecommendationService(catalog);
        this.repository = repository;
        serviceRepository = new BuildServiceRepository(
                repository.getFilePath().resolveSibling("build-services.json")
        );
    }

    // Observer registration
    public void addObserver(BuildObserver observer) {
        publisher.addObserver(observer);
    }

    public void removeObserver(BuildObserver observer) {
        publisher.removeObserver(observer);
    }

    // Create a new build using Builder
    public PcBuild createBuild(
            String name,
            long budget,
            List<PcComponent> components
    ) {
        validateBudget(budget);

        if (components == null) {
            throw new IllegalArgumentException(
                    "Select components."
            );
        }

        PcBuild.Builder builder =
                new PcBuild.Builder().name(name);

        for (PcComponent component : components) {
            builder.component(component);
        }

        PcBuild build = builder.build();

        validateBuild(build, budget);

        return build;
    }

    // Recommend build using Strategy
    public PcBuild recommendBuild(
            String name,
            long budget,
            BuildSelectionStrategy strategy
    ) {
        validateBudget(budget);

        PcBuild build = recommendationService.recommend(
                name,
                budget,
                strategy
        );

        validateBuild(build, budget);

        return build;
    }

    public ServiceSelection getBuildServices(String buildId) throws IOException {
        if (buildId == null || buildId.isBlank()) {
            throw new IllegalArgumentException("Invalid build ID.");
        }
        return serviceRepository.findByBuildId(buildId);
    }

    public void saveBuild(PcBuild build) throws IOException {
        if (build == null) {
            throw new IllegalArgumentException("Create a build first.");
        }
        saveBuild(build, getBuildServices(build.getId()));
    }

    public void saveBuild(PcBuild build, ServiceSelection services)
            throws IOException {
        if (build == null || services == null) {
            throw new IllegalArgumentException("Build and services are required.");
        }

        validateBuild(build, Long.MAX_VALUE);

        boolean exists = repository.findAll().stream()
                .anyMatch(saved -> saved.getId().equals(build.getId()));

        boolean hadServices = serviceRepository.containsBuildId(build.getId());
        ServiceSelection previous = getBuildServices(build.getId());

        serviceRepository.save(build.getId(), services);

        try {
            repository.save(build);
        } catch (IOException exception) {
            restoreServices(build.getId(), hadServices, previous, exception);
            throw exception;
        }

        publisher.notifyObservers(new BuildEvent(
                exists ? BuildEventType.UPDATED : BuildEventType.CREATED,
                build.getId()
        ));
    }

    private void restoreServices(
            String buildId,
            boolean hadServices,
            ServiceSelection previous,
            IOException originalError
    ) {
        try {
            if (hadServices) {
                serviceRepository.save(buildId, previous);
            } else {
                serviceRepository.delete(buildId);
            }
        } catch (IOException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    // Get all saved builds
    public List<PcBuild> getSavedBuilds() throws IOException {
        return repository.findAll();
    }

    // Update existing build
    public PcBuild updateBuild(
            String buildId,
            PcComponent newComponent
    ) throws IOException {

        if (buildId == null || buildId.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid build ID."
            );
        }

        if (newComponent == null) {
            throw new IllegalArgumentException(
                    "Select a component."
            );
        }

        // Find existing build
        PcBuild oldBuild = repository.findAll()
                .stream()
                .filter(build ->
                        build.getId().equals(buildId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Build not found."
                        )
                );

        // Keep the original ID
        PcBuild.Builder builder = new PcBuild.Builder()
                .id(oldBuild.getId())
                .name(oldBuild.getName());

        // Replace selected component
        for (PcComponent component : oldBuild.getComponents()) {

            if (component.type() == newComponent.type()) {
                builder.component(newComponent);
            } else {
                builder.component(component);
            }
        }

        PcBuild updatedBuild = builder.build();

        // Check compatibility
        validateBuild(updatedBuild, Long.MAX_VALUE);

        // Save updated build
        repository.save(updatedBuild);

        // Notify observers
        publisher.notifyObservers(
                new BuildEvent(
                        BuildEventType.UPDATED,
                        updatedBuild.getId()
                )
        );

        return updatedBuild;
    }

    public boolean deleteBuild(String id) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Select a saved build.");
        }

        boolean hadServices = serviceRepository.containsBuildId(id);
        ServiceSelection previous = getBuildServices(id);

        serviceRepository.delete(id);

        boolean deleted;
        try {
            deleted = repository.deleteById(id);
        } catch (IOException exception) {
            restoreServices(id, hadServices, previous, exception);
            throw exception;
        }

        if (deleted) {
            publisher.notifyObservers(new BuildEvent(BuildEventType.DELETED, id));
        }

        return deleted;
    }

    // Validate budget
    private void validateBudget(long budget) {

        if (budget <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }
    }

    // Validate compatibility and price
    private void validateBuild(PcBuild build, long budget) {

        List<String> errors =
                compatibilityService.validate(build);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    String.join("\n", errors)
            );
        }

        if (build.getTotalPrice() > budget) {
            throw new IllegalArgumentException(
                    "Build exceeds your budget by "
                            + (build.getTotalPrice() - budget)
                            + " KZT."
            );
        }
    }
}
