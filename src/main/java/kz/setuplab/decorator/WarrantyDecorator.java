
package kz.setuplab.decorator;

import kz.setuplab.model.AdditionalService;

public class WarrantyDecorator extends BuildServiceDecorator {
    public static final long PRICE =
            AdditionalService.WARRANTY.price();

    public WarrantyDecorator(PricedBuild wrappedBuild) {
        super(wrappedBuild);
    }

    @Override
    public long getPrice() {
        return Math.addExact(super.getPrice(), PRICE);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\nExtended Warranty: +20000 KZT";
    }
}
