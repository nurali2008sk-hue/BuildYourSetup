package kz.setuplab.data;

import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcComponent;

import java.util.List;

public class ComponentCatalog {
    private final List<PcComponent> components;

    public ComponentCatalog() {
        components = List.of(
                new PcComponent(
                        "cpu-1", "Atlas CPU 6", ComponentType.CPU,
                        60000, "AM4", "", 65, 60
                ),
                new PcComponent(
                        "cpu-2", "Atlas CPU 8", ComponentType.CPU,
                        110000, "AM5", "", 105, 90
                ),
                new PcComponent(
                        "mb-1", "Atlas Board A", ComponentType.MOTHERBOARD,
                        45000, "AM4", "DDR4", 35, 0
                ),
                new PcComponent(
                        "mb-2", "Atlas Board B", ComponentType.MOTHERBOARD,
                        75000, "AM5", "DDR5", 40, 0
                ),
                new PcComponent(
                        "gpu-1", "Nova Graphics 6", ComponentType.GPU,
                        120000, "", "", 120, 60
                ),
                new PcComponent(
                        "gpu-2", "Nova Graphics 8", ComponentType.GPU,
                        220000, "", "", 200, 90
                ),
                new PcComponent(
                        "ram-1", "Atlas 16 GB DDR4", ComponentType.RAM,
                        22000, "", "DDR4", 8, 40
                ),
                new PcComponent(
                        "ram-2", "Atlas 32 GB DDR4", ComponentType.RAM,
                        38000, "", "DDR4", 12, 70
                ),
                new PcComponent(
                        "ram-3", "Atlas 16 GB DDR5", ComponentType.RAM,
                        30000, "", "DDR5", 8, 50
                ),
                new PcComponent(
                        "ram-4", "Atlas 32 GB DDR5", ComponentType.RAM,
                        50000, "", "DDR5", 12, 80
                ),
                new PcComponent(
                        "ssd-1", "Nova SSD 500 GB", ComponentType.STORAGE,
                        20000, "", "", 5, 40
                ),
                new PcComponent(
                        "ssd-2", "Nova SSD 1 TB", ComponentType.STORAGE,
                        35000, "", "", 7, 70
                ),
                new PcComponent(
                        "psu-1", "Atlas Power 450 W", ComponentType.PSU,
                        25000, "", "", 450, 0
                ),
                new PcComponent(
                        "psu-2", "Atlas Power 650 W", ComponentType.PSU,
                        40000, "", "", 650, 0
                ),
                new PcComponent(
                        "case-1", "Atlas Basic Case", ComponentType.CASE,
                        20000, "", "", 0, 0
                ),
                new PcComponent(
                        "case-2", "Atlas Airflow Case", ComponentType.CASE,
                        35000, "", "", 0, 0
                )
        );
    }

    public List<PcComponent> getAll() {
        return components;
    }

    public List<PcComponent> findByType(ComponentType type) {
        return components.stream()
                .filter(component -> component.type() == type)
                .toList();
    }
}