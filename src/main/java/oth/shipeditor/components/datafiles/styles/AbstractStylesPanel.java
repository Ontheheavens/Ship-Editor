package oth.shipeditor.components.datafiles.styles;

import lombok.Getter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 19.08.2023
 */
public abstract class AbstractStylesPanel extends JPanel {

    static final String ILLEGAL_STYLE_ARGUMENT = "Illegal style argument!";
    @Getter
    private final JPanel scrollerContent;

    AbstractStylesPanel() {
        this.setLayout(new BorderLayout());

        JPanel topContainer = this.createTopPanel();
        topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        this.add(topContainer, BorderLayout.PAGE_START);

        this.scrollerContent = new JPanel();
        scrollerContent.setLayout(new BoxLayout(scrollerContent, BoxLayout.PAGE_AXIS));
        JScrollPane scroller = new JScrollPane(scrollerContent);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(12);
        this.add(scroller, BorderLayout.CENTER);
        this.initListeners();
    }

    protected abstract JPanel createTopPanel();

    protected abstract void initListeners();

    JPanel createStylePanel(Object style) {
        JPanel stylePanel = new JPanel();
        stylePanel.setLayout(new BoxLayout(stylePanel, BoxLayout.PAGE_AXIS));

        Insets outsideInsets = new Insets(2, 2, 2, 2);
        Border border = ComponentUtilities.createRoundCompoundBorder(outsideInsets, true);
        Insets insets = new Insets(6, 6, 6, 6);
        stylePanel.setBorder(BorderFactory.createCompoundBorder(border,
                new EmptyBorder(insets)));

        JPanel titleContainer = this.createStyleTitlePanel(style);
        stylePanel.add(titleContainer);

        JPanel contentContainer = this.createStyleContentPanel(style);
        stylePanel.add(contentContainer);

        return stylePanel;
    }

    protected abstract JPanel createStyleTitlePanel(Object style);

    protected abstract JPanel createStyleContentPanel(Object style);

}
