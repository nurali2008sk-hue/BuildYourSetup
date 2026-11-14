package kz.setuplab.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PcBuild {
    private final String id;
    private final String name;
    private final List<PcComponent> components;

    private PcBuild(Builder builder) {
        id = UUID.randomUUID().toString();
        name = builder.name.trim();
        components = List.copyOf(builder.components.values());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PcComponent> getComponents() {
        return components;
    }

    public PcComponent getComponent(ComponentType type) {
        return components.stream()
                .filter(component -> component.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing component: " + type
                ));
    }

    public long getTotalPrice() {
        return components.stream()
                .mapToLong(PcComponent::price)
                .sum();
    }

    public int getEstimatedPower() {
        return components.stream()
                .filter(component -> component.type() != ComponentType.PSU)
                .mapToInt(PcComponent::powerWatts)
                .sum();
    }

    public static class Builder {
        private String name;
        private final Map<ComponentType, PcComponent> components =
                new EnumMap<>(ComponentType.class);

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder component(PcComponent component) {
            if (component == null) {
                throw new IllegalArgumentException("Select a component.");
            }

            components.put(component.type(), component);
            return this;
        }

        public PcBuild build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Enter a build name.");
            }

            if (name.trim().length() > 60) {
                throw new IllegalArgumentException(
                        "Build name must not exceed 60 characters."
                );
            }

            for (ComponentType type : ComponentType.values()) {
                if (!components.containsKey(type)) {
                    throw new IllegalArgumentException(
                            "Select a component for: " + type
                    );
                }
            }

            return new PcBuild(this);
        }
    }
}