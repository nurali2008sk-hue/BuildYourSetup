package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.strategy.BuildSelectionStrategy;
import kz.setuplab.strategy.EconomyStrategy;
import kz.setuplab.strategy.PerformanceStrategy;
import kz.setuplab.facade.SetupFacade;
import kz.setuplab.decorator.PricedBuild;
import kz.setuplab.decorator.BasePricedBuild;
import kz.setuplab.decorator.AssemblyDecorator;
import kz.setuplab.decorator.WarrantyDecorator;
import kz.setuplab.decorator.OsInstallationDecorator;
import kz.setuplab.export.BuildExporter;
import kz.setuplab.export.ExportFactory;
import kz.setuplab.export.TxtExportFactory;
import kz.setuplab.export.HtmlExportFactory;

import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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
import java.nio.file.Files;
import java.nio.file.Path;
import kz.setuplab.repository.BuildServiceRepository.ServiceSelection;

public class BuildPanel extends JPanel {
    private final JTextField nameField = new JTextField(25);
    private final JTextField budgetField = new JTextField("500000");

    private final JComboBox<BuildSelectionStrategy> strategyBox =
            new JComboBox<>();

    private PcBuild currentBuild;

    private final JCheckBox assemblyBox = new JCheckBox("Professional assembly (+15000 KZT)");
    private final JCheckBox warrantyBox = new JCheckBox("Extended warranty (+20000 KZT)");
    private final JCheckBox osBox = new JCheckBox("OS installation (+10000 KZT)");

    private final Map<ComponentType, JComboBox<PcComponent>> selectors =
            new EnumMap<>(ComponentType.class);

    private final JTextArea resultArea = new JTextArea();

    private final SetupFacade facade;

    private ServiceSelection getSelectedServices() {
        return new ServiceSelection(
                assemblyBox.isSelected(),
                warrantyBox.isSelected(),
                osBox.isSelected()
        );
    }

    public BuildPanel(ComponentCatalog catalog, SetupFacade facade) {
        this.facade = java.util.Objects.requireNonNull(facade);

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

        addFormRow(form, constraints, row++, "Assembly:", assemblyBox);
        addFormRow(form, constraints, row++, "Warranty:", warrantyBox);
        addFormRow(form, constraints, row++, "Operating system:", osBox);

        assemblyBox.addActionListener(event -> refreshPrice());
        warrantyBox.addActionListener(event -> refreshPrice());
        osBox.addActionListener(event -> refreshPrice());

        JButton autoSelectButton = new JButton("Auto select");
        autoSelectButton.addActionListener(event -> autoSelect());

        JButton createButton = new JButton("Create build");
        createButton.addActionListener(event -> createBuild());

        JButton saveButton = new JButton("Save build");
        saveButton.addActionListener(event -> saveBuild());

        JButton exportTxtButton = new JButton("Export TXT");
        exportTxtButton.addActionListener(event -> exportBuild(new TxtExportFactory()));

        JButton exportHtmlButton = new JButton("Export HTML");
        exportHtmlButton.addActionListener(event -> exportBuild(new HtmlExportFactory()));

        JButton newButton = new JButton("New build");
        newButton.addActionListener(event -> startNewBuild());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(newButton);
        buttons.add(autoSelectButton);
        buttons.add(createButton);
        buttons.add(saveButton);
        buttons.add(exportTxtButton);
        buttons.add(exportHtmlButton);

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
                        + "The budget includes selected services."
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

            long componentBudget = budgetForComponents(budget);

            PcBuild build = facade.recommendBuild(
                    nameField.getText(),
                    componentBudget,
                    strategy
            );

            for (PcComponent component : build.getComponents()) {
                selectors.get(component.type()).setSelectedItem(component);
            }

            build = keepCurrentId(build);
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

        long componentBudget = budgetForComponents(budget);

        PcBuild candidate = facade.createBuild(
                nameField.getText(),
                componentBudget,
                components
        );

        currentBuild = keepCurrentId(candidate);

        return currentBuild;
    }


    private PcBuild keepCurrentId(PcBuild candidate) {
        if (currentBuild == null) {
            return candidate;
        }

        PcBuild.Builder builder = new PcBuild.Builder()
                .id(currentBuild.getId())
                .name(candidate.getName());

        for (PcComponent component : candidate.getComponents()) {
            builder.component(component);
        }

        return builder.build();
    }

