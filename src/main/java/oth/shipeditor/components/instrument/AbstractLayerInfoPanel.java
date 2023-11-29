package oth.shipeditor.components.instrument;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 09.11.2023
 */
public abstract class AbstractLayerInfoPanel extends JPanel {

    private final LayerCircumstancePanel layerCircumstancePanel;

    protected AbstractLayerInfoPanel() {
        this.setLayout(new BorderLayout());

        layerCircumstancePanel = new LayerCircumstancePanel();
        this.add(layerCircumstancePanel, BorderLayout.PAGE_START);

        this.initListeners();
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                handleLayerSelected(checked.selected());
            } else if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == EditorInstrument.LAYER) {
                    handleLayerSelected(StaticController.getActiveLayer());
                }
            }
        });
    }

    private void handleLayerSelected(ViewerLayer selected) {
        clearData();

        boolean layerPainterPresent = selected != null && selected.getPainter() != null;
        if (!layerPainterPresent) {
            layerCircumstancePanel.refresh(null);
            return;
        }
        LayerPainter layerPainter = selected.getPainter();
        if (!isValidLayer(layerPainter)) {
            layerCircumstancePanel.refresh(null);
            return;
        }

        this.refreshData(selected);
        layerCircumstancePanel.refresh(selected.getPainter());
    }

    protected abstract boolean isValidLayer(LayerPainter layerPainter);

    protected abstract void clearData();

    /**
     * @param selected passed checks and is guaranteed to be valid layer.
     */
    protected abstract void refreshData(ViewerLayer selected);

}
