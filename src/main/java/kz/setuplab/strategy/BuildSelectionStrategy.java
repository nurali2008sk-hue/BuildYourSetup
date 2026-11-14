package kz.setuplab.strategy;

import kz.setuplab.model.PcBuild;

import java.util.List;

public interface BuildSelectionStrategy {
    PcBuild select(List<PcBuild> candidates);

    String getName();
}