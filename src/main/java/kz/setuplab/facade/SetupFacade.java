package kz.setuplab.facade;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.service.BuildRecommendationService;
import kz.setuplab.service.CompatibilityService;
import kz.setuplab.strategy.BuildSelectionStrategy;

import java.util.List;

public class SetupFacade {
    private final CompatibilityService compatibilityService;
    private final BuildRecommendationService recommendationService;

    public SetupFacade(ComponentCatalog catalog) {
        compatibilityService = new CompatibilityService();
        recommendationService = new BuildRecommendationService(catalog);
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
            throw new IllegalArgumentException(String.join("\n", errors));
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