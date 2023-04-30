package oth.shipeditor.components;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class PointsPainter implements Painter {

    @Getter
    private final List<Painter> delegates;

    /**
     * The world-to-screen transform that will be passed to the delegates
     */
    private final AffineTransform delegateWorldToScreen;

    /**
     * Create a new painter that is a composition of the given painters
     * @param delegates The delegate painters
     */
    public PointsPainter(Painter ... delegates)
    {
        this.delegates = new ArrayList<>(Arrays.asList(delegates));
        this.delegateWorldToScreen = new AffineTransform();
    }

    @Override
    public void paint(Graphics2D g,
                      AffineTransform worldToScreen, double w, double h)
    {
        for (Painter delegate : delegates)
        {
            if (delegate != null)
            {
                delegateWorldToScreen.setTransform(worldToScreen);
                delegate.paint(g, delegateWorldToScreen, w, h);
            }
        }
    }

}
