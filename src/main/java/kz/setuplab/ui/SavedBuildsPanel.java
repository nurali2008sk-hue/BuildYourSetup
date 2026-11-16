
package kz.setuplab.ui;

import kz.setuplab.facade.SetupFacade;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;
import kz.setuplab.repository.BuildServiceRepository.ServiceSelection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SavedBuildsPanel extends JPanel {

    private final SetupFacade facade;
    private final List<PcComponent> availableComponents;
    private final Consumer<PcBuild> onLoadBuild;
    private final JButton loadButton = new JButton("Load in Configurator");

    private final DefaultListModel<PcBuild> listModel =
            new DefaultListModel<>();

    private final JList<PcBuild> buildList =
            new JList<>(listModel);

    private final JTextArea detailsArea =
            new JTextArea();

    private final JLabel countLabel =
            new JLabel("Saved builds: 0");

    private final JButton deleteButton =
            new JButton("Delete selected");

    private final JButton editButton =
            new JButton("Edit selected");

    public SavedBuildsPanel(
            SetupFacade facade,
            List<PcComponent> availableComponents,
            Consumer<PcBuild> onLoadBuild
    ) {

        this.facade = facade;
        this.availableComponents = List.copyOf(availableComponents);
        this.onLoadBuild = java.util.Objects.requireNonNull(onLoadBuild);

        setLayout(new BorderLayout(0, 12));

        setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // Toolbar
        JButton refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(
                event -> reloadBuilds()
        );

        editButton.setEnabled(false);
        editButton.addActionListener(
                event -> editSelected()
        );

        deleteButton.setEnabled(false);
        deleteButton.addActionListener(
                event -> deleteSelected()
        );

        loadButton.setEnabled(false);
        loadButton.addActionListener(event -> {
            PcBuild selected = buildList.getSelectedValue();
            if (selected != null) {
                onLoadBuild.accept(selected);
            }
        });

        JPanel toolbar =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        toolbar.add(refreshButton);
        toolbar.add(loadButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);

        // Build list
        buildList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        buildList.setFixedCellHeight(32);

        buildList.setCellRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {

                        JLabel label =
                                (JLabel) super.getListCellRendererComponent(
                                        list,
                                        value,
                                        index,
                                        isSelected,
                                        cellHasFocus
                                );

                        if (value instanceof PcBuild build) {
                            label.setText(
                                    build.getName()
                                            + " — "
                                            + build.getTotalPrice()
                                            + " KZT"
                            );
                        }

                        return label;
                    }
                }
        );

        buildList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelected();
            }
        });

        // Build details
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        detailsArea.setMargin(
                new java.awt.Insets(10, 10, 10, 10)
        );

        detailsArea.setText(
                "Open this tab or click Refresh to load builds."
        );

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(buildList),
                new JScrollPane(detailsArea)
        );

        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(320);

        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(countLabel, BorderLayout.SOUTH);
    }

    // Edit selected build
    private void editSelected() {

        PcBuild selectedBuild = buildList.getSelectedValue();

        if (selectedBuild == null) {
            return;
        }

        Window owner =
                SwingUtilities.getWindowAncestor(this);

        EditBuildDialog dialog = new EditBuildDialog(
                owner,
                facade,
                selectedBuild,
                availableComponents
        );

        dialog.setVisible(true);
    }

    // Load saved builds
    public void reloadBuilds() {

        try {
            List<PcBuild> builds = facade.getSavedBuilds();

            updateBuildList(builds);

        } catch (IOException exception) {

            listModel.clear();

            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            loadButton.setEnabled(false);

            countLabel.setText(
                    "Could not load saved builds"
            );

            detailsArea.setText(
                    exception.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Loading error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Observer callback
    public void updateBuildList(List<PcBuild> builds) {

        if (!SwingUtilities.isEventDispatchThread()) {

            SwingUtilities.invokeLater(
                    () -> updateBuildList(builds)
            );

            return;
        }

        PcBuild selected = buildList.getSelectedValue();

        String selectedId = selected == null
                ? null
                : selected.getId();

        listModel.clear();

        int selectionIndex = 0;

        for (int index = 0; index < builds.size(); index++) {

            PcBuild build = builds.get(index);

            listModel.addElement(build);

            if (build.getId().equals(selectedId)) {
                selectionIndex = index;
            }
        }

        countLabel.setText(
                "Saved builds: " + builds.size()
        );

        if (builds.isEmpty()) {
            showSelected();
        } else {
            buildList.setSelectedIndex(selectionIndex);
            showSelected();
        }
    }

    // Display selected build
    private void showSelected() {

        PcBuild build = buildList.getSelectedValue();

        editButton.setEnabled(build != null);
        deleteButton.setEnabled(build != null);
        loadButton.setEnabled(build != null);

        if (build == null) {

            detailsArea.setText("No build selected.");
            return;
        }

        StringBuilder text = new StringBuilder();

        text.append("Build: ")
                .append(build.getName())
                .append("\n\n");

        for (PcComponent component : build.getComponents()) {

            text.append(component.type())
                    .append(": ")
                    .append(component.name())
                    .append(" — ")
                    .append(component.price())
                    .append(" KZT\n");
        }

        int score = build.getComponents()
                .stream()
                .mapToInt(PcComponent::performanceScore)
                .sum();

        text.append("\nComponents price: ")
                .append(build.getTotalPrice())
                .append(" KZT\n");
        try {
            ServiceSelection services = facade.getBuildServices(build.getId());
            text.append("Optional services:\n");
            if (services.assembly()) text.append("Professional assembly: +15000 KZT\n");
            if (services.warranty()) text.append("Extended warranty: +20000 KZT\n");
            if (services.osInstallation()) text.append("OS installation: +10000 KZT\n");
            if (!services.assembly() && !services.warranty() && !services.osInstallation()) {
                text.append("None\n");
            }
            text.append("Final price with services: ")
                    .append(getFinalPrice(build))
                    .append(" KZT\n");
        } catch (IOException | ArithmeticException exception) {
            text.append("Could not load service prices: ")
                    .append(exception.getMessage())
                    .append("\n");
        }

        text.append("Demo performance score: ")
                .append(score)
                .append("\n");

        text.append("Estimated component power: ")
                .append(build.getEstimatedPower())
                .append(" W\n");

        text.append("\nBuild ID: ")
                .append(build.getId());

        detailsArea.setText(text.toString());
        detailsArea.setCaretPosition(0);
    }

    private long getFinalPrice(PcBuild build) throws IOException {
        ServiceSelection services = facade.getBuildServices(build.getId());
        long price = build.getTotalPrice();
        if (services.assembly()) price = Math.addExact(price, 15000);
        if (services.warranty()) price = Math.addExact(price, 20000);
        if (services.osInstallation()) price = Math.addExact(price, 10000);
        return price;
    }

    // Delete selected build
    private void deleteSelected() {

        PcBuild build = buildList.getSelectedValue();

        if (build == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete build \"" + build.getName() + "\"?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            // Observer automatically refreshes the list
            facade.deleteBuild(build.getId());

        } catch (IOException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not delete build:\n"
                            + exception.getMessage(),
                    "Deletion error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}