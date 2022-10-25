package org.openhab.binding.aladdinconnect.handler;

import org.openhab.binding.aladdinconnect.http.AladdinConnectClient;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.model.Door;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDoorStatusTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(UpdateDoorStatusTask.class);
    private final AladdinBridgeHandler bridgeHandler;
    private final AladdinConnectClient aladdinConnectClient;

    public UpdateDoorStatusTask(AladdinBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        aladdinConnectClient = AladdinConnectClient.instance(bridgeHandler);
    }

    @Override
    public void run() {

        try {
            for (Door door : aladdinConnectClient.getDoors()) {
                GarageDoorHandler handler = bridgeHandler.getDoorHandlers().get(door.getOhId());

                if (handler != null) {
                    logger.debug("updating door status for id={}", handler.getId());

                    handler.updateState(AladdinConnectBindingConstants.CHANNEL_DOOR_STATUS,
                            new DecimalType(door.getStatus()));
                }
            }
        } catch (Exception e) {
            logger.warn("Error updating door status(es)... error={}", e.getMessage());
        }
    }
}
