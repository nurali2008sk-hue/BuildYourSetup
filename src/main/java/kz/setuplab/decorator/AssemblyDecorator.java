
package kz.setuplab.decorator;

import kz.setuplab.model.AdditionalService;

public class AssemblyDecorator extends BuildServiceDecorator {
    public static final long PRICE =
            AdditionalService.ASSEMBLY.price();

    public AssemblyDecorator(PricedBuild wrappedBuild) {
        super(wrappedBuild);
    }

    @Override
    public long getPrice() {
        return Math.addExact(super.getPrice(), PRICE);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "\nProfessional Assembly: +15000 KZT";
    }
}
