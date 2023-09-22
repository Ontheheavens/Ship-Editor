package oth.shipeditor.components.datafiles.trees;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectWeaponDataEntry;
import oth.shipeditor.communication.events.files.WeaponTreeReloadQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@Log4j2
public class WeaponsTreePanel extends CSVDataTreePanel<WeaponCSVEntry>{

    private WeaponCSVEntry cachedEntry;

    private boolean filtersOpened;

    private boolean autoExpandNodes;

    public WeaponsTreePanel() {
        super("Weapon files");
    }

    @Override
    protected Action getLoadDataAction() {
        return FileUtilities.getLoadWeaponDataAction();
    }

    @Override
    protected String getEntryTypeName() {
        return "weapon";
    }

    @Override
    protected Map<String, WeaponCSVEntry> getRepository() {
        GameDataRepository gameData = SettingsManager.getGameData();
        return gameData.getAllWeaponEntries();
    }

    @Override
    protected JTree createCustomTree() {
        JTree custom = super.createCustomTree();
        custom.setCellRenderer(new WeaponsTreeCellRenderer());
        return custom;
    }

    /**
     * This could very well be a full-fledged panel stamp, with type icon borders and size icon too.
     * Unfortunately, there's not enough development time for that.
     */
    private static class WeaponsTreeCellRenderer extends DefaultTreeCellRenderer {

