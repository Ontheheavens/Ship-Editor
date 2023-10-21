package oth.shipeditor.components.instrument.ship.variant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 21.10.2023
 */
@Getter
@Setter
@RequiredArgsConstructor
class DataSpinnerContainer<T extends Number> {

    private final SpinnerNumberModel model;

    private final JSpinner spinner;

    private Consumer<T> setter;

    Consumer<T> getCurrentSetter() {
        return setter;
    }

    void disableSpinner() {
        setter = null;
        model.setMaximum(0);
        model.setValue(0);
        spinner.setEnabled(false);
    }

    void enableSpinner(ShipLayer shipLayer, T newCurrent, T newMaximum, Consumer<T> newSetter) {
        Comparable<?> comparable = (Comparable<?>) newMaximum;
        setter = null;
        model.setMaximum(comparable);
        model.setValue(newCurrent);
        spinner.setEnabled(true);
        setter = newSetter;
    }

    @SuppressWarnings("unchecked")
    T getMaxValue() {
        Comparable<?> maxComparable = model.getMaximum();
        return (T) maxComparable;
    }

    @SuppressWarnings("unchecked")
    T getMinValue() {
        Comparable<?> minComparable = model.getMinimum();
        return (T) minComparable;
    }

}
