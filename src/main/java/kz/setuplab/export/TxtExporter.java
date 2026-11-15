
package kz.setuplab.export;

import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.decorator.PricedBuild;

public class TxtExporter implements BuildExporter {

    @Override
    public String export(PcBuild build, PricedBuild pricedBuild) {
        StringBuilder result = new StringBuilder();

        result.append("PC BUILD REPORT\n");
        result.append("====================\n");
        result.append("Build: ").append(build.getName()).append("\n\n");

        for (PcComponent component : build.getComponents()) {
            result.append(component.type())
                    .append(": ")
                    .append(component.name())
                    .append(" - ")
                    .append(component.price())
                    .append(" KZT\n");
        }

        result.append("\nComponents price: ")
                .append(build.getTotalPrice())
                .append(" KZT\n");

        result.append("\nServices:\n")
                .append(pricedBuild.getDescription())
                .append("\n");

        result.append("\nFinal price: ")
                .append(pricedBuild.getPrice())
                .append(" KZT\n");

        return result.toString();
    }

    @Override
    public String getExtension() {
        return "txt";
    }
}
