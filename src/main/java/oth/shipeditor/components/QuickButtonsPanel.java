package oth.shipeditor.components;

import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.swing.FontIcon;
import oth.shipeditor.undo.UndoOverseer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 06.08.2023
 */
public class QuickButtonsPanel extends JPanel {

    QuickButtonsPanel() {
        this.add(QuickButtonsPanel.createUndoRedoButtons());
        this.setBackground(Color.LIGHT_GRAY);
    }

    private static JPanel createUndoRedoButtons() {
        JPanel buttonsContainer = new JPanel();

        JButton undo = new JButton();
        undo.setAction(UndoOverseer.getUndoAction());
        undo.setHideActionText(true);
        Icon undoIcon = FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16);
        undo.setIcon(undoIcon);
        undo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_UNDO_20, 16, Color.GRAY));

        buttonsContainer.add(undo);

        JButton redo = new JButton();
        redo.setAction(UndoOverseer.getRedoAction());
        redo.setHideActionText(true);
        Icon redoIcon = FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16);
        redo.setIcon(redoIcon);
        redo.setDisabledIcon(FontIcon.of(FluentUiRegularAL.ARROW_REDO_20, 16, Color.GRAY));

        buttonsContainer.add(redo);

        return buttonsContainer;
    }

}
