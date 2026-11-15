
package kz.setuplab.export;

import kz.setuplab.decorator.PricedBuild;
import kz.setuplab.model.PcBuild;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportFactoryTest {

    private final PricedBuild pricedBuild = new PricedBuild() {
        @Override
        public long getPrice() {
            return 485000;
        }

        @Override
        public String getDescription() {
            return "Gaming PC\nProfessional Assembly\nExtended Warranty";
        }
    };

    @Test
    void shouldCreateTxtExporter() {
        ExportFactory factory = new TxtExportFactory();

        assertInstanceOf(TxtExporter.class, factory.createExporter());
        assertEquals("txt", factory.createExporter().getExtension());
    }

    @Test
    void shouldCreateHtmlExporter() {
        ExportFactory factory = new HtmlExportFactory();

        assertInstanceOf(HtmlExporter.class, factory.createExporter());
        assertEquals("html", factory.createExporter().getExtension());
    }

    @Test
    void shouldRejectNullBuild() {
        ExportFactory factory = new TxtExportFactory();

        assertThrows(
                NullPointerException.class,
                () -> factory.exportToFile(
                        null,
                        pricedBuild,
                        java.nio.file.Path.of("test.txt")
                )
        );
    }

    @Test
    void shouldRejectNullDestination() {
        ExportFactory factory = new HtmlExportFactory();

        assertThrows(
                NullPointerException.class,
                () -> factory.exportToFile(
                        null,
                        pricedBuild,
                        null
                )
        );
    }
}
