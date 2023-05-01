package oth.shipeditor.components;

import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class ViewerStatusPanel extends JPanel {

    JLabel dimensionsLabel;

    public ViewerStatusPanel() {
        super();
        FontIcon dimensions = FontIcon.of(FluentUiRegularMZ.SLIDE_SIZE_24, 20);
        dimensionsLabel = new JLabel("", dimensions, JLabel.TRAILING);
        dimensionsLabel.setToolTipText("Width / Height");
        this.add(dimensionsLabel);
    }

    public void setDimensionsLabelString(BufferedImage sprite) {
        dimensionsLabel.setText(sprite.getWidth() + " x " + sprite.getHeight());
    }

}
