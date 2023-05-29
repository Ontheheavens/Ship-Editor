package oth.shipeditor.communication;

import oth.shipeditor.communication.events.BusEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 09.05.2023
 */
public class EventBus {

    private static final EventBus bus = new EventBus();

    private final HashMap<Class<? extends BusEvent>, List<BusEventListener<? extends BusEvent>>> subscribers;

    private EventBus() {
        this.subscribers = new HashMap<>();
    }

    public static <T extends BusEvent> void subscribe(Class<T> eventClass, BusEventListener<? extends T> listener) {
        List<BusEventListener<? extends BusEvent>> eventListeners = bus.subscribers.computeIfAbsent(eventClass,
                k -> new ArrayList<>());
        eventListeners.add(listener);
    }

    public static <T extends BusEvent> void unsubscribe(Class<T> eventClass, BusEventListener<? extends T> listener) {
        List<BusEventListener<? extends BusEvent>> eventListeners = bus.subscribers.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                bus.subscribers.remove(eventClass);
            }
        }
    }

    public static <T extends BusEvent> void publish(T event) {
        Class<? extends BusEvent> eventClass = event.getClass();
        List<BusEventListener<?>> eventListeners = bus.subscribers.get(eventClass);
        if (eventListeners != null) {
            for (BusEventListener<?> listener : eventListeners) {
                // We know it's the correct type because we just got the class from the event instance.
                @SuppressWarnings("unchecked") BusEventListener<T> typedListener = (BusEventListener<T>) listener;
                typedListener.handleEvent(event);
            }
        }

    }

}
