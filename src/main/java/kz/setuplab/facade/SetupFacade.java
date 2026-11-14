
package kz.setuplab.facade;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.repository.JsonBuildRepository;
import kz.setuplab.service.BuildRecommendationService;
import kz.setuplab.service.CompatibilityService;
import kz.setuplab.strategy.BuildSelectionStrategy;
import kz.setuplab.observer.BuildEventPublisher;
import kz.setuplab.observer.BuildObserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SetupFacade {

    private final CompatibilityService compatibilityService;
    private final BuildRecommendationService recommendationService;
    private final JsonBuildRepository repository;


    private final BuildEventPublisher publisher =
            new BuildEventPublisher();

    public SetupFacade(ComponentCatalog catalog) {
        compatibilityService = new CompatibilityService();
        recommendationService = new BuildRecommendationService(catalog);

        repository = new JsonBuildRepository(
                Path.of("data", "builds.json")
        );
    }


    public void addObserver(BuildObserver observer) {
        publisher.addObserver(observer);
    }


    public void removeObserver(BuildObserver observer) {
        publisher.removeObserver(observer);
    }


    public PcBuild createBuild(
            String name,
            long budget,
            List<PcComponent> components
    ) {
        validateBudget(budget);

        if (components == null) {
            throw new IllegalArgumentException("Select components.");
        }

        PcBuild.Builder builder = new PcBuild.Builder().name(name);

        for (PcComponent component : components) {
            builder.component(component);
        }

        PcBuild build = builder.build();

        validateBuild(build, budget);

        return build;
    }


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

    public void saveBuild(PcBuild build) throws IOException {
        if (build == null) {
            throw new IllegalArgumentException(
                    "Create a build first."
            );
        }

        validateBuild(build, Long.MAX_VALUE);

        repository.save(build);

        publisher.notifyObservers(build);
    }

    public List<PcBuild> getSavedBuilds() throws IOException {
        return repository.findAll();
    }

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

        PcBuild oldBuild = repository.findAll()
                .stream()
                .filter(build -> build.getId().equals(buildId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Build not found."
                        )
                );

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

        validateBuild(updatedBuild, Long.MAX_VALUE);

        repository.save(updatedBuild);

        publisher.notifyObservers(updatedBuild);

        return updatedBuild;
    }

    public boolean deleteBuild(String id) throws IOException {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Select a saved build."
            );
        }

        boolean deleted = repository.deleteById(id);

        return deleted;
    }

    private void validateBudget(long budget) {
        if (budget <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }
    }

    private void validateBuild(PcBuild build, long budget) {

        List<String> errors = compatibilityService.validate(build);

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
