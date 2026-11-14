package kz.setuplab.model;

public enum ComponentType {
    CPU("Processor"),
    MOTHERBOARD("Motherboard"),
    GPU("Graphics card"),
    RAM("Memory"),
    STORAGE("Storage"),
    PSU("Power supply"),
    CASE("Case");

    private final String displayName;

    ComponentType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}