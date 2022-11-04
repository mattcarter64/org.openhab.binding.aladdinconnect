package org.openhab.binding.aladdinconnect.handler;

import java.util.List;

import org.openhab.binding.aladdinconnect.http.AladdinConnectClient;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.model.AuthenticationException;
import org.openhab.binding.aladdinconnect.model.Door;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
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

        if (bridgeHandler.getDoorHandlers().size() == 0) {
            logger.debug("no handlers registered. Status call skipped...");
            return;
        }

        logger.debug("getting door status(es)....");

        try {
            for (Door door : getDoors()) {
                GarageDoorHandler handler = bridgeHandler.getDoorHandlers().get(door.getOhId());

                if (handler != null) {
                    logger.debug("updating door status for id={} with status={}", handler.getId(), door.getStatus());

                    handler.updateState(AladdinConnectBindingConstants.CHANNEL_DOOR_STATUS,
                            new DecimalType(door.getStatus()));
                    handler.updateState(AladdinConnectBindingConstants.CHANNEL_DOOR_LAST_EVENT_TS,
                            new StringType(door.getLast_status_ts()));
                    handler.updateState(AladdinConnectBindingConstants.CHANNEL_DOOR_LAST_EVENT,
                            new StringType(door.getDoor_updated_at()));
                }
            }
        } catch (Exception e) {
            logger.warn("Error updating door status(es)... error={}", e.getMessage());
        }
    }

    private List<Door> getDoors() throws Exception {
        try {
            return aladdinConnectClient.getDoors();
        } catch (AuthenticationException e) {
            logger.warn("Authentication error. login and try one more time");

            aladdinConnectClient.login();

            return aladdinConnectClient.getDoors();
        }
    }
}
