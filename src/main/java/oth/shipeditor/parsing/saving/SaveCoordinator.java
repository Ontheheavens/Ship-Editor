package oth.shipeditor.parsing.saving;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.files.saving.VariantSaveQueued;

/**
 * @author Ontheheavens
 * @since 22.10.2023
 */
public final class SaveCoordinator {

    private SaveCoordinator() {
    }

    public static void init() {
        EventBus.subscribe(event -> {
            if (event instanceof VariantSaveQueued(var variant)) {
                SaveVariantAction.saveVariant(variant);
            }
        });
    }

}
