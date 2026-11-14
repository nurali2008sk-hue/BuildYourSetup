
package kz.setuplab.ui;

import kz.setuplab.data.ComponentCatalog;
import kz.setuplab.facade.SetupFacade;
import kz.setuplab.observer.SavedBuildsObserver;
import kz.setuplab.observer.NotificationObserver;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

public class MainFrame extends JFrame {

    public MainFrame() {

        setTitle("Build Your Setup");
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ComponentCatalog catalog = new ComponentCatalog();
        SetupFacade facade = new SetupFacade(catalog);

        JPanel root = new JPanel(new BorderLayout(0, 16));

        root.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel title = new JLabel("Build Your Setup");

        title.setFont(
                new Font(Font.SANS_SERIF, Font.BOLD, 28)
        );

        JTabbedPane tabs = new JTabbedPane();

        BuildPanel buildPanel =
                new BuildPanel(catalog, facade);

        // Pass all available components to the editor
        SavedBuildsPanel savedBuildsPanel =
                new SavedBuildsPanel(
                        facade,
                        catalog.getAll()
                );

        // Register Observer
        SavedBuildsObserver savedBuildsObserver =
                new SavedBuildsObserver(
                        facade,
                        savedBuildsPanel::updateBuildList
                );

        facade.addObserver(savedBuildsObserver);
        facade.addObserver(new NotificationObserver());

        tabs.addTab(
                "Component Catalog",
                new CatalogPanel(catalog)
        );

        tabs.addTab(
                "Build Configurator",
                buildPanel
        );

        tabs.addTab(
                "Saved Builds",
                savedBuildsPanel
        );

        tabs.addChangeListener(event -> {

            if (tabs.getSelectedComponent() == savedBuildsPanel) {
                savedBuildsPanel.reloadBuilds();
            }
        });

        JLabel status = new JLabel(
                "Create, configure and save your PC builds."
        );

        root.add(title, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
