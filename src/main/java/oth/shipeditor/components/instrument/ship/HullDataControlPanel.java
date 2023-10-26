package oth.shipeditor.components.instrument.ship;

import com.formdev.flatlaf.ui.FlatLineBorder;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.persistence.SettingsManager;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.graphics.ColorUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 26.10.2023
 */
public class HullDataControlPanel extends JPanel {

    private ShipLayer cachedLayer;

    private JLabel coversColorValue;

    private JLabel spriteNameValue;

    private JComboBox<HullStyle> styleSelector;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    HullDataControlPanel() {
        this.setLayout(new GridBagLayout());
        ComponentUtilities.outfitPanelWithTitle(this,
                new Insets(1, 0, 0, 0), "Hull data");

        addStyleSelector();
        addCoversColorChooser();
        addSpriteNameLabel();
    }

    private void addSpriteNameLabel() {
        spriteNameValue = new JLabel();
        JLabel spriteNameLabel = new JLabel("Sprite name:");
        spriteNameLabel.setBorder(new EmptyBorder(2, 0, 4, 0));

        ComponentUtilities.addLabelAndComponent(this, spriteNameLabel,
                spriteNameValue, 2, 4, 0, 4);
    }

    private void addCoversColorChooser() {
        coversColorValue = new JLabel();
        JLabel coversColorLabel = new JLabel("Covers color:");

        coversColorLabel.setToolTipText("Right-click to change color");
        JPopupMenu colorChooserMenu = HullDataControlPanel.getColorChooserMenu();
        coversColorLabel.addMouseListener(new MouseoverLabelListener(colorChooserMenu, coversColorLabel));

        Insets insets = ComponentUtilities.createLabelInsets();
        insets.top = 1;
        coversColorLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(insets));

        ComponentUtilities.addLabelAndComponent(this, coversColorLabel,
                coversColorValue, 0, 2, 0, 5);
    }

    private void addStyleSelector() {
        JLabel selectorLabel = new JLabel("Hull style:");
        styleSelector  = new JComboBox<>();
        styleSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                HullStyle style = (HullStyle) value;
                if (style != null) {
                    setText(style.getHullStyleID());
                } else {
                    setText(StringValues.NOT_INITIALIZED);
                }

                return this;
            }
        });

        styleSelector.addActionListener(e -> {
            HullStyle selectedValue = (HullStyle) styleSelector.getSelectedItem();

            if (cachedLayer != null) {
                cachedLayer.setHullStyle(selectedValue);
                StaticController.reselectCurrentLayer();
            }
        });

        ComponentUtilities.addLabelAndComponent(this, selectorLabel, styleSelector,
                2, 0, 0, 3);
    }

    void clearData() {
        cachedLayer = null;

        spriteNameValue.setText(StringValues.NOT_INITIALIZED);

        coversColorValue.setIcon(null);
        coversColorValue.setOpaque(false);
        coversColorValue.setBorder(new EmptyBorder(0, 2, 0, 2));
        coversColorValue.setBackground(null);
        coversColorValue.setToolTipText(null);
        coversColorValue.setText(StringValues.NOT_INITIALIZED);

        styleSelector.setSelectedItem(null);
        styleSelector.setEnabled(false);
    }

    void refreshData(ShipLayer layer) {
        cachedLayer = layer;

        ShipHull shipHull = layer.getHull();
        var coversColor = shipHull.getCoversColor();
        if (coversColor != null) {
            ImageIcon colorIcon = ComponentUtilities.createIconFromColor(coversColor, 10, 10);
            coversColorValue.setIcon(colorIcon);
            coversColorValue.setOpaque(true);
            coversColorValue.setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), Color.GRAY));
            coversColorValue.setBackground(Color.LIGHT_GRAY);
            coversColorValue.setToolTipText(ColorUtilities.getColorBreakdown(coversColor));
            coversColorValue.setText(null);
        } else {
            coversColorValue.setText("Not defined");
        }

        spriteNameValue.setText(layer.getRelativeSpritePath());

        GameDataRepository gameData = SettingsManager.getGameData();
        Map<String, HullStyle> allHullStyles = gameData.getAllHullStyles();
        if (allHullStyles != null) {
            Collection<HullStyle> styleCollection = allHullStyles.values();
            HullStyle[] hullStyles = styleCollection.toArray(new HullStyle[0]);

            ComboBoxModel<HullStyle> styleModel = new DefaultComboBoxModel<>(hullStyles);

            styleSelector.setEnabled(true);
            styleSelector.setModel(styleModel);
            styleSelector.setSelectedItem(shipHull.getHullStyle());
        }
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private static JPopupMenu getColorChooserMenu() {
        JPopupMenu colorChooserMenu = new JPopupMenu();
        JMenuItem adjustColor = new JMenuItem(StringValues.ADJUST_VALUE);
        adjustColor.addActionListener(event -> {
            var activeLayer = StaticController.getActiveLayer();
            if (activeLayer instanceof ShipLayer shipLayer) {
                ShipHull shipHull = shipLayer.getHull();
                if (shipHull != null) {
                    Color chosen;
                    var current = shipHull.getCoversColor();
                    if (current != null) {
                        chosen = ColorUtilities.showColorChooser(current);
                    } else {
                        chosen = ColorUtilities.showColorChooser();
                    }
                    shipHull.setCoversColor(chosen);
                    StaticController.reselectCurrentLayer();
                } else {
                    HullDataControlPanel.abortColorInteraction();
                }
            } else {
                HullDataControlPanel.abortColorInteraction();
            }
        });
        colorChooserMenu.add(adjustColor);

        JMenuItem removeColor = new JMenuItem("Clear value");
        removeColor.addActionListener(event -> {
            var activeLayer = StaticController.getActiveLayer();
            if (activeLayer instanceof ShipLayer shipLayer) {
                ShipHull shipHull = shipLayer.getHull();
                if (shipHull != null) {
                    shipHull.setCoversColor(null);
                    StaticController.reselectCurrentLayer();
                } else {
                    HullDataControlPanel.abortColorInteraction();
                }
            } else {
                HullDataControlPanel.abortColorInteraction();
            }
        });
        colorChooserMenu.add(removeColor);

        return colorChooserMenu;
    }

    private static void abortColorInteraction() {
        JOptionPane.showMessageDialog(null,
                "Current layer invalid, color interaction aborted.",
                "Color interaction",
                JOptionPane.ERROR_MESSAGE);
    }

}
