package oth.shipeditor.components.layering;

import lombok.extern.log4j.Log4j2;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SelectShipDataEntry;
import oth.shipeditor.communication.events.components.WindowRepaintQueued;
import oth.shipeditor.communication.events.files.HullFileOpened;
import oth.shipeditor.communication.events.files.saving.HullSaveQueued;
import oth.shipeditor.communication.events.files.saving.VariantSaveQueued;
import oth.shipeditor.communication.events.viewer.layers.ActiveLayerUpdated;
import oth.shipeditor.communication.events.viewer.layers.LayerRemovalQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.ViewerLayerRemovalConfirmed;
import oth.shipeditor.communication.events.viewer.layers.ships.ShipLayerCreated;
import oth.shipeditor.communication.events.viewer.layers.weapons.WeaponLayerCreated;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.viewer.PaintOrderController;
import oth.shipeditor.components.viewer.PrimaryViewer;
import oth.shipeditor.components.viewer.layers.LayerManager;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.weapon.WeaponLayer;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponSprites;
import oth.shipeditor.menubar.LayersMenu;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.parsing.loading.OpenSpriteAction;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.HullSpecFile;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSpecFile;
import oth.shipeditor.utility.components.containers.SortableTabbedPane;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;
import oth.shipeditor.utility.themes.Themes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

/**
 * @author Ontheheavens
 * @since 01.06.2023
 */
@SuppressWarnings("OverlyCoupledClass")
@Log4j2
public final class ViewerLayersPanel extends SortableTabbedPane {

    private static final String LAYER = "Layer #";

    /**
     * Expected to be the same instance that is originally created and assigned in viewer;
     * Reference in this class is present for both conceptual and convenience purposes.
     */
    private final LayerManager layerManager;

    private final Map<ViewerLayer, LayerTab> tabIndex;

