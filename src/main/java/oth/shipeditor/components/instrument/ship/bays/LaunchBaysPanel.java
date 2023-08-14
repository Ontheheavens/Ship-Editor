package oth.shipeditor.components.instrument.ship.bays;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BaysPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.communication.events.viewer.points.LaunchBayAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.LaunchBayRemoveConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointRemovedConfirmed;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.LaunchBayPainter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
@Log4j2
public class LaunchBaysPanel extends JPanel {

    private final LaunchBaysTree baysTree;

    LaunchBaysPanel() {
        this.setLayout(new BorderLayout());

        baysTree = new LaunchBaysTree(new DefaultMutableTreeNode("Bays"));

        JScrollPane scrollableContainer = new JScrollPane(baysTree);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.PAGE_AXIS));
        northContainer.add(new JPanel());

        ComponentUtilities.addSeparatorToBoxPanel(northContainer);

        northContainer.add(new JPanel());

        this.add(northContainer, BorderLayout.PAGE_START);

        this.add(scrollableContainer, BorderLayout.CENTER);
        this.initPointListener();
        this.initLayerListeners();
    }

    private void initPointListener() {
        EventBus.subscribe(event -> {
            if (event instanceof BaysPanelRepaintQueued) {
                this.repaint();
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayAddConfirmed checked) {
                baysTree.addBay(checked.added());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof LaunchBayRemoveConfirmed checked) {
                baysTree.removeBay(checked.removed());
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointAddConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.addPort(point);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof PointRemovedConfirmed checked && checked.point() instanceof LaunchPortPoint point) {
                baysTree.removePort(point);
            }
        });
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                baysTree.clearRoot();
                if (!(selected instanceof ShipLayer checkedLayer)) {
                    return;
                }
                ShipPainter painter = checkedLayer.getPainter();
                if (painter != null && !painter.isUninitialized()) {
                    LaunchBayPainter bayPainter = painter.getBayPainter();
                    baysTree.repopulateTree(bayPainter);
                    baysTree.repaint();
                }
            }
        });
    }

}
