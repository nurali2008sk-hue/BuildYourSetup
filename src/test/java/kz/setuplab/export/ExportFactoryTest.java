
package kz.setuplab.export;

import kz.setuplab.decorator.PricedBuild;
import kz.setuplab.model.PcBuild;
import org.junit.jupiter.api.Test;
import kz.setuplab.model.PcComponent;
import kz.setuplab.model.ComponentType;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

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
                        sampleBuild(),
                        pricedBuild,
                        null
                )
        );
    }
    @Test
    void shouldExportTxtFile(@TempDir Path tempDir) throws IOException {
        ExportFactory factory = new TxtExportFactory();
        Path destination = tempDir.resolve("build.txt");

        Path result = factory.exportToFile(
                sampleBuild(),
                pricedBuild,
                destination
        );

        assertEquals(destination, result);
        assertTrue(Files.exists(destination));

        String content = Files.readString(destination);

        assertTrue(content.contains("Gaming PC"));
        assertTrue(content.contains("485000"));
    }

    @Test
    void shouldExportHtmlFile(@TempDir Path tempDir) throws IOException {
        ExportFactory factory = new HtmlExportFactory();
        Path destination = tempDir.resolve("build.html");

        factory.exportToFile(
                sampleBuild(),
                pricedBuild,
                destination
        );

        assertTrue(Files.exists(destination));

        String content = Files.readString(destination);

        assertTrue(content.toLowerCase().contains("<html"));
        assertTrue(content.contains("Gaming PC"));
        assertTrue(content.contains("485000"));
    }
    private PcBuild sampleBuild() {
        PcBuild.Builder builder = new PcBuild.Builder()
                .name("Gaming PC");

        for (ComponentType type : ComponentType.values()) {
            builder.component(new PcComponent(
                    type.name().toLowerCase(),
                    "Test " + type.name(),
                    type,
                    10000,
                    "",
                    "",
                    50,
                    100
            ));
        }

        return builder.build();
    }
}
