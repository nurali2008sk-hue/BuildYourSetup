
package kz.setuplab.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BuildServiceRepository {

    public record ServiceSelection(
            boolean assembly,
            boolean warranty,
            boolean osInstallation
    ) {}

    private static final Type DATA_TYPE =
            new TypeToken<Map<String, ServiceSelection>>() {}.getType();

    private final Path file;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public BuildServiceRepository(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath();
    }

    private Map<String, ServiceSelection> readAll() throws IOException {
        if (!Files.exists(file)) {
            return new HashMap<>();
        }

        try {
            Map<String, ServiceSelection> data = gson.fromJson(
                    Files.readString(file, StandardCharsets.UTF_8),
                    DATA_TYPE
            );

            if (data == null || data.containsKey(null)
                    || data.containsValue(null)) {
                throw new IOException("Invalid service data.");
            }

            return new HashMap<>(data);
        } catch (com.google.gson.JsonParseException exception) {
            throw new IOException("Invalid service data.", exception);
        }
    }

    public boolean containsBuildId(String buildId) throws IOException {
        Objects.requireNonNull(buildId);
        return readAll().containsKey(buildId);
    }

    public ServiceSelection findByBuildId(String buildId)
            throws IOException {
        Objects.requireNonNull(buildId);

        return readAll().getOrDefault(
                buildId,
                new ServiceSelection(false, false, false)
        );
    }

    public void save(String buildId, ServiceSelection selection)
            throws IOException {
        Objects.requireNonNull(buildId);
        Objects.requireNonNull(selection);

        Map<String, ServiceSelection> data = readAll();
        data.put(buildId, selection);
        writeAll(data);
    }

    public void delete(String buildId) throws IOException {
        Map<String, ServiceSelection> data = readAll();

        if (data.remove(buildId) != null) {
            writeAll(data);
        }
    }

    private void writeAll(Map<String, ServiceSelection> data)
            throws IOException {
        Path parent = file.getParent();
        Files.createDirectories(parent);

        Path temporary = Files.createTempFile(
                parent, "services-", ".tmp"
        );

        try {
            Files.writeString(
                    temporary,
                    gson.toJson(data, DATA_TYPE),
                    StandardCharsets.UTF_8
            );

            Files.move(
                    temporary,
                    file,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
