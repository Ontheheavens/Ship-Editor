package oth.shipeditor.components.datafiles.trees;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.WeaponTreeReloadQueued;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

/**
 * @author Ontheheavens
 * @since 08.09.2023
 */
class WeaponFilterPanel extends JPanel {

    @Getter
    @Setter
    private static String currentTextFilter;

    @SuppressWarnings("StaticCollection")
    @Getter
    private static final Map<WeaponType, Boolean> TYPE_FILTERS = new EnumMap<>(WeaponType.class);

    @SuppressWarnings("StaticCollection")
    @Getter
    private static final Map<WeaponSize, Boolean> SIZE_FILTERS = new EnumMap<>(WeaponSize.class);

    static {
        for (WeaponType type : WeaponType.values()) {
            TYPE_FILTERS.put(type, true);
        }
        for (WeaponSize size : WeaponSize.values()) {
            SIZE_FILTERS.put(size, true);
        }
    }

    WeaponFilterPanel() {
        this.setLayout(new BorderLayout());

        JPanel filtersPane = new JPanel();
        filtersPane.setLayout(new BoxLayout(filtersPane, BoxLayout.PAGE_AXIS));
        filtersPane.setAlignmentY(0);
        filtersPane.add(Box.createRigidArea(new Dimension(10, 4)));
        filtersPane.add(WeaponFilterPanel.createWeaponTypeFilters());
        filtersPane.add(Box.createRigidArea(new Dimension(10, 4)));
        filtersPane.add(WeaponFilterPanel.createWeaponSizeFilters());
        filtersPane.add(Box.createRigidArea(new Dimension(10, 4)));

        filtersPane.add(Box.createVerticalGlue());

        JScrollPane scrollContainer = new JScrollPane(filtersPane);

        this.add(scrollContainer, BorderLayout.CENTER);
    }

    static Map<String, List<WeaponCSVEntry>> getFilteredEntries() {
        GameDataRepository gameData = SettingsManager.getGameData();
        Map<Path, List<WeaponCSVEntry>> weaponEntriesByPackage = gameData.getWeaponEntriesByPackage();

        if (weaponEntriesByPackage == null) return null;

        Map<String, List<WeaponCSVEntry>> filteredResult = new HashMap<>();
        for (Map.Entry<Path, List<WeaponCSVEntry>> entryPackage : weaponEntriesByPackage.entrySet()) {
            List<WeaponCSVEntry> entryList = entryPackage.getValue();
            List<WeaponCSVEntry> filteredList = entryList.stream()
                    .filter(WeaponFilterPanel::shouldDisplayByType)
                    .filter(WeaponFilterPanel::shouldDisplayBySize)
                    .filter(WeaponFilterPanel::shouldDisplayByHandle)
                    .toList();
            if (!filteredList.isEmpty()) {
                Path entryPackageKey = entryPackage.getKey();
                filteredResult.put(entryPackageKey.toString(), filteredList);
            }
        }
        return filteredResult;
    }

    private static boolean shouldDisplayBySize(WeaponCSVEntry entry) {
        WeaponSize weaponSize = entry.getSize();
        return SIZE_FILTERS.get(weaponSize);
    }


    private static boolean shouldDisplayByType(WeaponCSVEntry entry) {
        WeaponType weaponType = entry.getType();
        return TYPE_FILTERS.get(weaponType);
    }

    private static boolean shouldDisplayByHandle(WeaponCSVEntry entry) {
        if (currentTextFilter == null || currentTextFilter.isEmpty()) return true;
        String currentInput = currentTextFilter.toLowerCase(Locale.ROOT);
        String name = entry.toString();
        if (name.toLowerCase(Locale.ROOT).contains(currentInput)) {
            return true;
        }
        String id = entry.getWeaponID();
        return id.toLowerCase(Locale.ROOT).contains(currentInput);
    }

    private static JPanel createWeaponTypeFilters() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Weapon Type");

        Collection<WeaponType> weaponTypes = new ArrayList<>(List.of(WeaponType.values()));
        weaponTypes.remove(WeaponType.STATION_MODULE);
        weaponTypes.remove(WeaponType.SYSTEM);
        weaponTypes.remove(WeaponType.BUILT_IN);

        for (WeaponType type : weaponTypes) {
            JPanel buttonContainer = new JPanel();

            buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.LINE_AXIS));
            buttonContainer.setBorder(new EmptyBorder(4, 0, 0, 0));

            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(type.getDisplayName());
            checkBox.setSelected(TYPE_FILTERS.get(type));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    TYPE_FILTERS.put(type, Boolean.TRUE);
                } else {
                    TYPE_FILTERS.put(type, Boolean.FALSE);
                }
                EventBus.publish(new WeaponTreeReloadQueued());
            });
            JLabel colorIcon = ComponentUtilities.createColorIconLabel(type.getColor());
            buttonContainer.add(colorIcon);
            buttonContainer.add(checkBox);
            buttonContainer.add(Box.createHorizontalGlue());

            container.add(buttonContainer);
        }
        container.add(Box.createRigidArea(new Dimension(10, 4)));
        return container;
    }

    private static JPanel createWeaponSizeFilters() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setAlignmentX(0.5f);
        container.setAlignmentY(0);

        ComponentUtilities.outfitPanelWithTitle(container,
                new Insets(1, 0, 0, 0), "Weapon Size");

        Iterable<WeaponSize> weaponSizes = new ArrayList<>(List.of(WeaponSize.values()));

        for (WeaponSize size : weaponSizes) {
            JPanel buttonContainer = new JPanel();

            buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.LINE_AXIS));
            buttonContainer.setBorder(new EmptyBorder(4, 0, 0, 0));

            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(size.getDisplayName());
            checkBox.setSelected(SIZE_FILTERS.get(size));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    SIZE_FILTERS.put(size, Boolean.TRUE);
                } else {
                    SIZE_FILTERS.put(size, Boolean.FALSE);
                }
                EventBus.publish(new WeaponTreeReloadQueued());
            });
            buttonContainer.add(checkBox);
            buttonContainer.add(Box.createHorizontalGlue());

            container.add(buttonContainer);
        }
        container.add(Box.createRigidArea(new Dimension(10, 4)));
        return container;
    }

}
