package oth.shipeditor.components.instrument.ship.shared;

import oth.shipeditor.components.instrument.LayerPropertiesPanel;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.weapon.WeaponAnimator;
import oth.shipeditor.components.viewer.layers.weapon.WeaponPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.DynamicMenuListener;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.graphics.Sprite;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 29.12.2023
 */
public class WeaponAnimationPanel extends LayerPropertiesPanel {

    private WeaponAnimator cachedAnimator;

    private Consumer<LayerPainter> currentFrameRefresher;
    private Consumer<LayerPainter> spritePathRefresher;

    @Override
    public WeaponPainter getCachedLayerPainter() {
        return (WeaponPainter) super.getCachedLayerPainter();
    }

    @Override
    public void refreshContent(LayerPainter layerPainter) {
        if (layerPainter == null || layerPainter.isUninitialized()) {
            fireClearingListeners(null);
            if (cachedAnimator != null) {
                cachedAnimator.setOnFrameChange(null);
            }
            cachedAnimator = null;
            return;
        }

        fireRefresherListeners(layerPainter);
        WeaponPainter cachedLayerPainter = getCachedLayerPainter();
        cachedAnimator = cachedLayerPainter.getAnimator();
        cachedAnimator.setOnFrameChange(integer -> {
            if (!isWidgetsReadyForInput()) {
                return;
            }
            if (getCachedLayerPainter() instanceof WeaponPainter weaponPainter) {
                spritePathRefresher.accept(weaponPainter);
                currentFrameRefresher.accept(weaponPainter);
            }
        });
    }

