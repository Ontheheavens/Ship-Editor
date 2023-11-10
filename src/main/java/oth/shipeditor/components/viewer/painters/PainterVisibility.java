package oth.shipeditor.components.viewer.painters;

import lombok.Getter;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.layers.PainterVisibilityChanged;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.objects.Pair;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
@Getter
public enum PainterVisibility {
    ALWAYS_HIDDEN("Always hidden"),
    SHOWN_WHEN_SELECTED("When selected"),
    SHOWN_WHEN_EDITED("When edited"),
    ALWAYS_SHOWN("Always shown");

    private final String name;

    PainterVisibility(String inputName) {
        this.name = inputName;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static DefaultListCellRenderer createCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component rendererComponent = super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                setText(value instanceof PainterVisibility ? ((PainterVisibility) value).getName() : value.toString());
                return rendererComponent;
            }
        };
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static ActionListener createActionListener(JComboBox<PainterVisibility> visibilityList,
                                                      Class<? extends AbstractPointPainter> painterClass) {
        return e -> {
            PainterVisibility changedValue = (PainterVisibility) visibilityList.getSelectedItem();
            EventBus.publish(new PainterVisibilityChanged(painterClass, changedValue));
        };
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static BusEventListener createBusEventListener(JComboBox<PainterVisibility> visibilityList,
                                                          ActionListener selectionAction) {
        return event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                if (selected == null) {
                    visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
                    visibilityList.setEnabled(false);
                    return;
                }
                LayerPainter painter = selected.getPainter();
                if (painter instanceof ShipPainter checkedPainter && !checkedPainter.isUninitialized()) {
                    selectionAction.actionPerformed(new ActionEvent(checkedPainter,
                            ActionEvent.ACTION_PERFORMED, null));
                    visibilityList.setEnabled(true);
                } else {
                    visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
                    visibilityList.setEnabled(false);
                }
            }
        };
    }

    public static JPanel createVisibilityWidget(JComboBox<PainterVisibility> visibilityList,
                                                Class<? extends AbstractPointPainter> painterClass,
                                                ActionListener selectionAction, String labelName) {
        ActionListener chooseAction = createActionListener(visibilityList, painterClass);
        return createVisibilityWidgetRaw(visibilityList, chooseAction, selectionAction, labelName);
    }

    public static JPanel createVisibilityWidgetRaw(JComboBox<PainterVisibility> visibilityList,
                                                   ActionListener chooseAction,
                                                   ActionListener selectionAction, String labelName) {
        String widgetLabel = labelName;
        JPanel widgetPanel = new JPanel();
        widgetPanel.setLayout(new GridBagLayout());

        visibilityList.setRenderer(createCellRenderer());
        visibilityList.addActionListener(chooseAction);
        EventBus.subscribe(createBusEventListener(visibilityList, selectionAction));

        visibilityList.setMaximumSize(visibilityList.getPreferredSize());

        if (widgetLabel.isEmpty()) {
            widgetLabel = StringValues.PAINTER_VIEW;
        }

        JLabel visibilityWidgetLabel = new JLabel(widgetLabel);
        visibilityWidgetLabel.setToolTipText(StringValues.TOGGLED_ON_PER_LAYER_BASIS);

        ComponentUtilities.addLabelAndComponent(widgetPanel, visibilityWidgetLabel, visibilityList, 0);
        widgetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));

        return widgetPanel;
    }


    public static Pair<JLabel, JComboBox<PainterVisibility>> createVisibilityWidget(
            BooleanSupplier widgetChecker, Function<LayerPainter, PainterVisibility> getter,
            Consumer<PainterVisibility> setter,
            BiConsumer<JComponent, Consumer<LayerPainter>> clearerListener,
            BiConsumer<JComponent, Consumer<LayerPainter>> refresherListener) {

        JComboBox<PainterVisibility> visibilityList = new JComboBox<>(PainterVisibility.values());
        visibilityList.setRenderer(PainterVisibility.createCellRenderer());
        JLabel visibilityLabel = new JLabel("Visibility:");

        visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
        visibilityList.setEnabled(false);

        visibilityList.addActionListener(e -> {
            if (widgetChecker.getAsBoolean()) {
                PainterVisibility changedValue = (PainterVisibility) visibilityList.getSelectedItem();
                setter.accept(changedValue);
            }
        });

        clearerListener.accept(visibilityList, layer -> {
            visibilityList.setSelectedItem(PainterVisibility.SHOWN_WHEN_EDITED);
            visibilityList.setEnabled(false);
        });

        refresherListener.accept(visibilityList, layerPainter -> {
            visibilityList.setEnabled(true);
            visibilityList.setSelectedItem(getter.apply(layerPainter));
        });

        return new Pair<>(visibilityLabel, visibilityList);
    }
}
