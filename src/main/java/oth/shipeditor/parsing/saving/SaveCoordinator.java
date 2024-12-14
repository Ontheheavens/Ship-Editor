package oth.shipeditor.parsing.saving;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.saving.HullSaveQueued;
import oth.shipeditor.communication.events.files.saving.VariantSaveQueued;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public final class SaveCoordinator {

    private SaveCoordinator() {
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof VariantSaveQueued checked) {
                SaveVariantAction.saveVariant(checked.variant());
            } else if (event instanceof HullSaveQueued checked) {
                SaveHullAction.saveHullFromLayer(checked.shipLayer());
            }
        });
    }

}
