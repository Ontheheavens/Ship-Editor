package oth.shipeditor.components.viewer.painters.points;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.points.LaunchBayAddConfirmed;
import oth.shipeditor.communication.events.viewer.points.LaunchBayRemoveConfirmed;
import oth.shipeditor.communication.events.viewer.points.PointCreationQueued;
import oth.shipeditor.components.instrument.ship.ShipInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.entities.bays.LaunchBay;
import oth.shipeditor.components.viewer.entities.bays.LaunchPortPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.DrawUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
@Log4j2
public class LaunchBayPainter extends MirrorablePointPainter {

    private final List<LaunchPortPoint> portsIndex;

    @Getter
    private final List<LaunchBay> baysList;

    @Getter
    private static boolean addPortHotkeyPressed;

    @Getter
    private static boolean addBayHotkeyPressed;

    private final int addPortHotkey = KeyEvent.VK_P;
    private final int addBayHotkey = KeyEvent.VK_B;

    private KeyEventDispatcher hotkeyDispatcher;

    public LaunchBayPainter(ShipPainter parent) {
        super(parent);
        this.portsIndex = new ArrayList<>();
        this.baysList = new ArrayList<>();
        this.initHotkeys();
    }

    @Override
    protected ShipInstrument getInstrumentType() {
        return ShipInstrument.LAUNCH_BAYS;
    }

    @Override
    public void cleanupListeners() {
        super.cleanupListeners();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(hotkeyDispatcher);
    }

    @Override
    protected void handleCreation(PointCreationQueued event) {
        ShipPainter parentLayer = this.getParentLayer();
        Point2D position = event.position();
        String generatedID = this.generateUniqueBayID();
        if (addPortHotkeyPressed) {
            LaunchPortPoint selected = (LaunchPortPoint) this.getSelected();
            if (selected != null) {
                LaunchBay selectedBay = selected.getParentBay();
                LaunchPortPoint newPort = new LaunchPortPoint(position, parentLayer, selectedBay);
                EditDispatch.postPointAdded(this, newPort);
            } else {
                LaunchBay newBay = new LaunchBay(generatedID, this);
                LaunchPortPoint newPort = new LaunchPortPoint(position, parentLayer, newBay);
                EditDispatch.postPointAdded(this, newPort);
                this.setSelected(newPort);
            }
        } else if (addBayHotkeyPressed) {
            LaunchBay newBay = new LaunchBay(generatedID, this);
            LaunchPortPoint newPort = new LaunchPortPoint(position, parentLayer, newBay);
            EditDispatch.postPointAdded(this, newPort);
        }
    }

    private String generateUniqueBayID() {
        ShipPainter parentLayer = getParentLayer();
        return parentLayer.generateUniqueSlotID("LB");
    }

    @SuppressWarnings("DuplicatedCode")
    private void initHotkeys() {
        hotkeyDispatcher = ke -> {
            int keyCode = ke.getKeyCode();
            boolean isPortHotkey = (keyCode == addPortHotkey);
            boolean isBayHotkey = (keyCode == addBayHotkey);
            switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (isPortHotkey) {
                        addPortHotkeyPressed = true;
                        EventBus.publish(new ViewerRepaintQueued());
                    } else if (isBayHotkey) {
                        addBayHotkeyPressed = true;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (isPortHotkey) {
                        addPortHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    } else if (isBayHotkey) {
                        addBayHotkeyPressed = false;
                        EventBus.publish(new ViewerRepaintQueued());
                    }
                    break;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(hotkeyDispatcher);
    }

    @Override
    protected void handlePointSelectionEvent(BaseWorldPoint point) {
        if (addPortHotkeyPressed) return;
        super.handlePointSelectionEvent(point);
    }

    @Override
    public List<LaunchPortPoint> getPointsIndex() {
        return portsIndex;
    }

    public void addBay(LaunchBay bay) {
        baysList.add(bay);
        EventBus.publish(new LaunchBayAddConfirmed(bay));
    }

    private void removeBay(LaunchBay bay) {
        baysList.remove(bay);
        EventBus.publish(new LaunchBayRemoveConfirmed(bay));
    }

    @Override
    protected void addPointToIndex(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            LaunchBay targetBay = checked.getParentBay();
            if (!baysList.contains(targetBay)) {
                this.addBay(targetBay);
            }
            List<LaunchPortPoint> portPoints = targetBay.getPortPoints();
            portPoints.add(checked);
            portsIndex.add(checked);
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    protected void removePointFromIndex(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            portsIndex.remove(checked);
            LaunchBay parentBay = checked.getParentBay();
            List<LaunchPortPoint> portPoints = parentBay.getPortPoints();
            portPoints.remove(checked);
            if (portPoints.isEmpty()) {
                this.removeBay(parentBay);
            }
        } else {
            throwIllegalPoint();
        }
    }

    @Override
    public int getIndexOfPoint(BaseWorldPoint point) {
        if (point instanceof LaunchPortPoint checked) {
            return portsIndex.indexOf(checked);
        } else {
            throwIllegalPoint();
            return -1;
        }
    }

    @Override
    protected Class<? extends BaseWorldPoint> getTypeReference() {
        return LaunchPortPoint.class;
    }

    @Override
    public void insertPoint(BaseWorldPoint toInsert, int precedingIndex) {
        throwIllegalPoint();
    }

    @Override
    public void paintPainterContent(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        if (!isInteractionEnabled()) return;
        Point2D finalWorldCursor = StaticController.getFinalWorldCursor();
        Point2D finalScreenCursor = worldToScreen.transform(finalWorldCursor, null);
        WorldPoint selected = this.getSelected();
        if (selected != null) {
            Point2D selectedPosition = worldToScreen.transform(selected.getPosition(), null);
            DrawUtilities.drawScreenLine(g, selectedPosition, finalScreenCursor, Color.BLACK, 4.0f);
            DrawUtilities.drawScreenLine(g, selectedPosition, finalScreenCursor, Color.LIGHT_GRAY, 2.0f);
        }
    }

}