    public ViewerLayersPanel(LayerManager manager) {
        this.layerManager = manager;
        this.tabIndex = new HashMap<>();

        this.putClientProperty("JTabbedPane.tabClosable", true);
        this.putClientProperty("JTabbedPane.tabCloseToolTipText", "Remove this layer");
        this.putClientProperty( "JTabbedPane.tabCloseCallback", (IntConsumer) index -> {
            LayerTab tab = (LayerTab) getComponentAt(index);
            ViewerLayer layer = getLayerByTab(tab);
            EventBus.publish(new LayerRemovalQueued(layer));
        });

        this.initLayerListeners();
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.addChangeListener(event -> {
            ViewerLayer newlySelected = getLayerByTab((LayerTab) getSelectedComponent());
            log.trace("Layer panel change!");
            // If the change results from the last layer being removed and the newly selected layer is null,
            // call to set active layer is unnecessary as this case is handled directly by layer manager.
            if (newlySelected != null) {
                layerManager.setActiveLayer(newlySelected);
            }
        });
        this.addMouseListener(new TabContextListener());
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,  Themes.getBorderColor()));
    }

    @SuppressWarnings({"OverlyCoupledMethod", "ChainOfInstanceofChecks"})
    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof ShipLayerCreated checked) {
                ShipLayer layer = checked.newLayer();
                Icon tabIcon = FontIcon.of(BoxiconsRegular.ROCKET, 20, Themes.getIconColor());
                ShipLayerTab created = new ShipLayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + (getTabCount() + 1), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
            else if (event instanceof WeaponLayerCreated checked) {
                WeaponLayer layer = checked.newLayer();
                Icon tabIcon = FontIcon.of(BoxiconsRegular.TARGET_LOCK, 20, Themes.getIconColor());
                WeaponLayerTab created = new WeaponLayerTab(layer);
                tabIndex.put(layer, created);
                String tooltip = created.getTabTooltip();
                this.addTab(LAYER + (getTabCount() + 1), tabIcon, tabIndex.get(layer), tooltip);
                EventBus.publish(new WindowRepaintQueued());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ActiveLayerUpdated checked) {
                this.handleTabUpdates(checked);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof ViewerLayerRemovalConfirmed checked) {
                ViewerLayer layer = checked.removed();
                closeLayer(layer);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer newlySelected = checked.selected();
                ViewerLayer selectedTabLayer = getLayerByTab((LayerTab) getSelectedComponent());
                if (newlySelected == selectedTabLayer) return;
                this.setSelectedIndex(indexOfComponent(tabIndex.get(newlySelected)));
            }
        });
    }

    private void handleTabUpdates(ActiveLayerUpdated event) {
        ViewerLayer eventLayer = event.updated();
        LayerTab updated = tabIndex.get(eventLayer);
        if (updated instanceof ShipLayerTab checkedShipTab && eventLayer instanceof ShipLayer checkedLayer) {
            this.updateShipTab(checkedShipTab, checkedLayer);
        } else {
            if (updated instanceof WeaponLayerTab checkedWeaponTab && eventLayer instanceof WeaponLayer checkedLayer) {
                this.updateWeaponTab(checkedWeaponTab, checkedLayer);
            }
        }
    }

    private void updateShipTab(ShipLayerTab tab, ShipLayer layer) {
        LayerPainter painter = layer.getPainter();
        if (painter == null) return;
        Sprite sprite = painter.getSprite();
        if (sprite != null) {
            tab.setSpriteFileName(sprite.getFilename());
            this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
        }

        String hullName;

        ShipHull shipHull = layer.getHull();

        if (shipHull != null) {
            hullName = shipHull.getHullName();
        } else {
            List<ViewerLayer> layers = layerManager.getLayers();
            int index = layers.indexOf(layer) + 1;
            hullName = LAYER + index;
        }

        tab.setHullFileName(layer.getHullFileName());
        this.setTitleAt(indexOfComponent(tab), hullName);

        ShipPainter layerPainter = layer.getPainter();
        if (layerPainter == null) return;

        tab.setSkinFileNames(layer.getSkinFileNames());
        ShipSkin activeSkin = layerPainter.getActiveSkin();
        if (activeSkin == null || activeSkin.isBase()) {
            tab.setActiveSkinFileName("");
            this.setTitleAt(indexOfComponent(tab), hullName);
        } else {
            String skinFileName = activeSkin.getFileName();
            tab.setActiveSkinFileName(skinFileName);
            if (activeSkin.getHullName() != null) {
                this.setTitleAt(indexOfComponent(tab), activeSkin.getHullName());
            }
        }
        this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
    }

    private void updateWeaponTab(WeaponLayerTab tab, WeaponLayer layer) {
        WeaponPainter painter = layer.getPainter();
        WeaponMount mount = painter.getMount();
        WeaponSprites weaponSprites = painter.getWeaponSprites();

        Sprite mainSprite = weaponSprites.getMainSprite(mount);
        if (mainSprite != null) {
            tab.setSpriteName(mainSprite.getFilename());
        }

        Sprite underSprite = weaponSprites.getUnderSprite(mount);
        if (underSprite != null) {
            tab.setUnderSpriteName(underSprite.getFilename());
        }

        Sprite gunSprite = weaponSprites.getGunSprite(mount);
        if (gunSprite != null) {
            tab.setGunSpriteName(gunSprite.getFilename());
        }

        Sprite glowSprite = weaponSprites.getGlowSprite(mount);
        if (glowSprite != null) {
            tab.setGlowSpriteName(glowSprite.getFilename());
        }

        WeaponSpecFile specFile = layer.getSpecFile();
        if (specFile != null) {
            tab.setSpecFileName(layer.getSpecFileName());
        }

        String weaponName = layer.getWeaponName();
        if (weaponName != null && !weaponName.isEmpty()) {
            this.setTitleAt(indexOfComponent(tab), weaponName);
        }
        this.setToolTipTextAt(indexOfComponent(tab), tab.getTabTooltip());
    }

    @Override
    protected void sortTabObjects() {
        List<ViewerLayer> layers = new ArrayList<>(tabIndex.keySet());

        ToIntFunction<ViewerLayer> intFunction = layer -> indexOfComponent(tabIndex.get(layer));
        layers.sort(Comparator.comparingInt(intFunction));
        layerManager.setLayers(layers);
    }

    private void closeLayer(ViewerLayer layer) {
        this.removeTabAt(indexOfComponent(tabIndex.get(layer)));
        tabIndex.remove(layer);
        EventBus.publish(new WindowRepaintQueued());
    }

    private ViewerLayer getLayerByTab(LayerTab value) {
        ViewerLayer result;
        for (Map.Entry<ViewerLayer, LayerTab> entry : tabIndex.entrySet()) {
            LayerTab entryValue = entry.getValue();
            if (entryValue.equals(value)) {
                result = entry.getKey();
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    class TabContextListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(SwingUtilities.isRightMouseButton(e)){
                TabbedPaneUI paneUI = getUI();
                int targetTab = paneUI.tabForCoordinate(ViewerLayersPanel.this, e.getX(), e.getY());
                if (targetTab < 0) {
                    JPopupMenu menu = new JPopupMenu();
                    menu.add(LayersMenu.createAddLayerOption());
                    menu.show(ViewerLayersPanel.this, e.getPoint().x, e.getPoint().y);
                    return;
                }
                LayerTab tab = (LayerTab) getComponentAt(targetTab);
                ViewerLayer layer = getLayerByTab(tab);

                showMenuIfMatching(layer, e);
            }
        }

        private void showMenuIfMatching(ViewerLayer layer, MouseEvent e) {
            if (layer instanceof ShipLayer shipLayer) {
                var menu = TabContextListener.createContextMenu(shipLayer);
                menu.show(ViewerLayersPanel.this, e.getPoint().x, e.getPoint().y);
            }
        }

        @SuppressWarnings("OverlyCoupledMethod")
        private static JPopupMenu createContextMenu(ShipLayer shipLayer) {
            ShipPainter shipPainter = shipLayer.getPainter();
            JPopupMenu menu = new JPopupMenu();

            JMenuItem openSprite = new JMenuItem("Load new sprite");
            openSprite.addActionListener(e -> OpenSpriteAction.openSpriteAndDo(sprite -> {
                PrimaryViewer viewer = StaticController.getViewer();
                viewer.loadSpriteToLayer(shipLayer, sprite);
            }));
            openSprite.setIcon(FontIcon.of(FluentUiRegularAL.IMAGE_20, 16, Themes.getIconColor()));
            menu.add(openSprite);

            if (shipPainter == null) {
                return menu;
            }

            JMenuItem createHullData = new JMenuItem("Create new ship data");
            createHullData.setIcon(FontIcon.of(BoxiconsRegular.DETAIL, 16, Themes.getIconColor()));
            createHullData.addActionListener(event -> {
                HullSpecFile created = new HullSpecFile();
                shipLayer.initializeHullData(created);
                EventBus.publish(new HullFileOpened(created, null));
            });
            menu.add(createHullData);

            if (shipLayer.getHull() == null) {
                return menu;
            }

            menu.addSeparator();

            JMenuItem selectEntry = new JMenuItem(StringValues.SELECT_SHIP_ENTRY);
            selectEntry.setIcon(FontIcon.of(FluentUiRegularMZ.SEARCH_SQUARE_24, 16, Themes.getIconColor()));
            String baseHullID = GameDataRepository.getBaseHullID(shipLayer.getShipID());
            if (baseHullID != null && !baseHullID.isEmpty()) {
                ShipCSVEntry entry = GameDataRepository.retrieveShipCSVEntryByID(baseHullID);
                if (entry != null) {
                    selectEntry.addActionListener(event -> EventBus.publish(new SelectShipDataEntry(entry)));
                } else {
                    selectEntry.setEnabled(false);
                }
            } else {
                selectEntry.setEnabled(false);
            }
            menu.add(selectEntry);

            menu.addSeparator();

            JMenuItem saveHullData = new JMenuItem("Save hull data");
            saveHullData.setIcon(FontIcon.of(FluentUiRegularMZ.SAVE_20, 16, Themes.getIconColor()));
            var shipHull = shipLayer.getHull();
            saveHullData.addActionListener(event -> EventBus.publish(new HullSaveQueued(shipLayer)));
            menu.add(saveHullData);

            JMenuItem saveActiveVariant = new JMenuItem("Save active variant");
            saveActiveVariant.setIcon(FontIcon.of(FluentUiRegularMZ.SAVE_20, 16, Themes.getIconColor()));
            var activeVariant = shipPainter.getActiveVariant();
            if (activeVariant != null && !activeVariant.isEmpty()) {
                saveActiveVariant.addActionListener(event -> EventBus.publish(new VariantSaveQueued(activeVariant)));
            } else {
                saveActiveVariant.setEnabled(false);
            }
            menu.add(saveActiveVariant);

            menu.addSeparator();

            menu.add(TabContextListener.createPrintLayerOption(shipLayer));

            return menu;
        }

        @SuppressWarnings("CallToPrintStackTrace")
        private static JMenuItem createPrintLayerOption(ViewerLayer layer) {
            JMenuItem printLayer = new JMenuItem("Print layer to image");
            printLayer.setIcon(FontIcon.of(BoxiconsRegular.IMAGE_ADD, 16, Themes.getIconColor()));

            printLayer.addActionListener(event -> {
                var chooser = FileUtilities.getImageChooser();

                SpinnerNumberModel widthModel = new SpinnerNumberModel(1000.0d,
                        100.0d, 4000.0d, 1.0d);
                SpinnerNumberModel heightModel = new SpinnerNumberModel(1000.0d,
                        100.0d, 4000.0d, 1.0d);

                String widthLabelText = "Image width:";
                String heightLabelText = "Image height:";

                JPanel dimensionsController = Spinners.createTwinSpinnerPanel(widthModel, heightModel,
                        widthLabelText, heightLabelText);
                dimensionsController.setBorder(new EmptyBorder(0, 16, 0, 0));
                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
                container.add(Box.createVerticalGlue());
                container.add(dimensionsController);
                container.add(Box.createVerticalGlue());
                chooser.setAccessory(container);

                chooser.setDialogTitle("Print layer to image file");

                int returnVal = chooser.showSaveDialog(null);
                FileUtilities.setLastDirectory(chooser.getCurrentDirectory());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String extension = ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0];
                    File result = FileUtilities.ensureFileExtension(chooser, extension);
                    log.info("Commencing layer printing: {}", result);

                    Number widthModelNumber = widthModel.getNumber();
                    int width = widthModelNumber.intValue();
                    Number heightModelNumber = heightModel.getNumber();
                    int height = heightModelNumber.intValue();

                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    BufferedImage toWrite = ColorUtilities.clearToTransparent(image);
                    Graphics2D g2d = toWrite.createGraphics();

                    AffineTransform transform = new AffineTransform();

                    LayerPainter painter = layer.getPainter();
                    Point2D spriteCenter = painter.getSpriteCenter();
                    Point2D midpoint = new Point2D.Double((double) width / 2, (double) height / 2);
                    double dx = midpoint.getX() - spriteCenter.getX();
                    double dy = midpoint.getY() - spriteCenter.getY();
                    transform.translate(dx, dy);
                    PaintOrderController.paintLayer(g2d, transform, width, height, layer);

                    g2d.dispose();
                    try {
                        javax.imageio.ImageIO.write(toWrite , extension, result);
                    } catch (IOException e) {
                        log.error("Layer printing failed: {}", result.getName());
                        JOptionPane.showMessageDialog(null,
                                "Layer printing failed, exception thrown at: " + result,
                                StringValues.FILE_SAVING_ERROR,
                                JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            });
            return printLayer;
        }

    }

}
