package oth.shipeditor.components.instrument.ship;

import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.ScrollableHeightContainer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 27.08.2023
 */
public abstract class AbstractBuiltInsPanel<T extends CSVEntry> extends JPanel {

    @Getter @Setter
    private ShipLayer cachedLayer;

    @Getter
    private final JPanel contentPane;

    AbstractBuiltInsPanel() {
        this.setLayout(new BorderLayout());
        JPanel hintPanel = AbstractBuiltInsPanel.createHintPanel();
        this.add(hintPanel, BorderLayout.PAGE_START);

        contentPane = new ScrollableHeightContainer();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        JScrollPane scrollContainer = new JScrollPane(contentPane);
        this.add(scrollContainer, BorderLayout.CENTER);

        this.initLayerListeners();
    }

    private void initLayerListeners() {
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

    void installPlaceholderLabel(String text) {
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(AbstractBuiltInsPanel.createPlaceholderLabel(text), BorderLayout.CENTER);
    }

    static JPanel createPlaceholderLabel(String text) {
        var emptyContainer = new JPanel();
        emptyContainer.setBorder(new EmptyBorder(6, 2, 6, 2));
        emptyContainer.setLayout(new BoxLayout(emptyContainer, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        emptyContainer.add(Box.createHorizontalGlue());
        emptyContainer.add(label);
        emptyContainer.add(Box.createHorizontalGlue());
        return emptyContainer;
    }

    protected abstract void refreshPanel(ShipLayer layer);

    private static JPanel createHintPanel() {
        JPanel hintPanel = new JPanel();
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.LINE_AXIS));

        JLabel hintIcon = new JLabel(FontIcon.of(FluentUiRegularAL.INFO_28, 28));
        hintIcon.setBorder(new EmptyBorder(4, 4, 0, 0));
        hintIcon.setAlignmentY(0.5f);
        hintPanel.add(hintIcon);

        JPanel hintInfo = ComponentUtilities.createTextPanel("Use right-click context menu of " +
                "game data widget to add entries.", 2);
        hintInfo.setBorder(new EmptyBorder(4, 0, 4, 4));
        hintInfo.setAlignmentY(0.5f);
        hintPanel.add(hintInfo);

        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return hintPanel;
    }

    void handleSkinChanges(List<T> entryList, Color panelColor) {
        if (entryList != null && !entryList.isEmpty()) {
            contentPane.add(Box.createVerticalStrut(2));
            JPanel title = ComponentUtilities.createTitledSeparatorPanel("Added by skin");
            title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
            title.setAlignmentY(0);
            contentPane.add(title);

            this.populateWithEntries(contentPane, entryList, panel -> {
                if (panelColor != null) {
                    panel.setBackground(panelColor);
                }
            });
        }
    }

    protected abstract  void populateWithEntries(JPanel container, List<T> entryList,
                                     Consumer<JPanel> panelMutator);


}
