package oth.shipeditor.components.help;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import oth.shipeditor.components.help.parts.ArticlePart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
@SuppressWarnings("SameParameterValue")
class HelpArticle {

    @Getter
    private final List<ArticlePart> articleParts;

    private final String name;

    @JsonCreator
    HelpArticle(@JsonProperty("name") String displayedName,
                @JsonProperty("articleParts") List<ArticlePart> partList) {
        this.articleParts = new ArrayList<>(partList);
        this.name = displayedName;
    }

    void addContentPart(ArticlePart part) {
        articleParts.add(part);
    }

    @Override
    public String toString() {
        return name;
    }

}
