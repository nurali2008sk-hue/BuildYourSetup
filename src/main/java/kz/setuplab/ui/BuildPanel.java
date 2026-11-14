package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.service.BuildRecommendationService;
import kz.setuplab.service.CompatibilityService;
import kz.setuplab.strategy.BuildSelectionStrategy;
import kz.setuplab.strategy.EconomyStrategy;
import kz.setuplab.strategy.PerformanceStrategy;

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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BuildPanel extends JPanel {
    private final JTextField nameField = new JTextField(25);
    private final JTextField budgetField = new JTextField("500000");

    private final JComboBox<BuildSelectionStrategy> strategyBox =
            new JComboBox<>();

    private final Map<ComponentType, JComboBox<PcComponent>> selectors =
            new EnumMap<>(ComponentType.class);

    private final JTextArea resultArea = new JTextArea();

    private final CompatibilityService compatibilityService =
            new CompatibilityService();

    private final BuildRecommendationService recommendationService;

    public BuildPanel(ComponentCatalog catalog) {
        recommendationService = new BuildRecommendationService(catalog);

        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 5, 4, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        strategyBox.addItem(new EconomyStrategy());
        strategyBox.addItem(new PerformanceStrategy());

        addFormRow(form, constraints, 0, "Build name:", nameField);
        addFormRow(form, constraints, 1, "Budget (KZT):", budgetField);
        addFormRow(form, constraints, 2, "Selection strategy:", strategyBox);

        int row = 3;

        for (ComponentType type : ComponentType.values()) {
            JComboBox<PcComponent> selector = new JComboBox<>();

            for (PcComponent component : catalog.findByType(type)) {
                selector.addItem(component);
            }

            selectors.put(type, selector);
            addFormRow(form, constraints, row++, type + ":", selector);
        }

        JButton autoSelectButton = new JButton("Auto select");
        autoSelectButton.addActionListener(event -> autoSelect());

        JButton createButton = new JButton("Create build");
        createButton.addActionListener(event -> createBuild());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(autoSelectButton);
        buttons.add(createButton);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 1;
        form.add(buttons, constraints);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setText(
                "Enter a build name and budget.\n"
                        + "Use Auto select or choose components manually.\n"
                        + "The budget applies to both actions."
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

    private long readBudget() {
        String value = budgetField.getText().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Enter a budget.");
        }

        long budget;

        try {
            budget = Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Budget must be a whole number, for example 500000."
            );
        }

        if (budget <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero."
            );
        }

        return budget;
    }

    private void autoSelect() {
        try {
            long budget = readBudget();

            BuildSelectionStrategy strategy =
                    (BuildSelectionStrategy) strategyBox.getSelectedItem();

            PcBuild build = recommendationService.recommend(
                    nameField.getText(),
                    budget,
                    strategy
            );

            for (PcComponent component : build.getComponents()) {
                selectors.get(component.type()).setSelectedItem(component);
            }

            showBuild(
                    build,
                    "Automatically selected using " + strategy.getName()
            );
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void createBuild() {
        try {
            long budget = readBudget();

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
                throw new IllegalArgumentException(
                        String.join("\n", errors)
                );
            }

            if (build.getTotalPrice() > budget) {
                throw new IllegalArgumentException(
                        "Build exceeds your budget by "
                                + (build.getTotalPrice() - budget)
                                + " KZT."
                );
            }

            showBuild(build, "Created from your current selection");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void showError(String message) {
        resultArea.setText(message);
        resultArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showBuild(PcBuild build, String action) {
        StringBuilder summary = new StringBuilder();

        summary.append(action)
                .append("\n\nBuild: ")
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

        int score = build.getComponents().stream()
                .mapToInt(PcComponent::performanceScore)
                .sum();

        summary.append("\nTotal price: ")
                .append(build.getTotalPrice())
                .append(" KZT\n");

        summary.append("Demo performance score: ")
                .append(score)
                .append("\n");

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