package oth.shipeditor.utility.components.containers;

import lombok.Getter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.EventScheduler;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * As of October 2023 this class is the preferred way of implementing various property panels like hull/skin data values.
 * Uses GridBagLayout, handles refresh and population of values on layer selection.
 * @author Ontheheavens
 * @since 29.10.2023
 */
@SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
@Getter
public abstract class PropertiesPanel extends JPanel {

    private final Map<JComponent, Consumer<ViewerLayer>> clearingListeners;

    private final Map<JComponent, Consumer<ViewerLayer>> refresherListeners;

    private boolean widgetsReadyForInput;

    private ViewerLayer cachedLayer;

    protected PropertiesPanel() {
        clearingListeners = new LinkedHashMap<>();
        refresherListeners = new LinkedHashMap<>();
        this.setLayout(new BorderLayout());
        this.populateContent();
    }

    protected void installWidgets(Map<JLabel, JComponent> widgets) {
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new GridBagLayout());
        ComponentUtilities.outfitPanelWithTitle(contentContainer, "Skin data");

        int ordering = 0;
        for (Map.Entry<JLabel, JComponent> entry : widgets.entrySet()) {
            JLabel label = entry.getKey();
            JComponent widget = entry.getValue();
            ComponentUtilities.addLabelAndComponent(contentContainer, label, widget, ordering);
            ordering++;
        }

        this.add(contentContainer, BorderLayout.PAGE_START);
    }

    public void refresh(ViewerLayer layer) {
        cachedLayer = layer;
        widgetsReadyForInput = false;
        this.refreshContent(layer);
        widgetsReadyForInput = true;
    }

    public abstract void refreshContent(ViewerLayer layer);

    protected abstract void populateContent();

    protected void processChange() {
        this.refresh(cachedLayer);
        EventScheduler repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueActiveLayerUpdate();
    }

    protected void fireClearingListeners(ViewerLayer layer) {
        clearingListeners.forEach((widget, clearer) -> clearer.accept(layer));
    }

    protected void fireRefresherListeners(ViewerLayer layer) {
        refresherListeners.forEach((widget, refresher) -> refresher.accept(layer));
    }

    protected void registerWidgetListeners(JComponent widget,
                                           Consumer<ViewerLayer> clearer,
                                           Consumer<ViewerLayer> refresher) {
        clearingListeners.put(widget, clearer);
        refresherListeners.put(widget, refresher);
    }


}
