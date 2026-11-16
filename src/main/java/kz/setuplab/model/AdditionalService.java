package kz.setuplab.model;

public record AdditionalService(String id, String name, long price) {
    public static final AdditionalService ASSEMBLY =
            new AdditionalService(
                    "assembly",
                    "Professional assembly",
                    15000
            );

    public static final AdditionalService WARRANTY =
            new AdditionalService(
                    "warranty",
                    "Extended warranty",
                    20000
            );

    public static final AdditionalService OS_INSTALLATION =
            new AdditionalService(
                    "os-installation",
                    "OS installation",
                    10000
            );

    public AdditionalService {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Service ID is required."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Service name is required."
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Service price cannot be negative."
            );
        }

        id = id.trim();
        name = name.trim();
    }
}