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
public class ArticleTextBlock implements ArticlePart {

    private String text;

    private int pad;

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public JPanel createContent() {
        return ComponentUtilities.createTextPanel(text, pad);
    }

    @Override
    public ArticleType getType() {
        return ArticleType.TEXT_BLOCK;
    }

}
