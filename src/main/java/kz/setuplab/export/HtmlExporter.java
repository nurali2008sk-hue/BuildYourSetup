
package kz.setuplab.export;

import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.decorator.PricedBuild;

public class HtmlExporter implements BuildExporter {

    @Override
    public String export(PcBuild build, PricedBuild pricedBuild) {
        StringBuilder result = new StringBuilder();

        result.append("<!DOCTYPE html>\n");
        result.append("<html lang=\"en\">\n");
        result.append("<head>\n");
        result.append("<meta charset=\"UTF-8\">\n");
        result.append("<title>PC Build Report</title>\n");
        result.append("</head>\n<body>\n");

        result.append("<h1>")
                .append(escape(build.getName()))
                .append("</h1>\n");

        result.append("<h2>Components</h2>\n<ul>\n");

        for (PcComponent component : build.getComponents()) {
            result.append("<li>")
                    .append(escape(component.type().toString()))
                    .append(": ")
                    .append(escape(component.name()))
                    .append(" - ")
                    .append(component.price())
                    .append(" KZT</li>\n");
        }

        result.append("</ul>\n");

        result.append("<p>Components price: ")
                .append(build.getTotalPrice())
                .append(" KZT</p>\n");

        result.append("<h2>Selected services</h2>\n<pre>")
                .append(escape(pricedBuild.getDescription()))
                .append("</pre>\n");

        result.append("<h2>Final price: ")
                .append(pricedBuild.getPrice())
                .append(" KZT</h2>\n");

        result.append("</body>\n</html>");

        return result.toString();
    }

    private String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    public String getExtension() {
        return "html";
    }
}
