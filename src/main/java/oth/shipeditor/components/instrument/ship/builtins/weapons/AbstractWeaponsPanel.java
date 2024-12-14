package oth.shipeditor.components.instrument.ship.builtins.weapons;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.components.WeaponEntryPicked;
import oth.shipeditor.communication.events.viewer.points.PointSelectedConfirmed;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.AbstractShipPropertiesPanel;
import oth.shipeditor.components.instrument.ship.shared.InstalledFeatureList;
import oth.shipeditor.components.instrument.ship.shared.WeaponAnimationPanel;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 03.12.2023
 */
public abstract class AbstractWeaponsPanel extends AbstractShipPropertiesPanel {

    private InstalledFeatureList baseHullList;

    private DefaultListModel<InstalledFeature> baseModel;

    private InstalledFeatureList skinEntriesList;

    private DefaultListModel<InstalledFeature> skinEntriesModel;

    private WeaponAnimationPanel weaponAnimationPanel;

    private JPanel pickerPanel;

    private JPanel pickerInfo;

    @SuppressWarnings("ChainedMethodCall")
    @Override
    public void refreshContent(LayerPainter layerPainter) {
        DefaultListModel<InstalledFeature> newBaseModel = new DefaultListModel<>();
        DefaultListModel<InstalledFeature> newSkinModel = new DefaultListModel<>();

        int[] baseCachedSelected = this.baseHullList.getSelectedIndices();
        int[] skinCachedSelected = this.skinEntriesList.getSelectedIndices();

        refreshWeaponAnimationPanel(null);

        if (!(layerPainter instanceof ShipPainter shipPainter)
                || shipPainter.isUninitialized()) {

            this.baseModel = newBaseModel;
            this.skinEntriesModel = newSkinModel;

            this.baseHullList.setModel(baseModel);
            this.skinEntriesList.setModel(skinEntriesModel);

            fireClearingListeners(layerPainter);

            this.baseHullList.setEnabled(false);
            this.skinEntriesList.setEnabled(false);
            return;
        }

        newBaseModel.addAll(getBaseHullFilteredEntries(shipPainter.getParentLayer()).values());

        this.baseModel = newBaseModel;
        this.baseHullList.setModel(baseModel);
        this.baseHullList.setEnabled(true);

        if (!this.baseModel.isEmpty() && baseCachedSelected.length > 0) {
            this.baseHullList.setSelectedIndices(baseCachedSelected);
            this.baseHullList.ensureIndexIsVisible(baseCachedSelected[0]);
        }

        ShipSkin activeSkin = shipPainter.getActiveSkin();

        if (activeSkin != null && !activeSkin.isBase()) {
            var added = getSkinFilteredEntries(shipPainter.getParentLayer());

            newSkinModel.addAll(added.values());

            this.skinEntriesModel = newSkinModel;
            this.skinEntriesList.setModel(skinEntriesModel);
            this.skinEntriesList.setEnabled(true);

            if (!this.skinEntriesModel.isEmpty() && skinCachedSelected.length > 0) {
                this.skinEntriesList.setSelectedIndices(skinCachedSelected);
                this.skinEntriesList.ensureIndexIsVisible(skinCachedSelected[0]);
            }
        } else {
            this.skinEntriesModel = newSkinModel;
            this.skinEntriesList.setModel(skinEntriesModel);
            this.skinEntriesList.setEnabled(false);
        }

        fireRefresherListeners(layerPainter);
    }

    private void refreshWeaponAnimationPanel(InstalledFeature feature) {
        if (feature == null) {
            this.weaponAnimationPanel.refresh(null);
            return;
        }
        if (feature.getFeaturePainter() instanceof WeaponPainter weaponPainter) {
            this.weaponAnimationPanel.refresh(weaponPainter);
        } else {
            this.weaponAnimationPanel.refresh(null);
        }
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        this.baseModel = new DefaultListModel<>();
        this.skinEntriesModel = new DefaultListModel<>();

        this.baseHullList = createBaseEntriesList();
        baseHullList.setBelongsToBaseHullBuiltIns(true);

        JPanel baseWrapper = AbstractWeaponsPanel.createListWrapper(baseHullList, StringValues.BASE_BUILT_INS);
        this.skinEntriesList = createSkinEntriesList();
        JPanel skinWrapper = AbstractWeaponsPanel.createListWrapper(skinEntriesList, "Skin built-ins");

        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.5;
        constraints.weightx = 1;
        constraints.ipady = 40;
        constraints.gridy = 0;

        container.add(baseWrapper, constraints);
        constraints.gridy = 1;
        container.add(skinWrapper, constraints);

        JScrollPane scroller = new JScrollPane(container);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.weaponAnimationPanel = new WeaponAnimationPanel();

        ComponentUtilities.outfitPanelWithTitle(this.weaponAnimationPanel, StringValues.WEAPON_ANIMATION);

        weaponAnimationPanel.refresh(null);
        pickerPanel = new JPanel(new BorderLayout());

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BorderLayout());
        northContainer.add(weaponAnimationPanel, BorderLayout.PAGE_START);
        northContainer.add(pickerPanel, BorderLayout.CENTER);

