package kz.setuplab.service;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.strategy.BuildSelectionStrategy;

import java.util.ArrayList;
import java.util.List;

public class BuildRecommendationService {
    private final ComponentCatalog catalog;
    private final CompatibilityService compatibilityService;
    private final ComponentType[] types = ComponentType.values();

    public BuildRecommendationService(ComponentCatalog catalog) {
        this.catalog = catalog;
        compatibilityService = new CompatibilityService();
    }

    public PcBuild recommend(
            String name,
            long budget,
            BuildSelectionStrategy strategy
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enter a build name.");
        }

        if (name.trim().length() > 60) {
            throw new IllegalArgumentException(
                    "Build name must not exceed 60 characters."
            );
        }

        if (budget <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }

        if (strategy == null) {
            throw new IllegalArgumentException("Select a strategy.");
        }

        List<PcBuild> candidates = new ArrayList<>();

        findCandidates(
                name,
                budget,
                0,
                0,
                new ArrayList<>(),
                candidates
        );

        return strategy.select(List.copyOf(candidates));
    }

    private void findCandidates(
            String name,
            long budget,
            int typeIndex,
            long currentPrice,
            List<PcComponent> selected,
            List<PcBuild> candidates
    ) {
        if (typeIndex == types.length) {
            PcBuild.Builder builder = new PcBuild.Builder().name(name);

            for (PcComponent component : selected) {
                builder.component(component);
            }

            PcBuild build = builder.build();

            if (compatibilityService.validate(build).isEmpty()) {
                candidates.add(build);
            }

            return;
        }

        ComponentType type = types[typeIndex];

        for (PcComponent component : catalog.findByType(type)) {
            if (component.price() > budget - currentPrice) {
                continue;
            }

            selected.add(component);

            findCandidates(
                    name,
                    budget,
                    typeIndex + 1,
                    currentPrice + component.price(),
                    selected,
                    candidates
            );

            selected.remove(selected.size() - 1);
        }
    }
}