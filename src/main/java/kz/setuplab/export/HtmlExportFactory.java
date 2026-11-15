
package kz.setuplab.export;

public class HtmlExportFactory extends ExportFactory {

    @Override
    public BuildExporter createExporter() {
        return new HtmlExporter();
    }
}
