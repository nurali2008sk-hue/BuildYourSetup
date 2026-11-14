package kz.setuplab.strategy;

import kz.setuplab.model.PcBuild;

import java.util.Comparator;
import java.util.List;

public class EconomyStrategy implements BuildSelectionStrategy {
    @Override
    public PcBuild select(List<PcBuild> candidates) {
        return candidates.stream()
                .min(Comparator.comparingLong(PcBuild::getTotalPrice))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No compatible build fits your budget."
                ));
    }

    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public String toString() {
        return getName();
    }
}