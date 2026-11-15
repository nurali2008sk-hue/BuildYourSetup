
package kz.setuplab.decorator;

public class AssemblyDecorator extends BuildServiceDecorator {
    public static final long PRICE = 15000;

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
