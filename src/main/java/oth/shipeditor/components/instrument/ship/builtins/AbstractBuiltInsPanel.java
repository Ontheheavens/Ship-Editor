package oth.shipeditor.components.instrument.ship.builtins;

import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.ScrollableHeightContainer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
@Getter
public abstract class AbstractBuiltInsPanel extends JPanel {

    static final String REMOVED_BY_SKIN = "Removed by skin";

    @Setter
    private ShipLayer cachedLayer;

    private final JPanel contentPane;

    protected AbstractBuiltInsPanel() {
        this.setLayout(new BorderLayout());

        this.addHintPanel();

        contentPane = createContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        JScrollPane scrollContainer = new JScrollPane(contentPane);
        JScrollBar verticalScrollBar = scrollContainer.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(12);
        this.add(scrollContainer, BorderLayout.CENTER);

        this.initLayerListeners();
    }

    protected void addHintPanel() {
        FontIcon hintIcon = FontIcon.of(FluentUiRegularAL.INFO_28, 28);
        JPanel hintPanel = ComponentUtilities.createHintPanel(getHintText(), hintIcon);
        this.add(hintPanel, BorderLayout.PAGE_START);
    }

    protected JPanel createContentPane() {
        return new ScrollableHeightContainer();
    }

    protected String getHintText() {
        return "Use right-click context menu of " +
                "game data widget to add entries.";
    }

    protected void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                contentPane.removeAll();
                ViewerLayer selected = checked.selected();
                if (!(selected instanceof ShipLayer checkedLayer)) return;
                ShipPainter painter = checkedLayer.getPainter();
                if (painter == null || painter.isUninitialized()) {
                    this.installPlaceholderLabel("Layer data not initialized");
                    return;
                }
                this.cachedLayer = checkedLayer;
                this.refreshPanel(checkedLayer);
            }
        });
    }

    protected void installPlaceholderLabel(String text) {
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(AbstractBuiltInsPanel.createPlaceholderLabel(text), BorderLayout.CENTER);
    }

    protected static JPanel createPlaceholderLabel(String text) {
        var emptyContainer = new JPanel();
        emptyContainer.setBorder(new EmptyBorder(8, 2, 6, 2));
        emptyContainer.setLayout(new BoxLayout(emptyContainer, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        emptyContainer.add(Box.createHorizontalGlue());
        emptyContainer.add(label);
        emptyContainer.add(Box.createHorizontalGlue());
        return emptyContainer;
    }

    protected abstract void refreshPanel(ShipLayer layer);

}
