package oth.shipeditor.components.instrument.ship.skins;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SkinPanelRepaintQueued;
import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 30.07.2023
 */
public class SkinPanel extends JPanel {

    public SkinPanel() {
        this.setLayout(new BorderLayout());

        JTabbedPane tabContainer = new JTabbedPane(SwingConstants.BOTTOM);
        tabContainer.addTab("Skin List", new SkinListPanel());
        tabContainer.addTab("Slot Changes", new SkinSlotOverridesPanel());
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabContainer.putClientProperty(StringConstants.JTABBED_PANE_TAB_AREA_ALIGNMENT, "fill");
        this.add(tabContainer, BorderLayout.CENTER);
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof SkinPanelRepaintQueued) {
                this.repaint();
            }
        });
    }

}