    private void startNewBuild() {
        currentBuild = null;
        nameField.setText("");
        resultArea.setText(
                "New build started. Enter a name and configure components."
        );
        resultArea.setCaretPosition(0);
        nameField.requestFocusInWindow();
    }

    private void saveBuild() {
        try {
            PcBuild build = buildFromSelection();

            facade.saveBuild(build, getSelectedServices());

            showBuild(build, "Build saved successfully");

            JOptionPane.showMessageDialog(
                    this,
                    "Build and selected services saved successfully.",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IllegalArgumentException | ArithmeticException exception) {
            showError(exception.getMessage());
        } catch (IOException exception) {
            showError("Could not save build:\n" + exception.getMessage());
        }
    }


    public void loadBuild(PcBuild build) throws IOException {
        if (build == null) {
            throw new IllegalArgumentException("Build cannot be null.");
        }

        ServiceSelection services =
                facade.getBuildServices(build.getId());

        nameField.setText(build.getName());

        for (PcComponent component : build.getComponents()) {
            JComboBox<PcComponent> selector =
                    selectors.get(component.type());

            if (selector != null) {
                selector.setSelectedItem(component);
            }
        }

        assemblyBox.setSelected(services.assembly());
        warrantyBox.setSelected(services.warranty());
        osBox.setSelected(services.osInstallation());

        currentBuild = build;

        long finalPrice = applyServices(build).getPrice();
        long currentBudget;
        try {
            currentBudget = Long.parseLong(budgetField.getText().trim());
        } catch (NumberFormatException exception) {
            currentBudget = 0;
        }
        if (currentBudget < finalPrice) {
            budgetField.setText(Long.toString(finalPrice));
        }

        showBuild(build, "Loaded saved build");
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

        PricedBuild pricedBuild = applyServices(build);

        summary.append("\nComponents price: ")
                .append(build.getTotalPrice())
                .append(" KZT\n");

        summary.append("\nOptional services:\n");
        if (assemblyBox.isSelected()) {
            summary.append("Professional assembly: +15000 KZT\n");
        }
        if (warrantyBox.isSelected()) {
            summary.append("Extended warranty: +20000 KZT\n");
        }
        if (osBox.isSelected()) {
            summary.append("OS installation: +10000 KZT\n");
        }
        if (!assemblyBox.isSelected() && !warrantyBox.isSelected() && !osBox.isSelected()) {
            summary.append("None\n");
        }

        summary.append("\nFinal price: ")
                .append(pricedBuild.getPrice())
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
    private PricedBuild applyServices(PcBuild build) {
        PricedBuild result = new BasePricedBuild(build);
        if (assemblyBox.isSelected()) {
            result = new AssemblyDecorator(result);
        }
        if (warrantyBox.isSelected()) {
            result = new WarrantyDecorator(result);
        }
        if (osBox.isSelected()) {
            result = new OsInstallationDecorator(result);
        }
        return result;
    }

    private long selectedServicesPrice() {
        return getSelectedServices().totalPrice();
    }

    private long budgetForComponents(long budget) {
        long remaining = budget - selectedServicesPrice();
        if (remaining <= 0) {
            throw new IllegalArgumentException("Selected services leave no budget for components.");
        }
        return remaining;
    }

    private void refreshPrice() {
        if (currentBuild != null) {
            showBuild(currentBuild, "Current build preview");
        }
    }

    private void exportBuild(ExportFactory factory) {
        try {
            PcBuild build = buildFromSelection();
            PricedBuild pricedBuild = applyServices(build);
            long budget = readBudget();

            if (pricedBuild.getPrice() > budget) {
                throw new IllegalArgumentException("Build with services exceeds your budget.");
            }

            BuildExporter exporter = factory.createExporter();
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("pc-build." + exporter.getExtension()));

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            Path destination = chooser.getSelectedFile().toPath();
            if (Files.exists(destination)) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Replace the existing file?",
                        "Confirm overwrite",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            factory.exportToFile(build, pricedBuild, destination);
            JOptionPane.showMessageDialog(
                    this,
                    "Build exported successfully:\n" + destination,
                    "Export successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IllegalArgumentException | ArithmeticException exception) {
            showError(exception.getMessage());
        } catch (IOException exception) {
            showError("Export failed:\n" + exception.getMessage());
        }
    }


}