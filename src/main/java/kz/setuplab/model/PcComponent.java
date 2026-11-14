package kz.setuplab.model;

import java.util.Objects;

public record PcComponent(
        String id,
        String name,
        ComponentType type,
        long price,
        String socket,
        String memoryType,
        int powerWatts,
        int performanceScore
) {
    public PcComponent {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Component ID is required.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Component name is required.");
        }

        Objects.requireNonNull(type, "Component type is required.");

        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }

        if (powerWatts < 0) {
            throw new IllegalArgumentException("Power cannot be negative.");
        }

        if (performanceScore < 0) {
            throw new IllegalArgumentException(
                    "Performance score cannot be negative."
            );
        }

        id = id.trim();
        name = name.trim();
        socket = socket == null ? "" : socket.trim();
        memoryType = memoryType == null ? "" : memoryType.trim();
    }

    @Override
    public String toString() {
        return name + " — " + price + " KZT";
    }
}