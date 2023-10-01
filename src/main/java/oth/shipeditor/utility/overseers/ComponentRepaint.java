package oth.shipeditor.utility.overseers;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.*;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 04.09.2023
 */
public class ComponentRepaint {

    private boolean viewerRepaintQueued;

    private boolean centerPanelsRepaintQueued;

    private boolean boundsPanelRepaintQueued;

    private boolean baysPanelRepaintQueued;

    private boolean enginesPanelRepaintQueued;

    private boolean skinPanelRepaintQueued;

    private boolean slotControlRepaintQueued;

    private boolean slotsPanelRepaintQueued;

    private boolean builtInsPanelRepaintQueued;

    private boolean variantsPanelRepaintQueued;

    ComponentRepaint() {
        Timer repaintTimer = new Timer(2, e -> {
            if (viewerRepaintQueued) {
                EventBus.publish(new ViewerRepaintQueued());
                viewerRepaintQueued = false;
            }
            if (centerPanelsRepaintQueued) {
                EventBus.publish(new CenterPanelsRepaintQueued());
                centerPanelsRepaintQueued = false;
            }
            if (boundsPanelRepaintQueued) {
                EventBus.publish(new BoundsPanelRepaintQueued());
                boundsPanelRepaintQueued = false;
            }
            if (baysPanelRepaintQueued) {
                EventBus.publish(new BaysPanelRepaintQueued());
                baysPanelRepaintQueued = false;
            }
            if (enginesPanelRepaintQueued) {
                EventBus.publish(new EnginesPanelRepaintQueued());
                enginesPanelRepaintQueued = false;
            }
            if (skinPanelRepaintQueued) {
                EventBus.publish(new SkinPanelRepaintQueued());
                skinPanelRepaintQueued = false;
            }
            if (slotControlRepaintQueued) {
                EventBus.publish(new SlotControlRepaintQueued());
                slotControlRepaintQueued = false;
            }
            if (slotsPanelRepaintQueued) {
                EventBus.publish(new SlotsPanelRepaintQueued());
                slotsPanelRepaintQueued = false;
            }
            if (builtInsPanelRepaintQueued) {
                EventBus.publish(new BuiltInsPanelsRepaintQueued());
                builtInsPanelRepaintQueued = false;
            }
            if (variantsPanelRepaintQueued) {
                EventBus.publish(new VariantPanelRepaintQueued());
                variantsPanelRepaintQueued = false;
            }
        });
        repaintTimer.setRepeats(true);
        repaintTimer.start();
    }

    public void queueViewerRepaint() {
        this.viewerRepaintQueued = true;
    }

    public void queueCenterPanelsRepaint() {
        this.centerPanelsRepaintQueued = true;
    }

    public void queueBoundsPanelRepaint() {
        this.boundsPanelRepaintQueued = true;
    }

    public void queueBaysPanelRepaint() {
        this.baysPanelRepaintQueued = true;
    }

    public void queueEnginesPanelRepaint() {
        this.enginesPanelRepaintQueued = true;
    }

    @SuppressWarnings("unused")
    public void queueSkinPanelRepaint() {
        this.skinPanelRepaintQueued = true;
    }

    public void queueSlotControlRepaint() {
        this.slotControlRepaintQueued = true;
    }

    @SuppressWarnings("unused")
    public void queueSlotsPanelRepaint() {
        this.slotsPanelRepaintQueued = true;
    }

    public void queueBuiltInsRepaint() {
        this.builtInsPanelRepaintQueued = true;
    }

    public void queueVariantsRepaint() {
        this.variantsPanelRepaintQueued = true;
    }

}
