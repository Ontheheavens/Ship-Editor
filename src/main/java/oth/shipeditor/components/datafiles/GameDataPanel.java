package oth.shipeditor.components.datafiles;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.Skin;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    private JPanel entryDataPanel;

    private JPanel rightPanel;

    private JScrollPane dataScrollContainer;

    public GameDataPanel() {
        this.setLayout(new BorderLayout());
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Game data"));
        JButton loadCSVButton = new JButton(FileUtilities.getLoadGameDataAction());
        loadCSVButton.setText("Load ship data");
        topContainer.add(loadCSVButton);
        this.add(topContainer, BorderLayout.PAGE_START);
        JSplitPane splitPane = createContentSplitter();
        this.add(splitPane, BorderLayout.CENTER);
    }

    private JSplitPane createContentSplitter() {
        HullsTree hullsTree = new HullsTree(this);
        entryDataPanel = new JPanel();
        entryDataPanel.add(new JLabel("Entry Content"));
        entryDataPanel.setLayout(new BoxLayout(entryDataPanel, BoxLayout.PAGE_AXIS));
        entryDataPanel.setAlignmentX(CENTER_ALIGNMENT);
        entryDataPanel.setBorder(BorderFactory.createEmptyBorder(2,6, 2, 2));
        dataScrollContainer = new JScrollPane(entryDataPanel);
        dataScrollContainer.setBorder(BorderFactory.createEmptyBorder());
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        rightPanel.add(dataScrollContainer);
        JSplitPane treeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeSplitter.setOneTouchExpandable(true);
        treeSplitter.setResizeWeight(0.4f);
        treeSplitter.setLeftComponent(hullsTree);
        treeSplitter.setRightComponent(rightPanel);
        return treeSplitter;
    }

    void resetInfoPanel() {
        rightPanel.removeAll();
    }

    void updateEntryPanel(ShipCSVEntry selected) {
        rightPanel.removeAll();
        Map<String, Skin> skins = selected.getSkins();
        if (skins != null) {
            Collection<Skin> values = skins.values();
            Skin[] skinArray = values.toArray(new Skin[0]);
            JComboBox<Skin> skinChooser = new JComboBox<>(skinArray);
            skinChooser.setSelectedItem(selected.getActiveSkin());
            skinChooser.addActionListener(e -> {
                Skin chosen = (Skin) skinChooser.getSelectedItem();
                selected.setActiveSkin(chosen);
                updateEntryPanel(selected);
            });
            rightPanel.add(skinChooser);
            Skin activeSkin = selected.getActiveSkin();
            if (activeSkin != null && !activeSkin.isBase()) {
                JPanel skinPanel = new JPanel();
                skinPanel.setLayout(new BoxLayout(skinPanel, BoxLayout.PAGE_AXIS));
                skinPanel.add(new JLabel(activeSkin.getHullName()));
                skinPanel.add(new JLabel(activeSkin.getDescriptionPrefix()));
                rightPanel.add(skinPanel);
            }
        } else {
            rightPanel.removeAll();
        }
        rightPanel.add(dataScrollContainer);
        entryDataPanel.removeAll();
        Map<String, String> data = selected.getRowData();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            JLabel keyLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
            entryDataPanel.add(keyLabel);
        }
        rightPanel.revalidate();
        rightPanel.repaint();
    }

}
