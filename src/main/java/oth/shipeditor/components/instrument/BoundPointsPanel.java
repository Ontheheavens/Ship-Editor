package oth.shipeditor.components.instrument;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BoundsPanelRepaintQueued;
import oth.shipeditor.communication.events.components.ViewerFocusRequestQueued;
import oth.shipeditor.communication.events.viewer.control.PointLinkageToleranceChanged;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.BoundInsertedConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.entities.BoundPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ShipLayer;
import oth.shipeditor.components.viewer.painters.BoundPointsPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
@Log4j2
public final class BoundPointsPanel extends JPanel {

    @Getter
    private final BoundList boundPointContainer;


    private final DefaultListModel<BoundPoint> model = new DefaultListModel<>();

    public BoundPointsPanel() {
        this.setLayout(new BorderLayout());
        boundPointContainer = new BoundList(model);
        JScrollPane scrollableContainer = new JScrollPane(boundPointContainer);
        Dimension listSize = new Dimension(boundPointContainer.getPreferredSize().width + 30,
                boundPointContainer.getPreferredSize().height);
        scrollableContainer.setPreferredSize(listSize);
        this.add(this.createPointLinkageToleranceSpinner(), BorderLayout.PAGE_START);
        this.add(scrollableContainer, BorderLayout.CENTER);

        this.initPointListener();
        this.initLayerListeners();
    }

    private JPanel createPointLinkageToleranceSpinner() {
        JPanel container = new JPanel();
        Integer[] numbers = {0, 1, 2, 3, 4, 5};
        SpinnerListModel model =  new SpinnerListModel(numbers);
        JSpinner spinner = new JSpinner(model);
        JLabel toleranceLabel = new JLabel("Mirror linkage tolerance:");
        container.add(toleranceLabel);
        container.add(spinner);
        spinner.addChangeListener(e -> {
            Integer current = (Integer) model.getValue();
            EventBus.publish(new PointLinkageToleranceChanged(current));
        });
        spinner.setValue(1);
        spinner.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                EventBus.publish(new ViewerFocusRequestQueued());
            }
        });
        return container;
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BoundsPanelRepaintQueued) {
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.addElement(point);
                boundPointContainer.setSelectedIndex(model.indexOf(point));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof BoundInsertedConfirmed checked) {
                model.insertElementAt(checked.toInsert(), checked.precedingIndex());
                boundPointContainer.setSelectedIndex(model.indexOf(checked.toInsert()));
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof BoundPoint point) {
                model.removeElement(point);
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ShipLayer old = checked.old();
                if (old != null && old.getPainter() != null) {
                    LayerPainter oldLayerPainter = old.getPainter();
                    BoundPointsPainter oldBoundsPainter = oldLayerPainter.getBoundsPainter();
                    Iterable<BoundPoint> oldBounds = new ArrayList<>(oldBoundsPainter.getBoundPoints());
                    for (BoundPoint bound : oldBounds) {
                        model.removeElement(bound);
                    }
                }
                ShipLayer selected = checked.selected();
                if (selected != null && selected.getPainter() != null) {
                    LayerPainter selectedLayerPainter = selected.getPainter();
                    BoundPointsPainter newBoundsPainter = selectedLayerPainter.getBoundsPainter();
                    Iterable<BoundPoint> newBounds = new ArrayList<>(newBoundsPainter.getBoundPoints());
                    for (BoundPoint bound : newBounds) {
                        model.addElement(bound);
                    }
                }
            }
        });
    }

}
