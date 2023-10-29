package oth.shipeditor.components.help.parts;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 29.10.2023
 */
@Getter @Setter
public class ArticleSeparator implements ArticlePart{

    private String title;

    @Override
    public JPanel createContent() {
        JPanel container = ComponentUtilities.createTitledSeparatorPanel(title);
        container.setAlignmentY(0);
        return container;
    }

    @Override
    public ArticleType getType() {
        return ArticleType.SEPARATOR;
    }

}