        this.add(northContainer, BorderLayout.PAGE_START);
        this.add(scroller, BorderLayout.CENTER);
    }

    private static JPanel createListWrapper(InstalledFeatureList entryList, String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(entryList, BorderLayout.CENTER);
        ComponentUtilities.outfitPanelWithTitle(wrapper, title);

        return wrapper;
    }

    private void refreshWeaponPicker() {
        if (pickerInfo != null) {
            pickerPanel.remove(pickerInfo);
        }

        WeaponCSVEntry pickedForInstall = FeaturesOverseer.getWeaponForInstall();
        if (pickedForInstall != null) {
            pickerInfo = pickedForInstall.createPickedWeaponPanel();
            pickerPanel.add(pickerInfo, BorderLayout.CENTER);
        }

        this.revalidate();
        this.repaint();
    }

    @Override
    protected void initLayerListeners() {
        super.initLayerListeners();
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued checked) {
                if (checked.editorMode() != getMode()) {
                    return;
                }
                ViewerLayer activeLayer = StaticController.getActiveLayer();
                this.refresh(activeLayer.getPainter());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof WeaponEntryPicked) {
                this.refreshWeaponPicker();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointSelectedConfirmed checked) {
                if (!(checked.point() instanceof WeaponSlotPoint slotPoint)) return;
                boolean correctMode = StaticController.getEditorMode() == getMode();
                if (baseHullList != null && correctMode) {
                    baseHullList.selectEntryByPoint(slotPoint);
                }
                if (skinEntriesList != null && correctMode) {
                    skinEntriesList.selectEntryByPoint(slotPoint);
                }
            }
        });
    }

    abstract EditorInstrument getMode();

    abstract Map<String, InstalledFeature> getBaseHullFilteredEntries(ShipLayer shipLayer);

    abstract Map<String, InstalledFeature> getSkinFilteredEntries(ShipLayer shipLayer);

    abstract Consumer<Map<String, InstalledFeature>> getBaseHullSortAction(ShipLayer shipLayer);

    abstract Consumer<Map<String, InstalledFeature>> getSkinSortAction(ShipLayer shipLayer);

    private InstalledFeatureList createBaseEntriesList() {
        Consumer<InstalledFeature> removeAction = feature -> StaticController.actOnCurrentShip(shipLayer -> {
            var shipPainter = shipLayer.getPainter();
            var weapons = shipPainter.getBuiltInWeapons();
            EditDispatch.postFeatureUninstalled(weapons, feature.getSlotID(), feature, null);
        });

        Consumer<Map<String, InstalledFeature>> sortAction = features ->
                StaticController.actOnCurrentShip(shipLayer -> {
            Consumer<Map<String, InstalledFeature>> baseSortAction = getBaseHullSortAction(shipLayer);
            baseSortAction.accept(features);
        });

        return this.createList(baseModel, removeAction, sortAction);
    }

    private InstalledFeatureList createSkinEntriesList() {
        Consumer<InstalledFeature> removeAction = feature -> StaticController.actOnCurrentShip(shipLayer -> {
            var shipPainter = shipLayer.getPainter();
            ShipSkin activeSkin = shipPainter.getActiveSkin();

            String slotID = feature.getSlotID();
            var addedBySkin = activeSkin.getBuiltInWeapons();
            WeaponCSVEntry toRemove = addedBySkin.get(slotID);
            EditDispatch.postFeatureUninstalled(addedBySkin, slotID,
                    toRemove, activeSkin::invalidateBuiltIns);
        });

        Consumer<Map<String, InstalledFeature>> sortAction = features ->
                StaticController.actOnCurrentShip(shipLayer -> {
                    Consumer<Map<String, InstalledFeature>> skinSortAction = getSkinSortAction(shipLayer);
                    skinSortAction.accept(features);
                });

        return this.createList(skinEntriesModel, removeAction, sortAction);
    }

    private InstalledFeatureList createList(ListModel<InstalledFeature> dataModel,
                                                   Consumer<InstalledFeature> removeAction,
                                                   Consumer<Map<String, InstalledFeature>> sortAction) {
        InstalledFeatureList entriesList = new InstalledFeatureList(dataModel,
                removeAction, sortAction, this::refreshWeaponAnimationPanel);
        entriesList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return entriesList;
    }

}
