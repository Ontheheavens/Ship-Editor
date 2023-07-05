package oth.shipeditor.components.datafiles;

import com.formdev.flatlaf.ui.FlatLineBorder;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.Hull;
import oth.shipeditor.representation.Skin;
import oth.shipeditor.utility.StringConstants;
import oth.shipeditor.utility.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Ontheheavens
 * @since 18.06.2023
 */
@Log4j2
public class GameDataPanel extends JPanel {

    public static final String NO_ENTRY_SELECTED = "No entry selected";
    private JPanel entryDataPanel;

    private JPanel rightPanel;

    public GameDataPanel() {
        this.setLayout(new BorderLayout());
        JPanel topContainer = new JPanel();
        topContainer.add(new JLabel("Game data"));
        JButton loadCSVButton = new JButton(FileUtilities.getLoadGameDataAction());
        loadCSVButton.setText("Reload ship data");
        loadCSVButton.setToolTipText("Reload all ship, skin and variant files, grouped by package");
        topContainer.add(loadCSVButton);
        this.add(topContainer, BorderLayout.PAGE_START);
        JSplitPane splitPane = createContentSplitter();
        this.add(splitPane, BorderLayout.CENTER);
    }

    private JSplitPane createContentSplitter() {
        HullsTree hullsTree = new HullsTree(this);
        entryDataPanel = new JPanel();
        entryDataPanel.setLayout(new BoxLayout(entryDataPanel, BoxLayout.PAGE_AXIS));
        entryDataPanel.setAlignmentX(CENTER_ALIGNMENT);
        entryDataPanel.setBorder(BorderFactory.createEmptyBorder(0,0, 0, 0));
        rightPanel = new JPanel(new GridBagLayout());
        rightPanel.add(entryDataPanel, getDefault());
        rightPanel.add(new JLabel(NO_ENTRY_SELECTED));
        JSplitPane treeSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        treeSplitter.setOneTouchExpandable(true);
        treeSplitter.setResizeWeight(0.4f);
        treeSplitter.setLeftComponent(hullsTree);
        treeSplitter.setRightComponent(rightPanel);
        return treeSplitter;
    }

    private GridBagConstraints getDefault() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, 0, 0, 0);
        return constraints;
    }

    void resetInfoPanel() {
        rightPanel.removeAll();
        rightPanel.add(new JLabel(NO_ENTRY_SELECTED));
    }

    private JPanel createShipFilesPanel(ShipCSVEntry selected) {
        JPanel shipFilesPanel = new JPanel();
        shipFilesPanel.setLayout(new BoxLayout(shipFilesPanel, BoxLayout.PAGE_AXIS));
        GridBagConstraints constraints = getDefault();
        Map<String, String> rowData = selected.getRowData();
        String shipName = rowData.get(StringConstants.NAME);
        String shipId = selected.getHullID();
        String hullFileName = selected.getHullFileName();
        Hull hullFile = selected.getHullFile();
        String spriteFileName = hullFile.getSpriteName();

        String skinFileName = "";
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
            skinChooser.setAlignmentX(CENTER_ALIGNMENT);

            constraints.insets = new Insets(0, 0, 0, 0);
            rightPanel.add(skinChooser, constraints);
            Skin activeSkin = selected.getActiveSkin();
            if (activeSkin != null && !activeSkin.isBase()) {
                shipName = activeSkin.getHullName();
                shipId = activeSkin.getSkinHullId();
                spriteFileName = activeSkin.getSpriteName();
                skinFileName = Utility.getSkinFileName(selected, activeSkin);
            }
        } else {
            rightPanel.removeAll();
        }
        shipFilesPanel.add(new JLabel("Ship name: " + shipName));
        shipFilesPanel.add(new JLabel("Ship ID: " + shipId));
        shipFilesPanel.add(new JLabel("Hull file : " + hullFileName));
        File spriteFile = new File(spriteFileName);
        shipFilesPanel.add(new JLabel("Sprite file: : " + spriteFile.getName()));
        if (!skinFileName.isEmpty()) {
            shipFilesPanel.add(new JLabel("Skin file: " + skinFileName));
        }
        return shipFilesPanel;
    }

    void updateEntryPanel(ShipCSVEntry selected) {
        rightPanel.removeAll();
        GridBagConstraints constraints = getDefault();
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 0, 5);
        JPanel shipFilesPanel = createShipFilesPanel(selected);
        rightPanel.add(shipFilesPanel, constraints);
        entryDataPanel.removeAll();
        Map<String, String> data = selected.getRowData();
        JScrollPane tableContainer = GameDataPanel.createTableFromMap(data);
        GridBagConstraints otherConstraints = new GridBagConstraints();
        otherConstraints.gridx = 0;
        otherConstraints.gridy = 2;
        otherConstraints.fill = GridBagConstraints.BOTH;
        otherConstraints.weightx = 1.0;
        otherConstraints.weighty = 1.0;
        otherConstraints.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(tableContainer, otherConstraints);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private static JScrollPane createTableFromMap(Map<String, String> data) {
        Set<Map.Entry<String, String>> entries = data.entrySet();
        Object[][] tableData = entries.stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .toArray(Object[][]::new);
        DefaultTableModel model = new DefaultTableModel(tableData, new Object[]{"Property", "Value"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make the table read-only.
            }
        };
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }

}
