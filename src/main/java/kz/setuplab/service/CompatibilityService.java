package kz.setuplab.service;

import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityService {
    public List<String> validate(PcBuild build) {
        List<String> errors = new ArrayList<>();

        PcComponent cpu = build.getComponent(ComponentType.CPU);
        PcComponent motherboard =
                build.getComponent(ComponentType.MOTHERBOARD);
        PcComponent ram = build.getComponent(ComponentType.RAM);
        PcComponent psu = build.getComponent(ComponentType.PSU);

        if (!cpu.socket().equals(motherboard.socket())) {
            errors.add(
                    "CPU socket " + cpu.socket()
                            + " does not match motherboard socket "
                            + motherboard.socket() + "."
            );
        }

        if (!ram.memoryType().equals(motherboard.memoryType())) {
            errors.add(
                    "RAM type " + ram.memoryType()
                            + " does not match motherboard memory type "
                            + motherboard.memoryType() + "."
            );
        }

        int requiredPower =
                (int) Math.ceil(build.getEstimatedPower() * 1.25);

        if (psu.powerWatts() < requiredPower) {
            errors.add(
                    "Power supply is too weak. Required: "
                            + requiredPower + " W including 25% reserve."
                            + " Selected: " + psu.powerWatts() + " W."
            );
        }

        return List.copyOf(errors);
    }
}