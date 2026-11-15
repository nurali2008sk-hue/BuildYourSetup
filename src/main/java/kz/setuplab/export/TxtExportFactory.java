
package kz.setuplab.export;

public class TxtExportFactory extends ExportFactory {

    @Override
    public BuildExporter createExporter() {
        return new TxtExporter();
    }
}
