package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.strategy.BuildSelectionStrategy;
import kz.setuplab.strategy.EconomyStrategy;
import kz.setuplab.strategy.PerformanceStrategy;
import kz.setuplab.facade.SetupFacade;

import java.util.ArrayList;
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
import java.io.IOException;

public class BuildPanel extends JPanel {
    private final JTextField nameField = new JTextField(25);
    private final JTextField budgetField = new JTextField("500000");

    private final JComboBox<BuildSelectionStrategy> strategyBox =
            new JComboBox<>();

    private PcBuild currentBuild;

    private final Map<ComponentType, JComboBox<PcComponent>> selectors =
            new EnumMap<>(ComponentType.class);

    private final JTextArea resultArea = new JTextArea();

    private final SetupFacade facade;

    public BuildPanel(ComponentCatalog catalog, SetupFacade facade) {
        this.facade = new SetupFacade(catalog);

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

        JButton saveButton = new JButton("Save build");
        saveButton.addActionListener(event -> saveBuild());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(autoSelectButton);
        buttons.add(createButton);
        buttons.add(saveButton);

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

            PcBuild build = facade.recommendBuild(
                    nameField.getText(),
                    budget,
                    strategy
            );

            for (PcComponent component : build.getComponents()) {
                selectors.get(component.type()).setSelectedItem(component);
            }

            currentBuild = build;

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
            PcBuild build = buildFromSelection();

            showBuild(build, "Created from your current selection");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private PcBuild buildFromSelection() {
        long budget = readBudget();

        List<PcComponent> components = new ArrayList<>();

        for (ComponentType type : ComponentType.values()) {
            PcComponent component =
                    (PcComponent) selectors.get(type).getSelectedItem();

            components.add(component);
        }

        PcBuild candidate = facade.createBuild(
                nameField.getText(),
                budget,
                components
        );

        boolean unchanged = currentBuild != null
                && currentBuild.getName().equals(candidate.getName())
                && currentBuild.getComponents().equals(candidate.getComponents());

        if (!unchanged) {
            currentBuild = candidate;
        }

        return currentBuild;
    }

    private void saveBuild() {
        try {
            PcBuild build = buildFromSelection();

            facade.saveBuild(build);

            showBuild(build, "Build saved successfully");

            JOptionPane.showMessageDialog(
                    this,
                    "Build saved to data/builds.json",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        } catch (IOException exception) {
            showError("Could not save build:\n" + exception.getMessage());
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