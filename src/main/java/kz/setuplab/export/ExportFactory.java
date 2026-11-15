
package kz.setuplab.export;

import kz.setuplab.model.PcBuild;
import kz.setuplab.decorator.PricedBuild;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public abstract class ExportFactory {

    public abstract BuildExporter createExporter();

    public Path exportToFile(
            PcBuild build,
            PricedBuild pricedBuild,
            Path destination
    ) throws IOException {
        Objects.requireNonNull(build);
        Objects.requireNonNull(pricedBuild);
        Objects.requireNonNull(destination);

        BuildExporter exporter = createExporter();

        Files.writeString(
                destination,
                exporter.export(build, pricedBuild),
                StandardCharsets.UTF_8
        );

        return destination;
    }
}
