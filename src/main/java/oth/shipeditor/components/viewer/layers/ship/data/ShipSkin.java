package oth.shipeditor.components.viewer.layers.ship.data;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.ShipSystemCSVEntry;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.representation.EngineSlot;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.representation.ShipTypeHints;
import oth.shipeditor.utility.graphics.Sprite;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 31.07.2023
 */
public class ShipSkin {

    private Path skinFilePath;

    private Path containingPackage;

    private Sprite loadedSkinSprite;

    private String baseHullId;

    private String skinHullId;

    private ShipSystemCSVEntry shipSystem;

    private String hullName;

    private String hullDesignation;

    private HullStyle hullStyle;

    private boolean restoreToBaseHull;

    private boolean incompatibleWithBaseHull;

    private int fleetPoints;

    private int ordnancePoints;

    private int baseValue;

    private double suppliesPerMonth;

    private double suppliesToRecover;

    private String descriptionId;

    private String descriptionPrefix;

    private Color coversColor;

    // TODO: Perhaps an enum field?.
    private List<String> tags;

    // TODO: load all tech designations from settings files (optionally, perhaps?) and make this an object field.
    // Disregard that. Make this an unverifiable string field and place all responsibility on user.
    private String tech;

    // TODO: Load all wings and make this an object field.
    private List<String> builtInWings;

    private int fighterBays;

    private String spriteName;

    private double baseValueMult;

    private List<ShipTypeHints> removeHints;

    private List<ShipTypeHints> addHints;

    private List<String> removeWeaponSlots;

    private List<Integer> removeEngineSlots;

    private List<HullmodCSVEntry> removeBuiltInMods;

    private List<String> removeBuiltInWeapons;

    private List<HullmodCSVEntry> builtInMods;

    // TODO: Will have to load all weapons and make this an object field.
    private Map<String, String> builtInWeapons;

    private Map<String, WeaponSlotOverride> weaponSlotChanges;

    // TODO: Needs to be a runtime type.
    private Map<String, EngineSlot> engineSlotChanges;

}
