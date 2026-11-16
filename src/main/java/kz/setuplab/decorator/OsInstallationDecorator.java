
package kz.setuplab.decorator;

import kz.setuplab.model.AdditionalService;

public class OsInstallationDecorator extends BuildServiceDecorator {

    public static final long PRICE =
            AdditionalService.OS_INSTALLATION.price();

    public OsInstallationDecorator(PricedBuild wrappedBuild) {
        super(wrappedBuild);
    }

    @Override
    public long getPrice() {
        return Math.addExact(super.getPrice(), PRICE);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\nOS Installation: +10000 KZT";
    }
}
