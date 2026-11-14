package kz.setuplab.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import kz.setuplab.model.PcBuild;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonBuildRepository {
    private static final Type BUILD_LIST_TYPE =
            new TypeToken<List<PcBuild>>() {}.getType();

    private final Path file;
    private final Gson gson;

    public JsonBuildRepository(Path file) {
        this.file = file.toAbsolutePath();
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public List<PcBuild> findAll() throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        String json = Files.readString(file, StandardCharsets.UTF_8);

        try {
            List<PcBuild> builds = gson.fromJson(json, BUILD_LIST_TYPE);

            if (builds == null) {
                throw new IllegalArgumentException("Expected a build list.");
            }

            Set<String> ids = new HashSet<>();

            for (PcBuild build : builds) {
                validateStoredBuild(build);

                if (!ids.add(build.getId())) {
                    throw new IllegalArgumentException(
                            "Duplicate build IDs."
                    );
                }
            }

            return new ArrayList<>(builds);
        } catch (JsonParseException | IllegalArgumentException
                 | NullPointerException exception) {
            throw new IOException(
                    "Saved builds file is invalid. "
                            + "The file has not been overwritten.",
                    exception
            );
        }
    }

    public void save(PcBuild build) throws IOException {
        validateStoredBuild(build);

        List<PcBuild> builds = findAll();

        builds.removeIf(existing ->
                existing.getId().equals(build.getId())
        );

        builds.add(build);

        writeAll(builds);
    }

    public boolean deleteById(String id) throws IOException {
        List<PcBuild> builds = findAll();

        boolean removed = builds.removeIf(build ->
                build.getId().equals(id)
        );

        if (removed) {
            writeAll(builds);
        }

        return removed;
    }

    private void validateStoredBuild(PcBuild build) {
        if (build == null
                || build.getId() == null
                || build.getId().isBlank()
                || build.getComponents() == null) {
            throw new IllegalArgumentException("Invalid saved build.");
        }

        PcBuild.Builder builder = new PcBuild.Builder()
                .name(build.getName());

        for (var component : build.getComponents()) {
            builder.component(component);
        }

        PcBuild validated = builder.build();

        if (validated.getComponents().size()
                != build.getComponents().size()) {
            throw new IllegalArgumentException(
                    "Duplicate component categories."
            );
        }
    }

    private void writeAll(List<PcBuild> builds) throws IOException {
        Path directory = file.getParent();

        Files.createDirectories(directory);

        Path temporaryFile = Files.createTempFile(
                directory,
                "builds-",
                ".tmp"
        );

        try {
            Files.writeString(
                    temporaryFile,
                    gson.toJson(builds, BUILD_LIST_TYPE),
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}