package oth.shipeditor.components.datafiles;

import com.formdev.flatlaf.ui.FlatRoundBorder;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.HullStylesLoaded;
import oth.shipeditor.menubar.FileUtilities;
import oth.shipeditor.representation.HullStyle;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.MouseoverLabelListener;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 23.07.2023
 */
class HullStylesPanel extends JPanel {

    private final JPanel scrollerContent;

    HullStylesPanel() {
        this.setLayout(new BorderLayout());
        this.scrollerContent = new JPanel();
        scrollerContent.setLayout(new BoxLayout(scrollerContent, BoxLayout.PAGE_AXIS));
        JScrollPane scroller = new JScrollPane(scrollerContent);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(12);
        this.add(scroller, BorderLayout.CENTER);
        this.initListeners();
    }

    private void initListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof HullStylesLoaded checked) {
                populatePanel(checked.hullStyles());
            }
        });
    }

    private void populatePanel(Map<String, HullStyle> hullStyles) {
        for (HullStyle style : hullStyles.values()) {
            scrollerContent.add(HullStylesPanel.createHullStylePanel(style));
        }
    }

    private static JPanel createHullStylePanel(HullStyle style) {
        JPanel stylePanel = new JPanel();
        stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.PAGE_AXIS));

        Insets outsideInsets = new Insets(2, 2, 2, 2);
        Border border = ComponentUtilities.createRoundCompoundBorder(outsideInsets, true);
        Insets insets = new Insets(6, 6, 6, 6);
        stylePanel.setBorder(BorderFactory.createCompoundBorder(border,
                new EmptyBorder(insets)));

        JPanel titleContainer = HullStylesPanel.createStyleTitlePanel(style);
        stylePanel.add(titleContainer);

        JPanel contentContainer = HullStylesPanel.createStyleContentPanel(style);
        stylePanel.add(contentContainer);

        return stylePanel;
    }

    private static JPanel createStyleTitlePanel(HullStyle style) {
        JLabel styleLabel = new JLabel(style.getHullStyleID());
        Insets empty = new Insets(0, 3, 2, 4);
        styleLabel.setBorder(ComponentUtilities.createLabelSimpleBorder(empty));

        Path filePath = style.getFilePath();
        JPopupMenu contextMenu = ComponentUtilities.createPathContextMenu(filePath);

        JMenuItem openContainingPackage = new JMenuItem(StringValues.OPEN_DATA_PACKAGE);
        openContainingPackage.addActionListener(e -> FileUtilities.openPathInDesktop(style.getContainingPackage()));
        contextMenu.add(openContainingPackage);

        styleLabel.addMouseListener(new MouseoverLabelListener(contextMenu, styleLabel, Color.GRAY));
        styleLabel.setToolTipText(filePath.toString());

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.LINE_AXIS));
        titleContainer.add(styleLabel);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));

        titleContainer.add(separator);

        titleContainer.setBorder(new FlatRoundBorder());
        titleContainer.setBackground(Color.LIGHT_GRAY);
        return titleContainer;
    }

    private static JPanel createStyleContentPanel(HullStyle style) {
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.PAGE_AXIS));

        JPanel ringColorPanel = ComponentUtilities.createColorPropertyPanel("Shield ring color:",
                style.getShieldRingColor());
        contentContainer.add(ringColorPanel);

        JPanel innerColorPanel = ComponentUtilities.createColorPropertyPanel("Shield inner color:",
                style.getShieldInnerColor());
        contentContainer.add(innerColorPanel);

        return contentContainer;
    }

}
