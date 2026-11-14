package kz.setuplab.strategy;

import kz.setuplab.model.PcBuild;

import java.util.Comparator;
import java.util.List;

public class PerformanceStrategy implements BuildSelectionStrategy {
    @Override
    public PcBuild select(List<PcBuild> candidates) {
        Comparator<PcBuild> comparator =
                Comparator.comparingInt(this::calculateScore)
                        .reversed()
                        .thenComparingLong(PcBuild::getTotalPrice);

        return candidates.stream()
                .min(comparator)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No compatible build fits your budget."
                ));
    }

    private int calculateScore(PcBuild build) {
        return build.getComponents().stream()
                .mapToInt(component -> component.performanceScore())
                .sum();
    }

    @Override
    public String getName() {
        return "Performance";
    }

    @Override
    public String toString() {
        return getName();
    }
}