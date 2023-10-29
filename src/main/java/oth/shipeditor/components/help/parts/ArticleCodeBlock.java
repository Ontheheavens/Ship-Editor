package oth.shipeditor.components.help.parts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 29.10.2023
 */
@Getter
@Setter
public class ArticleCodeBlock implements ArticlePart {

    private String code;

    @JsonCreator
    public ArticleCodeBlock(@JsonProperty("code") String inputCode) {
        this.code = inputCode;
    }

    @Override
    public String getTitle() {
        return "Code:";
    }

    @Override
    public JPanel createContent() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        JTextArea codeArea = new JTextArea(code);
        codeArea.setEditable(false);
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(codeArea);

        container.add(scrollPane);

        return container;
    }

    @Override
    public ArticleType getType() {
        return ArticleType.CODE_BLOCK;
    }
}