        @SuppressWarnings("ParameterHidesMemberVariable")
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object object = ((DefaultMutableTreeNode) value).getUserObject();
            if (object instanceof WeaponCSVEntry checked && leaf) {
                WeaponType hullSize = checked.getType();
                setIcon(ComponentUtilities.createIconFromColor(hullSize.getColor(), 10, 10));
            }
            return this;
        }

    }

    @Override
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setWeaponsDataLoaded(true);
    }

    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof WeaponTreeReloadQueued) {
                this.reload();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof SelectWeaponDataEntry checked) {
                WeaponCSVEntry entry = checked.entry();
                DefaultMutableTreeNode node = getNodeOfEntry(entry);
                if (node != null) {
                    JTree tree = this.getTree();
                    TreePath path = new TreePath(node.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                }
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    public void reload() {
        Map<String, List<WeaponCSVEntry>> weaponPackageList = WeaponFilterPanel.getFilteredEntries();
        populateEntries(weaponPackageList);

        JTree tree = getTree();

        if (autoExpandNodes) {
            this.expandAllNodes();
        }

        tree.repaint();
    }

    @Override
    void resetInfoPanel() {
        filtersOpened = false;
        super.resetInfoPanel();
    }

    protected JPanel createSearchContainer() {
        JPanel searchContainer = new JPanel(new GridBagLayout());
        searchContainer.setBorder(new EmptyBorder(0, 0, 0, 0));

        JTextField searchField = new JTextField();
        searchField.setToolTipText("Input is checked against displayed name and weapon ID as a substring.");

        Document document = searchField.getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                WeaponFilterPanel.setCurrentTextFilter(searchField.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                WeaponFilterPanel.setCurrentTextFilter(searchField.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                WeaponFilterPanel.setCurrentTextFilter(searchField.getText());
            }
        });
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        searchContainer.add(searchField, gridBagConstraints);
        JButton searchButton = new JButton(StringValues.SEARCH);
        searchButton.addActionListener(e -> this.reload());
        searchContainer.add(searchButton);
        return searchContainer;
    }

    @Override
    protected JPanel createTopPanel() {
        JPanel topPanel = super.createTopPanel();

        JButton filtersButton = new JButton(StringValues.FILTERS);
        filtersButton.addActionListener(e -> {
            if (filtersOpened) {
                if (cachedEntry != null) {
                    updateEntryPanel(cachedEntry);
                } else {
                    resetInfoPanel();
                }
                filtersOpened = false;
            } else {
                JPanel rightPanel = getRightPanel();
                rightPanel.removeAll();

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.insets = new Insets(0, 0, 0, 0);

                JPanel panel = new WeaponFilterPanel();
                rightPanel.add(panel, constraints);

                filtersOpened = true;
                rightPanel.revalidate();
                rightPanel.repaint();
            }
        });

        topPanel.add(filtersButton);

        JCheckBox expandNodes = new JCheckBox("Auto-expand nodes");
        expandNodes.addActionListener(e -> this.autoExpandNodes = expandNodes.isSelected());

        topPanel.add(expandNodes);

        return topPanel;
    }

    @Override
    protected void loadAllEntries(Map<String, List<WeaponCSVEntry>> entries) {
        if (entries == null || entries.isEmpty()) {
            log.info("No entries registered: input empty.");
            return;
        }

        int nodeCount = 0;

        for (Map.Entry<String, List<WeaponCSVEntry>> entry : entries.entrySet()) {
            Path folderPath = Paths.get(entry.getKey(), "");
            DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode(folderPath.getFileName().toString());

            List<WeaponCSVEntry> entriesInPackage = entry.getValue();
            for (WeaponCSVEntry dataEntry : entriesInPackage) {
                MutableTreeNode node = new DefaultMutableTreeNode(dataEntry);
                nodeCount++;
                packageRoot.add(node);
            }
            DefaultMutableTreeNode rootNode = getRootNode();
            rootNode.add(packageRoot);
        }
        log.info("Total {} {} entry nodes shown.", nodeCount, getEntryTypeName());
        setLoadedStatus();
    }

    @Override
    protected void updateEntryPanel(WeaponCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();

        filtersOpened = false;

        GridBagConstraints constraints = DataTreePanel.getDefaultConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 5, 0, 5);
        Sprite sprite = selected.getWeaponImage();
        if (sprite != null) {
            String tooltip = Utility.getTooltipForSprite(sprite);
            JLabel spriteIcon = ComponentUtilities.createIconFromImage(sprite.image(), tooltip, 128);
            JPanel iconPanel = new JPanel();
            iconPanel.add(spriteIcon);
            rightPanel.add(iconPanel, constraints);
        }

        JPanel specFilePanel = new JPanel();
        specFilePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        ComponentUtilities.outfitPanelWithTitle(specFilePanel, new Insets(1, 0, 0, 0),
                StringValues.FILES);
        specFilePanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel labelContainer = new JPanel();
        labelContainer.setAlignmentX(LEFT_ALIGNMENT);
        labelContainer.setBorder(new EmptyBorder(2, 0, 0, 0));
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

        WeaponSpecFile specFile = selected.getSpecFile();
        labelContainer.add(WeaponsTreePanel.createWeaponFileLabel(specFile));

        var projectileSpecFile = GameDataRepository.getProjectileByID(specFile.getProjectileSpecId());
        if (projectileSpecFile != null) {
            labelContainer.add(Box.createVerticalStrut(2));
            labelContainer.add(WeaponsTreePanel.createProjectileFileLabel(projectileSpecFile));
        }

        WeaponSprites sprites = selected.getSprites();
        WeaponsTreePanel.populateSpriteFileLabels(labelContainer, sprites);

        specFilePanel.add(labelContainer);
        constraints.gridy = 1;
        rightPanel.add(specFilePanel, constraints);

        Map<String, String> data = selected.getRowData();
        createRightPanelDataTable(data);

        cachedEntry = selected;

        FeaturesOverseer.setWeaponForInstall(selected);
    }

    private static void populateSpriteFileLabels(JPanel labelContainer, WeaponSprites sprites) {
        Sprite turretSprite = sprites.getTurretSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, turretSprite, "Turret sprite: ");
        Sprite turretGunSprite = sprites.getTurretGunSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, turretGunSprite, "Turret gun sprite: ");
        Sprite turretGlowSprite = sprites.getTurretGlowSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, turretGlowSprite, "Turret glow sprite: ");
        Sprite turretUnderSprite = sprites.getTurretUnderSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, turretUnderSprite, "Turret under sprite: ");

        Sprite hardpointSprite = sprites.getHardpointSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, hardpointSprite, "Hardpoint sprite: ");
        Sprite hardpointGunSprite = sprites.getHardpointGunSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, hardpointGunSprite, "Hardpoint gun sprite: ");
        Sprite hardpointGlowSprite = sprites.getHardpointGlowSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, hardpointGlowSprite, "Hardpoint glow sprite: ");
        Sprite hardpointUnderSprite = sprites.getHardpointUnderSprite();
        WeaponsTreePanel.addSpriteLabel(labelContainer, hardpointUnderSprite, "Hardpoint under sprite: ");
    }

    private static void addSpriteLabel(JPanel labelContainer, Sprite sprite, String description) {
        if (sprite != null) {
            JLabel label = WeaponsTreePanel.createFileLabel(sprite.path(), description);
            labelContainer.add(Box.createVerticalStrut(2));
            labelContainer.add(label);
        }
    }

    private static JLabel createFileLabel(Path path, String description) {
        JLabel label = new JLabel(description + path.getFileName());
        label.setToolTipText(String.valueOf(path));
        label.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        JPopupMenu pathContextMenu = ComponentUtilities.createPathContextMenu(path);
        label.addMouseListener(new MouseoverLabelListener(pathContextMenu, label));
        return label;
    }

    private static JLabel createWeaponFileLabel(WeaponSpecFile weaponSpecFile) {
        Path weaponSpecFilePath = weaponSpecFile.getWeaponSpecFilePath();
        return WeaponsTreePanel.createFileLabel(weaponSpecFilePath, "Weapon file : ");
    }

    private static JLabel createProjectileFileLabel(ProjectileSpecFile projectileSpecFile) {
        Path projectileSpecFilePath = projectileSpecFile.getProjectileSpecFilePath();
        return WeaponsTreePanel.createFileLabel(projectileSpecFilePath, "Projectile file : ");
    }

    @Override
    JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
        if (cachedSelectForMenu.getUserObject() instanceof WeaponCSVEntry) {
            menu.addSeparator();
            JMenuItem loadAsLayer = new JMenuItem("Load as weapon layer");
            loadAsLayer.addActionListener(new LoadWeaponLayerFromTree());
            menu.add(loadAsLayer);
        }
        return menu;
    }

    private class LoadWeaponLayerFromTree extends AbstractAction {
        @Override
        public boolean isEnabled() {
            DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
            return super.isEnabled() && cachedSelectForMenu.getUserObject() instanceof WeaponCSVEntry;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode cachedSelectForMenu = getCachedSelectForMenu();
            if (cachedSelectForMenu.getUserObject() instanceof WeaponCSVEntry checked) {
                checked.loadLayerFromEntry();
            }
        }
    }

    @Override
    protected WeaponCSVEntry getObjectFromNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (!(userObject instanceof WeaponCSVEntry checked)) return null;
        return checked;
    }

    @Override
    protected String getTooltipForEntry(Object entry) {
        if(entry instanceof WeaponCSVEntry checked) {
            String weaponID = "Weapon ID: " + checked.getWeaponID();
            WeaponType weaponType = checked.getType();
            String type =  "Weapon type: " + weaponType.getDisplayName();
            WeaponSize weaponSize = checked.getSize();
            String size =  "Weapon size: " + weaponSize.getDisplayName();
            return Utility.getWithLinebreaks(weaponID, type, size);
        }
        return null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return WeaponCSVEntry.class;
    }

}
