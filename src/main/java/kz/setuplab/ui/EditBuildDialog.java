
package kz.setuplab.ui;

import kz.setuplab.facade.SetupFacade;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcBuild;
import kz.setuplab.model.PcComponent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class EditBuildDialog extends JDialog {

    private final SetupFacade facade;
    private final PcBuild build;

    private final JComboBox<ComponentType> typeBox =
            new JComboBox<>(ComponentType.values());

    private final JComboBox<PcComponent> componentBox =
            new JComboBox<>();

    private final List<PcComponent> availableComponents;

    public EditBuildDialog(
            Window owner,
            SetupFacade facade,
            PcBuild build,
            List<PcComponent> availableComponents
    ) {
        super(owner, "Edit PC Build", ModalityType.APPLICATION_MODAL);

        this.facade = facade;
        this.build = build;
        this.availableComponents = availableComponents;

        setSize(500, 250);
        setLocationRelativeTo(owner);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Component category:"));
        add(typeBox);

        add(new JLabel("New component:"));
        add(componentBox);

        JButton saveButton = new JButton("Save changes");
        JButton cancelButton = new JButton("Cancel");

        add(saveButton);
        add(cancelButton);

        typeBox.addActionListener(event -> loadComponents());

        saveButton.addActionListener(event -> saveChanges());

        cancelButton.addActionListener(event -> dispose());

        componentBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof PcComponent component) {
                    label.setText(
                            component.name() + " — "
                                    + component.price() + " KZT"
                    );
                }

                return label;
            }
        });

        loadComponents();
    }

    private void loadComponents() {

        componentBox.removeAllItems();

        ComponentType selectedType =
                (ComponentType) typeBox.getSelectedItem();

        if (selectedType == null) {
            return;
        }

        for (PcComponent component : availableComponents) {

            if (component.type() == selectedType) {
                componentBox.addItem(component);
            }
        }

        if (componentBox.getItemCount() > 0) {
            PcComponent current = build.getComponent(selectedType);

            for (int i = 0; i < componentBox.getItemCount(); i++) {
                PcComponent candidate = componentBox.getItemAt(i);

                if (candidate.equals(current)) {
                    componentBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveChanges() {

        PcComponent selected =
                (PcComponent) componentBox.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a component.",
                    "Validation error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            facade.updateBuild(build.getId(), selected);

            JOptionPane.showMessageDialog(
                    this,
                    "Build updated successfully!"
            );

            dispose();

        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Update error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
