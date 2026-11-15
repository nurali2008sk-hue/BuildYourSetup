
package kz.setuplab.decorator;

import java.util.Objects;

public abstract class BuildServiceDecorator implements PricedBuild {
    protected final PricedBuild wrappedBuild;

    protected BuildServiceDecorator(PricedBuild wrappedBuild) {
        this.wrappedBuild = Objects.requireNonNull(wrappedBuild);
    }

    @Override
    public long getPrice() {
        return wrappedBuild.getPrice();
    }

    @Override
    public String getDescription() {
        return wrappedBuild.getDescription();
    }
}
