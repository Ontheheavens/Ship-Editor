package oth.shipeditor.components.instrument.ship.centers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.CenterPointPainter;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.widgets.PointLocationWidget;
import oth.shipeditor.utility.components.widgets.Spinners;
import oth.shipeditor.utility.components.widgets.TwinSpinnerPanel;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 08.11.2023
 */
public class ModuleAnchorPanel extends PointLocationWidget {

    @Getter(AccessLevel.PRIVATE) @Setter
    private CenterPointPainter centerPainter;

    @Override
    protected void populateContent() {
        super.populateContent();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 10, 0, 6);
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.LINE_START;

        JButton createDeleteButton = getCreateDeleteButton();

        String clearAnchor = "Clear anchor";
        registerWidgetListeners(createDeleteButton, layer -> {
            createDeleteButton.setText(StringValues.DEFINE_ANCHOR);
            createDeleteButton.setEnabled(false);
        }, layer -> {
            Supplier<Point2D> getter = retrieveGetter();
            Point2D existing = getter.get();
            if (existing != null) {
                createDeleteButton.setText(clearAnchor);
            }
            else {
                createDeleteButton.setText(StringValues.DEFINE_ANCHOR);
            }
            createDeleteButton.setEnabled(true);
        });

        constraints.gridy = 3;
        TwinSpinnerPanel twinSpinnerPanel = getTwinSpinnerPanel();
        twinSpinnerPanel.add(createDeleteButton, constraints);
    }

    private JButton getCreateDeleteButton() {
        JButton createDeleteButton = new JButton(StringValues.DEFINE_ANCHOR);
        createDeleteButton.addActionListener(e -> {
            if (isWidgetsReadyForInput()) {
                Supplier<Point2D> getter = retrieveGetter();
                Point2D existing = getter.get();
                Consumer<Point2D> setter = retrieveSetter();
                if (existing != null) {
                    setter.accept(null);
                }
                else {
                    setter.accept(new Point2D.Double());
                }
                EditDispatch.notifyTimedEditConcluded();
                processChange();
            }
        });
        return createDeleteButton;
    }

    /**
     * The coordinate name reversal completely intentional - blame Alex and module anchor field for that!
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected TwinSpinnerPanel createSpinnerPanel(Point2D initialPoint, Consumer<Point2D> pointSetter) {
        return Spinners.createLocationSpinners(initialPoint, retrieveGetter(), pointSetter,
                StringValues.Y_COORDINATE, StringValues.X_COORDINATE);
    }

    @Override
    protected boolean isLayerPainterEligible(LayerPainter layerPainter) {
        return layerPainter instanceof ShipPainter shipPainter && !shipPainter.isUninitialized();
    }

    @Override
    protected String getPanelTitleText() {
        return StringValues.MODULE_ANCHOR;
    }

    @Override
    protected Supplier<Point2D> retrieveGetter() {
        return () -> {
            CenterPointPainter painter = getCenterPainter();
            if (painter != null) {
                return painter.getModuleAnchorOffset();
            }
            return null;
        };
    }

    @Override
    protected Consumer<Point2D> retrieveSetter() {
        return point -> {
            CenterPointPainter painter = getCenterPainter();
            if (painter != null) {
                painter.setModuleAnchorOffset(point);
            }
        };
    }

}
