
package kz.setuplab.export;

import kz.setuplab.model.PcBuild;
import kz.setuplab.decorator.PricedBuild;

public interface BuildExporter {
    String export(PcBuild build, PricedBuild pricedBuild);
    String getExtension();
}
