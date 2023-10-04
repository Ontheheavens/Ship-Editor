package oth.shipeditor.components.datafiles.entities;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.components.viewer.entities.weapon.OffsetPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainterInitialization;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponRenderOrdering;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.components.viewer.painters.points.weapon.ProjectilePainter;
import oth.shipeditor.parsing.loading.FileLoading;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.*;
import oth.shipeditor.utility.objects.Size2D;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 05.08.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Getter
public class WeaponCSVEntry implements CSVEntry, InstallableEntry {

    private final Map<String, String> rowData;

    private final Path packageFolderPath;

    private final Path tableFilePath;

    private final String weaponID;

    private WeaponSpecFile specFile;

    private WeaponSprites sprites;

    private Sprite weaponImage;

    public WeaponCSVEntry(Map<String, String> row, Path folder, Path tablePath) {
        this.rowData = row;
        packageFolderPath = folder;
        this.tableFilePath = tablePath;
        weaponID = this.rowData.get("id");
    }

    @Override
    public String getID() {
        return weaponID;
    }

    public void setSpecFile(WeaponSpecFile weaponSpecFile) {
        this.specFile = weaponSpecFile;
    }

    public WeaponType getType() {
        WeaponType mountTypeOverride = specFile.getMountTypeOverride();
        if (mountTypeOverride != null) {
            return mountTypeOverride;
        } else {
            return specFile.getType();
        }
    }

    public int getOPCost() {
        var data = this.getRowData();
        String costText = data.get("OPs");
        if (costText == null|| costText.isEmpty()) return 0;
        return Integer.parseInt(costText);
    }

    public int getDrawOrder() {
        WeaponSize specFileSize = getSize();
        return specFileSize.getNumericSize();
    }

    public WeaponSize getSize() {
        return specFile.getSize();
    }

    public WeaponSprites getSprites() {
        if (sprites == null) {
            WeaponSprites spriteHolder = new WeaponSprites();
            WeaponSpecFile weaponSpecFile = this.getSpecFile();

            String turretSprite = weaponSpecFile.getTurretSprite();
            setSpecSpriteFromPath(turretSprite, spriteHolder::setTurretSprite);
            String turretGunSprite = weaponSpecFile.getTurretGunSprite();
            setSpecSpriteFromPath(turretGunSprite, spriteHolder::setTurretGunSprite);
            String turretGlowSprite = weaponSpecFile.getTurretGlowSprite();
            setSpecSpriteFromPath(turretGlowSprite, spriteHolder::setTurretGlowSprite);
            String turretUnderSprite = weaponSpecFile.getTurretUnderSprite();
            setSpecSpriteFromPath(turretUnderSprite, spriteHolder::setTurretUnderSprite);

            String hardpointSprite = weaponSpecFile.getHardpointSprite();
            setSpecSpriteFromPath(hardpointSprite, spriteHolder::setHardpointSprite);
            String hardpointGunSprite = weaponSpecFile.getHardpointGunSprite();
            setSpecSpriteFromPath(hardpointGunSprite, spriteHolder::setHardpointGunSprite);
            String hardpointGlowSprite = weaponSpecFile.getHardpointGlowSprite();
            setSpecSpriteFromPath(hardpointGlowSprite, spriteHolder::setHardpointGlowSprite);
            String hardpointUnderSprite = weaponSpecFile.getHardpointUnderSprite();
            setSpecSpriteFromPath(hardpointUnderSprite, spriteHolder::setHardpointUnderSprite);

            sprites = spriteHolder;
        }
        return sprites;
    }

