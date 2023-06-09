package oth.shipeditor.components.instrument;

import lombok.Getter;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 08.06.2023
 */
public final class InstrumentTabsPane extends JTabbedPane {

    /**
     * Panel for data representation of ship bounds.
     */
    @Getter
    private final BoundPointsPanel boundsPanel;

    private final HullPointsPanel centerPointsPanel;

    public InstrumentTabsPane() {
        this.setTabPlacement(SwingConstants.LEFT);
        centerPointsPanel = new HullPointsPanel();
        this.addTab("Centers",centerPointsPanel);
        boundsPanel = new BoundPointsPanel();
        this.addTab("Bounds", boundsPanel);
    }

}
