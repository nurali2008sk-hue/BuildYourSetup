package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.service.CompatibilityService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BuildPanel extends JPanel {
    private final JTextField nameField = new JTextField(25);

    private final Map<ComponentType, JComboBox<PcComponent>> selectors =
            new EnumMap<>(ComponentType.class);

    private final JTextArea resultArea = new JTextArea();

    private final CompatibilityService compatibilityService =
            new CompatibilityService();

    public BuildPanel(ComponentCatalog catalog) {
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        addFormRow(form, constraints, 0, "Build name:", nameField);

        int row = 1;

        for (ComponentType type : ComponentType.values()) {
            JComboBox<PcComponent> selector = new JComboBox<>();

            for (PcComponent component : catalog.findByType(type)) {
                selector.addItem(component);
            }

            selectors.put(type, selector);
            addFormRow(form, constraints, row++, type + ":", selector);
        }

        JButton createButton = new JButton("Create build");
        createButton.addActionListener(event -> createBuild());

        constraints.gridx = 1;
        constraints.gridy = row;
        form.add(createButton, constraints);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setText(
                "Choose components, enter a name and click Create build."
        );

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    private void addFormRow(
            JPanel form,
            GridBagConstraints constraints,
            int row,
            String label,
            java.awt.Component field
    ) {
        constraints.gridy = row;

        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(new JLabel(label), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(field, constraints);
    }

    private void createBuild() {
        try {
            PcBuild.Builder builder = new PcBuild.Builder()
                    .name(nameField.getText());

            for (ComponentType type : ComponentType.values()) {
                PcComponent component =
                        (PcComponent) selectors.get(type).getSelectedItem();

                builder.component(component);
            }

            PcBuild build = builder.build();

            List<String> errors = compatibilityService.validate(build);

            if (!errors.isEmpty()) {
                String message = String.join("\n", errors);

                resultArea.setText(
                        "Build failed compatibility checks:\n\n" + message
                );
                resultArea.setCaretPosition(0);

                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Compatibility error",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            showBuild(build);
        } catch (IllegalArgumentException exception) {
            resultArea.setText(exception.getMessage());

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Validation error",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showBuild(PcBuild build) {
        StringBuilder summary = new StringBuilder();

        summary.append("Build: ")
                .append(build.getName())
                .append("\n\n");

        for (PcComponent component : build.getComponents()) {
            summary.append(component.type())
                    .append(": ")
                    .append(component.name())
                    .append(" — ")
                    .append(component.price())
                    .append(" KZT\n");
        }

        int requiredPower =
                (int) Math.ceil(build.getEstimatedPower() * 1.25);

        summary.append("\nTotal price: ")
                .append(build.getTotalPrice())
                .append(" KZT\n");

        summary.append("Estimated component power: ")
                .append(build.getEstimatedPower())
                .append(" W\n");

        summary.append("Required PSU capacity with 25% reserve: ")
                .append(requiredPower)
                .append(" W\n");

        summary.append("\nCompatibility checks passed.");

        resultArea.setText(summary.toString());
        resultArea.setCaretPosition(0);
    }
}