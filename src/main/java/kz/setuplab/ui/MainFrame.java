package kz.setuplab.ui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Build Your Setup");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Build Your Setup");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Component Catalog", createPlaceholder("Component catalog"));
        tabs.addTab("Build Configurator", createPlaceholder("PC build configurator"));
        tabs.addTab("Saved Builds", createPlaceholder("Your saved builds"));

        JLabel status = new JLabel("Ready. Select a tab to get started.");

        root.add(title, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createPlaceholder(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}