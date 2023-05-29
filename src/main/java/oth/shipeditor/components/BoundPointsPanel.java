package oth.shipeditor.components;

import lombok.Getter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.PointPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationToggled;
import oth.shipeditor.communication.events.viewer.points.BoundCreationModeChanged;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.entities.BoundPoint;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class BoundPointsPanel extends JPanel implements PointsDisplay<BoundPoint> {

    @Getter
    private final BoundList boundPointContainer;

    @Getter
    private InteractionMode mode;

    private final DefaultListModel<BoundPoint> model = new DefaultListModel<>();
    @Getter
    private JToggleButton selectModeButton;
    @Getter
    private JToggleButton createModeButton;

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        boundPointContainer = new BoundList(model);
        JScrollPane scrollableContainer = new JScrollPane(boundPointContainer);
        Dimension listSize = new Dimension(boundPointContainer.getPreferredSize().width + 30,
                boundPointContainer.getPreferredSize().height);
        scrollableContainer.setPreferredSize(listSize);
        this.add(scrollableContainer, BorderLayout.CENTER);
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createEtchedBorder());

        this.createModeButtons(modePanel);
        this.initModeButtonListeners();

        this.add(modePanel, BorderLayout.NORTH);
        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        this.setBorder(line);
        this.setMode(InteractionMode.DISABLED);

        this.initPointListener();
    }

    public void setMode(InteractionMode mode) {
        this.mode = mode;
        EventBus.publish(new BoundCreationModeChanged(mode));
    }

    private void initPointListener() {
        EventBus.subscribe(PointPanelRepaintQueued.class,
                (BusEventListener<PointPanelRepaintQueued<BoundPoint>>) event -> this.repaint());
        EventBus.subscribe(PointAddConfirmed.class,
                        (BusEventListener<PointAddConfirmed<BoundPoint>>) event ->
                                model.addElement(event.point()));
        EventBus.subscribe(BoundInsertedConfirmed.class,
                event -> model.insertElementAt(event.toInsert(),
                        event.precedingIndex()));
        EventBus.subscribe(PointRemovedConfirmed.class,
                (BusEventListener<PointRemovedConfirmed<BoundPoint>>) event ->
                        model.removeElement(event.point())
        );
    }

    private void createModeButtons(JPanel modePanel) {
        selectModeButton = new JToggleButton(FontIcon.of(FluentUiRegularMZ.SELECT_OBJECT_20, 16));
        selectModeButton.setToolTipText("Select, move and delete.");
        modePanel.add(selectModeButton);
        createModeButton = new JToggleButton(FontIcon.of(FluentUiRegularAL.ADD_CIRCLE_20, 16));
        createModeButton.setToolTipText("Create new points.");
        modePanel.add(createModeButton);
        ButtonGroup group = new ButtonGroup();
        group.add(selectModeButton);
        group.add(createModeButton);
        selectModeButton.setSelected(false);
        createModeButton.setSelected(false);
    }

    private void initModeButtonListeners() {
        selectModeButton.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                BoundPointsPanel.this.setMode(InteractionMode.SELECT);
                EventBus.publish(new ViewerRotationToggled(true, true));
            }
        });
        createModeButton.addItemListener(ev -> {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
                BoundPointsPanel.this.setMode(InteractionMode.CREATE);
                EventBus.publish(new ViewerRotationToggled(false, false));
            }
        });
        EventBus.subscribe(ViewerRotationToggled.class, event -> {
            if (event.isSelected()) {
                BoundPointsPanel.this.setMode(InteractionMode.SELECT);
                selectModeButton.setSelected(true);
            }
        });
    }

}
