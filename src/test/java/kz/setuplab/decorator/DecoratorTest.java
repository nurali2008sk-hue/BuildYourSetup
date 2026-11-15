
package kz.setuplab.decorator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {

    @Test
    void shouldAddAssemblyPrice() {
        PricedBuild base = sampleBuild(450000);
        PricedBuild decorated = new AssemblyDecorator(base);

        assertEquals(465000, decorated.getPrice());
        assertEquals(450000, base.getPrice());
    }

    @Test
    void shouldCombineServices() {
        PricedBuild build = sampleBuild(450000);

        build = new AssemblyDecorator(build);
        build = new WarrantyDecorator(build);
        build = new OsInstallationDecorator(build);

        assertEquals(495000, build.getPrice());
        assertTrue(build.getDescription().contains("Extended Warranty"));
        assertTrue(build.getDescription().contains("OS Installation"));
    }

    private PricedBuild sampleBuild(long price) {
        return new PricedBuild() {
            @Override
            public long getPrice() {
                return price;
            }

            @Override
            public String getDescription() {
                return "Test PC";
            }
        };
    }
}
