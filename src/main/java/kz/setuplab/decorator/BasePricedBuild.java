
package kz.setuplab.decorator;

import kz.setuplab.model.PcBuild;
import java.util.Objects;

public class BasePricedBuild implements PricedBuild {
    private final PcBuild build;

    public BasePricedBuild(PcBuild build) {
        this.build = Objects.requireNonNull(build);
    }

    @Override
    public long getPrice() {
        return build.getTotalPrice();
    }

    @Override
    public String getDescription() {
        return build.getName();
    }
}
