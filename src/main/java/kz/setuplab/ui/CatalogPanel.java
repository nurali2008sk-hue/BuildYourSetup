package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.model.ComponentType;
import kz.setuplab.model.PcComponent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class CatalogPanel extends JPanel {
    private final ComponentCatalog catalog;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> categoryBox;
    private final JLabel countLabel;

    public CatalogPanel(ComponentCatalog catalog) {
        this.catalog = catalog;

        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        categoryBox = new JComboBox<>();
        categoryBox.addItem("All categories");

        for (ComponentType type : ComponentType.values()) {
            categoryBox.addItem(type.toString());
        }

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Category:"));
        filters.add(categoryBox);

        String[] columns = {
                "Name", "Category", "Price (KZT)",
                "Socket", "Memory type", "Power (W)", "Score"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 2 -> Long.class;
                    case 5, 6 -> Integer.class;
                    default -> String.class;
                };
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);

        countLabel = new JLabel();

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(countLabel, BorderLayout.WEST);
        footer.add(
                new JLabel("Demo data. PSU power means output capacity."),
                BorderLayout.EAST
        );

        add(filters, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        categoryBox.addActionListener(event -> refreshTable());

        refreshTable();
    }

    private void refreshTable() {
        int selectedIndex = categoryBox.getSelectedIndex();

        List<PcComponent> visibleComponents;

        if (selectedIndex == 0) {
            visibleComponents = catalog.getAll();
        } else {
            ComponentType type = ComponentType.values()[selectedIndex - 1];
            visibleComponents = catalog.findByType(type);
        }

        tableModel.setRowCount(0);

        for (PcComponent component : visibleComponents) {
            tableModel.addRow(new Object[]{
                    component.name(),
                    component.type().toString(),
                    component.price(),
                    component.socket(),
                    component.memoryType(),
                    component.powerWatts(),
                    component.performanceScore()
            });
        }

        countLabel.setText("Components: " + visibleComponents.size());
    }
}
