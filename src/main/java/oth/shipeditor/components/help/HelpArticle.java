package oth.shipeditor.components.help;

import lombok.Getter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
@SuppressWarnings("SameParameterValue")
public class HelpArticle {

    @Getter
    private final List<JPanel> articleParts;

    private final String name;

    HelpArticle(String displayedName) {
        this.articleParts = new ArrayList<>();
        this.name = displayedName;
    }

    void addTitle(String titleText) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setBorder(new EmptyBorder(4, 0, 6, 0));
        titleLabel.setFont(Utility.getOrbitron(14));
        container.add(titleLabel);
        container.setAlignmentY(0);
        articleParts.add(container);
    }

    void addSectionSeparator(String titleText) {
        JPanel container = ComponentUtilities.createTitledSeparatorPanel(titleText,
                new Insets(1, 0, 0, 0));
        container.setAlignmentY(0);
        articleParts.add(container);
    }

    void addTextBlock(String text) {
        addTextBlock(text, 0);
    }

    void addTextBlock(String text, int pad) {
        TextScrollPanel container = new TextScrollPanel(new FlowLayout());
        container.setBorder(new EmptyBorder(0, 0, pad, 0));
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMinimumSize(new Dimension(100,100));
        container.setMinimumSize(new Dimension(100,100));

        container.add(textArea);
        container.setAlignmentY(0);
        articleParts.add(container);
    }

    @Override
    public String toString() {
        return name;
    }

    @SuppressWarnings("ProtectedInnerClass")
    protected static class TextScrollPanel extends JPanel implements Scrollable {

        TextScrollPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 0;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 0;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    }

}
