package oth.shipeditor.components.layering;

import lombok.Getter;
import oth.shipeditor.components.viewer.layers.ViewerLayer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 02.09.2023
 */
public abstract class LayerTab extends JPanel {

    @Getter
    private final ViewerLayer associatedLayer;

    LayerTab(ViewerLayer layer) {
        this.associatedLayer = layer;
        this.setLayout(new BorderLayout());
    }

    public abstract String getTabTooltip();

}
