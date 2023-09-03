package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponDataLoaded;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.ProjectileSpecFile;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
public class WeaponsTreePanel extends CSVDataTreePanel<WeaponCSVEntry>{

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
    protected void setLoadedStatus() {
        GameDataRepository gameData = SettingsManager.getGameData();
        gameData.setWeaponsDataLoaded(true);
    }

    @Override
    protected void initWalkerListening() {
        EventBus.subscribe(event -> {
            if (event instanceof WeaponDataLoaded checked) {
                Map<Path, Map<String, WeaponCSVEntry>> weaponsByPackage = checked.weaponsByPackage();

                Map<String, List<WeaponCSVEntry>> weaponPackageList = new HashMap<>();
                for (Map.Entry<Path, Map<String, WeaponCSVEntry>> packageEntry : weaponsByPackage.entrySet()) {
                    Path packageEntryKey = packageEntry.getKey();
                    String packageName = packageEntryKey.toString();
                    Map<String, WeaponCSVEntry> weaponMap = packageEntry.getValue();

                    List<WeaponCSVEntry> weaponCSVEntries = new ArrayList<>(weaponMap.values());

                    weaponPackageList.put(packageName, weaponCSVEntries);
                }

                populateEntries(weaponPackageList);
            }
        });
    }

    @Override
    protected void updateEntryPanel(WeaponCSVEntry selected) {
        JPanel rightPanel = getRightPanel();
        rightPanel.removeAll();

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
            return "<html>" +
                    "<p>" + "Weapon ID: " + checked.getWeaponID() + "</p>" +
                    "</html>";
        }
        return null;
    }

    @Override
    protected Class<?> getEntryClass() {
        return WeaponCSVEntry.class;
    }

}
