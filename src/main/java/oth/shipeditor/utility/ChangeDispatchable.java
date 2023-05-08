package oth.shipeditor.utility;

import java.beans.PropertyChangeSupport;

/**
 * @author Ontheheavens
 * @since 08.05.2023
 * Indicates that class has {@link PropertyChangeSupport} instance and supports property listeners.
 */
public interface ChangeDispatchable {

    /**
     * Retrieves the {@link PropertyChangeSupport} instance associated with this object.
     *
     * @return The {@link PropertyChangeSupport} instance.
     */
    PropertyChangeSupport getPCS();
}