    @Override
    protected void populateContent() {
        this.setLayout(new BorderLayout());

        Map<JLabel, JComponent> widgets = new LinkedHashMap<>();

        var spritePathWidget = createSpritePathWidget();
        widgets.put(spritePathWidget.getFirst(), spritePathWidget.getSecond());

        var slotIdWidget = createCurrentFrameWidget();
        widgets.put(slotIdWidget.getFirst(), slotIdWidget.getSecond());

        var slotMountWidget = createFrameRateSelector();
        widgets.put(slotMountWidget.getFirst(), slotMountWidget.getSecond());

        JPanel widgetsPanel = createWidgetsPanel(widgets);
        this.add(widgetsPanel, BorderLayout.PAGE_START);

        JPanel buttonsPanel = createAnimationButtonsPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));

        this.add(buttonsPanel, BorderLayout.CENTER);
    }

    @Override
    protected void addWidgetRow(JPanel contentContainer, JLabel label, JComponent component, int ordering) {
        ComponentUtilities.addLabelAndComponent(contentContainer,
                label, component, 3, 5, 2, ordering);
    }

    private JPopupMenu getPathContextMenu() {
        WeaponPainter weaponPainter = getCachedLayerPainter();
        WeaponAnimator weaponAnimator = weaponPainter.getAnimator();
        if (weaponAnimator.isInitialized()) {
            Sprite sprite = weaponAnimator.getCurrentSprite(weaponPainter.getMount());
            Path path = sprite.getPath();
            return ComponentUtilities.createPathContextMenu(path);
        } else {
            return null;
        }
    }

    private Pair<JLabel, JComponent> createSpritePathWidget() {
        JLabel spriteLabel = new JLabel("Current sprite:");
        spriteLabel.setBorder(new EmptyBorder(2, 0, 4, 0));

        JLabel spritePathLabel = new JLabel();
        spritePathLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(ComponentUtilities.createLabelInsets()));
        spritePathLabel.addMouseListener(
                new DynamicMenuListener(this::getPathContextMenu, spritePathLabel)
        );

        spritePathRefresher = layerPainter -> {
            boolean isWeaponPainter = layerPainter instanceof WeaponPainter;
            if (!isWeaponPainter) {
                return;
            }

            if (layerPainter.isUninitialized()) {
                spritePathLabel.setText(StringValues.NOT_INITIALIZED);
                spritePathLabel.setEnabled(false);
                spritePathLabel.setToolTipText("");
            } else {
                WeaponPainter weaponPainter = (WeaponPainter) layerPainter;
                WeaponAnimator weaponAnimator = weaponPainter.getAnimator();
                if (weaponAnimator.isInitialized()) {
                    Sprite sprite = weaponAnimator.getCurrentSprite(weaponPainter.getMount());
                    spritePathLabel.setText(sprite.getPathFromPackage());
                    Path path = sprite.getPath();
                    spritePathLabel.setToolTipText(path.toString());
                    spritePathLabel.setEnabled(true);
                } else {
                    spritePathLabel.setText(StringValues.NOT_INITIALIZED);
                    spritePathLabel.setEnabled(false);
                    spritePathLabel.setToolTipText("");
                }
            }
        };
        registerWidgetListeners(spritePathLabel, layerPainter -> {
            spritePathLabel.setText(StringValues.NOT_INITIALIZED);
            spritePathLabel.setEnabled(false);
            spritePathLabel.setToolTipText("");
        }, spritePathRefresher);

        return new Pair<>(spriteLabel, spritePathLabel);
    }

    private JPanel createAnimationButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        JButton playButton = new JButton("Play");
        playButton.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                WeaponPainter layerPainter = getCachedLayerPainter();
                WeaponAnimator weaponAnimator = layerPainter.getAnimator();
                weaponAnimator.startAnimation();
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                WeaponPainter layerPainter = getCachedLayerPainter();
                WeaponAnimator weaponAnimator = layerPainter.getAnimator();
                weaponAnimator.stopAnimation();
            }
        });

        registerPlaybackButton(playButton);

        registerPlaybackButton(stopButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 0);
        buttonsPanel.add(playButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        buttonsPanel.add(stopButton, constraints);

        return buttonsPanel;
    }

    private void registerPlaybackButton(JButton button) {
        registerWidgetListeners(button,
                layerPainter -> button.setEnabled(false),
                layerPainter -> {
            boolean isWeaponPainter = layerPainter instanceof WeaponPainter;
            if (!isWeaponPainter) {
                button.setEnabled(false);
                return;
            }

            if (layerPainter.isUninitialized()) {
                button.setEnabled(false);
            } else {
                WeaponAnimator weaponAnimator = ((WeaponPainter) layerPainter).getAnimator();
                button.setEnabled(weaponAnimator.isInitialized());
            }
        });
    }

    private Pair<JLabel, JComponent> createCurrentFrameWidget() {
        JLabel frameLabel = new JLabel("Animation frame:");

        String tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        frameLabel.setToolTipText(tooltip);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(
                0.0d, 0.0d, 0.0d, 1.0d
        );
        JSpinner spinner =  Spinners.createUnaryIntegerWheelable(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                WeaponPainter layerPainter = getCachedLayerPainter();
                WeaponAnimator weaponAnimator = layerPainter.getAnimator();
                weaponAnimator.setCurrentFrame((int) current);

                spritePathRefresher.accept(getCachedLayerPainter());
            }
        });

        this.currentFrameRefresher = layerPainter -> {
            boolean isWeaponPainter = layerPainter instanceof WeaponPainter;
            if (!isWeaponPainter) {
                return;
            }

            if (layerPainter.isUninitialized()) {
                spinner.setValue(0.0d);
                spinner.setEnabled(false);
                spinnerNumberModel.setMaximum(0.0d);
            } else {
                WeaponAnimator weaponAnimator = ((WeaponPainter) layerPainter).getAnimator();
                if (weaponAnimator.isInitialized()) {
                    spinner.setValue((double) weaponAnimator.getCurrentFrame());
                    spinner.setEnabled(true);
                    spinnerNumberModel.setMaximum((double) weaponAnimator.getFrameCount() - 1);
                } else {
                    spinner.setValue(0.0d);
                    spinner.setEnabled(false);
                    spinnerNumberModel.setMaximum(0.0d);
                }
            }
        };
        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setEnabled(false);
        }, currentFrameRefresher);

        return new Pair<>(frameLabel, spinner);
    }

    private Pair<JLabel, JComponent> createFrameRateSelector() {
        JLabel frameRateLabel = new JLabel("Framerate override:");

        String tooltip = StringValues.MOUSEWHEEL_TO_CHANGE;
        frameRateLabel.setToolTipText(tooltip);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(
                0.0d, 0.0d, 60.0d, 1.0d
        );
        JSpinner spinner =  Spinners.createUnaryIntegerWheelable(spinnerNumberModel);

        spinner.addChangeListener(e -> {
            if (isWidgetsReadyForInput()) {
                Number modelNumber = spinnerNumberModel.getNumber();
                double current = modelNumber.doubleValue();

                WeaponPainter layerPainter = getCachedLayerPainter();
                WeaponAnimator weaponAnimator = layerPainter.getAnimator();
                weaponAnimator.setFrameRate((int) current);
            }
        });

        registerWidgetListeners(spinner, layerPainter -> {
            spinner.setValue(0.0d);
            spinner.setEnabled(false);
        }, layerPainter -> {
            boolean isWeaponPainter = layerPainter instanceof WeaponPainter;
            if (!isWeaponPainter) {
                return;
            }

            if (layerPainter.isUninitialized()) {
                spinner.setValue(0.0d);
                spinner.setEnabled(false);
            } else {
                WeaponAnimator weaponAnimator = ((WeaponPainter) layerPainter).getAnimator();
                if (weaponAnimator.isInitialized()) {
                    spinner.setValue((double) weaponAnimator.getFrameRate());
                    spinner.setEnabled(true);
                } else {
                    spinner.setValue(0.0d);
                    spinner.setEnabled(false);
                }
            }
        });

        return new Pair<>(frameRateLabel, spinner);
    }

}
