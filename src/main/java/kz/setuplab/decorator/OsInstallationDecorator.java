
package kz.setuplab.decorator;

public class OsInstallationDecorator extends BuildServiceDecorator {
    public static final long PRICE = 10000;

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
