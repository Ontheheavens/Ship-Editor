package oth.shipeditor.components.help;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
final class Articles {

    private Articles() {}

    static DefaultMutableTreeNode createDevelopmentSection() {
        DefaultMutableTreeNode packageRoot = new DefaultMutableTreeNode("Development Notes");

        packageRoot.add(Articles.createArticle("Weapon Slots Design", article -> {
            article.addTitle("Why weapon slots are displayed the way they are?");
            article.addSectionSeparator("Context");
            article.addTextBlock("Old editor by Trylobot had an extensive system for visual display of weapon slots. " +
                    "Perhaps too extensive, as it was trying to faithfully recreate an in-game visuals of slots.", 4);
            article.addSectionSeparator("Upshot");
            article.addTextBlock("This editor forfeits vanilla look of weapon " +
                    "slots in favor of more streamlined design.");
            article.addTextBlock("For example, weapon type is denoted strictly by color, size - by number of shapes, " +
                    "while mount - by shape of point.");
        }));

        return packageRoot;
    }

    @SuppressWarnings("SameParameterValue")
    private static MutableTreeNode createArticle(String title, Consumer<HelpArticle> content) {
        HelpArticle article = new HelpArticle(title);
        content.accept(article);
        return new DefaultMutableTreeNode(article);
    }

}