    public Sprite getWeaponImage() {
        if (weaponImage == null) {
            WeaponSprites spriteHolder = this.getSprites();

            var turretSprite = spriteHolder.getTurretSprite();
            if (turretSprite == null) return null;
            BufferedImage turretMain = turretSprite.image();

            var turretGunSprite = spriteHolder.getTurretGunSprite();
            BufferedImage turretGun = null;
            if (turretGunSprite != null) {
                turretGun = turretGunSprite.image();
            }

            BufferedImage combinedImage = new BufferedImage(turretMain.getWidth(), turretMain.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = combinedImage.createGraphics();

            boolean barrelsBelow = false;
            var renderHints = specFile.getRenderHints();
            if (renderHints != null && !renderHints.isEmpty()) {
                if (renderHints.contains(StringConstants.RENDER_BARREL_BELOW)) {
                    barrelsBelow = true;
                }
            }

            if (barrelsBelow) {
                if (turretGun != null) {
                    g2d.drawImage(turretGun, 0, 0, null);
                }
                g2d.drawImage(turretMain, 0, 0, null);
            } else {
                g2d.drawImage(turretMain, 0, 0, null);
                if (turretGun != null) {
                    g2d.drawImage(turretGun, 0, 0, null);
                }
            }

            g2d.dispose();
            weaponImage = new Sprite(combinedImage, turretSprite.path(), turretSprite.name());
        }
        return weaponImage;
    }

    public void loadLayerFromEntry() {
        String turretSprite = this.specFile.getTurretSprite();

        if (turretSprite == null || turretSprite.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Layer initialization failed, sprite file not defined for: " + this.getWeaponID(),
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Path spriteFilePath = Path.of(turretSprite);
        File spriteFile = FileLoading.fetchDataFile(spriteFilePath, this.packageFolderPath);

        if (spriteFile == null) {
            JOptionPane.showMessageDialog(null,
                    "Layer initialization failed, sprite file not found for: " + this.getWeaponID(),
                    StringValues.FILE_LOADING_ERROR,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Sprite sprite = FileLoading.loadSprite(spriteFile);

        var manager = StaticController.getLayerManager();
        WeaponLayer newLayer = manager.createWeaponLayer();
        newLayer.setSpecFile(specFile);

        WeaponPainter weaponPainter = createPainterFromEntry(newLayer, specFile);
        newLayer.setPainter(weaponPainter);

        manager.setActiveLayer(newLayer);
        var viewer = StaticController.getViewer();
        viewer.loadLayer(newLayer, sprite);

        EventBus.publish(new ActiveLayerUpdated(newLayer));
    }

    /**
     * @param layer can be null.
     */
    @SuppressWarnings("WeakerAccess")
    public WeaponPainter createPainterFromEntry(WeaponLayer layer, WeaponSpecFile weaponSpecFile) {
        WeaponPainter weaponPainter = new WeaponPainter(layer);
        var spriteHolder = this.getSprites();
        weaponPainter.setWeaponSprites(spriteHolder);
        weaponPainter.setWeaponID(weaponSpecFile.getId());

        if (weaponSpecFile.isRenderBelowAllWeapons()) {
            weaponPainter.setRenderOrderType(WeaponRenderOrdering.BELOW_ALL);
        } else if (weaponSpecFile.isRenderAboveAllWeapons()) {
            weaponPainter.setRenderOrderType(WeaponRenderOrdering.ABOVE_ALL);
        } else {
            weaponPainter.setRenderOrderType(WeaponRenderOrdering.NORMAL);
        }

        var hardpointOffsets = weaponSpecFile.getHardpointOffsets();
        var hardpointAngles = weaponSpecFile.getHardpointAngleOffsets();
        WeaponCSVEntry.initializeOffsets(weaponPainter, WeaponMount.HARDPOINT,
                hardpointOffsets, hardpointAngles);

        var turretOffsets = weaponSpecFile.getTurretOffsets();
        var turretAngles = weaponSpecFile.getTurretAngleOffsets();
        WeaponCSVEntry.initializeOffsets(weaponPainter, WeaponMount.TURRET,
                turretOffsets, turretAngles);

        var renderHints = specFile.getRenderHints();
        if (renderHints != null && !renderHints.isEmpty()) {
            List<WeaponRenderHints> hintEnums = new ArrayList<>();
            renderHints.forEach(hintText -> hintEnums.add(WeaponRenderHints.valueOf(hintText)));
            weaponPainter.setRenderHints(hintEnums);
        }

        ProjectileSpecFile projectileSpec = GameDataRepository.getProjectileByID(specFile.getProjectileSpecId());
        if (projectileSpec != null) {
            String sprite = projectileSpec.getSprite();
            if (sprite != null && !sprite.isEmpty()) {
                Path projectileSpecSpritePath = Path.of(sprite);
                Path containingPackage = projectileSpec.getContainingPackage();
                File file = FileLoading.fetchDataFile(projectileSpecSpritePath, containingPackage);
                Sprite projectileSprite = FileLoading.loadSprite(file);

                double spriteWidth = projectileSpec.getSize()[0];
                double spriteHeight = projectileSpec.getSize()[1];
                ProjectilePainter projectilePainter = new ProjectilePainter(projectileSprite,
                        projectileSpec.getCenter(), new Size2D(spriteWidth, spriteHeight));
                weaponPainter.setProjectilePainter(projectilePainter);
            }
        }

        Sprite loadedTurretSprite = spriteHolder.getTurretSprite();
        if (loadedTurretSprite != null) {
            weaponPainter.setSprite(loadedTurretSprite.image());
        }
        return weaponPainter;
    }

    private static void initializeOffsets(WeaponPainter painter, WeaponMount mount,
                                          Point2D[] offsetPoints, double[] offsetAngles) {
        int length = offsetPoints.length;
        painter.setMount(mount);
        var offsetPainter = painter.getOffsetPainter();
        for (int i = 0; i < length; i++) {
            Point2D offset = offsetPoints[i];
            Point2D rotated = ShipPainterInitialization.rotatePointByCenter(offset,
                    painter.getEntityCenter());
            OffsetPoint initialized = new OffsetPoint(rotated, painter);
            if (offsetAngles.length > i) {
                initialized.setAngle(offsetAngles[i]);
            }

            offsetPainter.addPoint(initialized);
        }
    }

    private void setSpecSpriteFromPath(String pathInPackage, Consumer<Sprite> setter) {
        Utility.setSpriteFromPath(pathInPackage, setter, this.packageFolderPath);
    }

    public JPanel createPickedWeaponPanel() {
        JPanel weaponPickPanel = new JPanel();
        weaponPickPanel.setLayout(new BoxLayout(weaponPickPanel, BoxLayout.LINE_AXIS));
        weaponPickPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        WeaponSize weaponSize = this.getSize();
        JLabel sizeLabel = new JLabel(weaponSize.getIcon());
        sizeLabel.setToolTipText(weaponSize.getDisplayName());
        weaponPickPanel.add(sizeLabel);

        WeaponType weaponType = this.getType();
        JLabel typeLabel = ComponentUtilities.createColorIconLabel(weaponType.getColor());
        typeLabel.setToolTipText(weaponType.getDisplayName());
        weaponPickPanel.add(typeLabel);

        JLabel text = new JLabel(this.toString());
        text.setBorder(new EmptyBorder(0, 4, 0, 0));
        weaponPickPanel.add(text);

        Insets insets = new Insets(1, 0, 0, 0);
        ComponentUtilities.outfitPanelWithTitle(weaponPickPanel, insets, "Picked weapon");

        return weaponPickPanel;
    }

    @Override
    public String toString() {
        String displayedName = rowData.get(StringConstants.NAME);
        if (displayedName.isEmpty()) {
            displayedName = this.getWeaponID();
        }
        return displayedName;
    }

}
