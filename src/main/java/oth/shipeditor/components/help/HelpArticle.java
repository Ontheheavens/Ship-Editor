package oth.shipeditor.components.help;

import lombok.Getter;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.TextScrollPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
@SuppressWarnings("SameParameterValue")
class HelpArticle {

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
        JPanel container = ComponentUtilities.createTitledSeparatorPanel(titleText);
        container.setAlignmentY(0);
        articleParts.add(container);
    }

    void addTextBlock(String text) {
        addTextBlock(text, 0);
    }

    void addTextBlock(String text, int pad) {
        TextScrollPanel container = ComponentUtilities.createTextPanel(text, pad);
        articleParts.add(container);
    }

    @Override
    public String toString() {
        return name;
    }

}
