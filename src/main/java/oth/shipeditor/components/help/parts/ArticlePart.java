package oth.shipeditor.components.help.parts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 29.10.2023
 */
@SuppressWarnings("ClassReferencesSubclass")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ArticleTitle.class, name = "TITLE"),
        @JsonSubTypes.Type(value = ArticleSeparator.class, name = "SEPARATOR"),
        @JsonSubTypes.Type(value = ArticleTextBlock.class, name = "TEXT_BLOCK"),
        @JsonSubTypes.Type(value = ArticleCodeBlock.class, name = "CODE_BLOCK")
})
public interface ArticlePart {

    String getTitle();

    JPanel createContent();

    ArticleType getType();

}
