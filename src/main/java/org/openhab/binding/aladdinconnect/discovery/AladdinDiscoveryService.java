package org.openhab.binding.aladdinconnect.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.aladdinconnect.handler.AladdinBridgeHandler;
import org.openhab.binding.aladdinconnect.http.AladdinConnectClient;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.model.Door;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.aladdin")
public class AladdinDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(AladdinDiscoveryService.class);

    private AladdinBridgeHandler bridgeHandler;
    private AladdinConnectClient aladdinConnectClient;

    public AladdinDiscoveryService() {
        super(AladdinConnectBindingConstants.DISCOVERABLE_DEVICE_TYPES_UIDS, 0);
    }

    public AladdinDiscoveryService(AladdinBridgeHandler bridgeHandler) {
        super(AladdinConnectBindingConstants.DISCOVERABLE_DEVICE_TYPES_UIDS, 0);

        this.bridgeHandler = bridgeHandler;
        aladdinConnectClient = AladdinConnectClient.instance(bridgeHandler);
    }

    public void startDiscovery(@NonNull ChannelUID channelUID) {

        logger.info("startDiscovery:");

        doDiscovery(channelUID);
    }

    public void stopDiscovery() {

        logger.info("stopDiscovery:");
    }

    @Override
    protected void startScan() {

        logger.info("startScan:");
    }

    @Override
    protected synchronized void stopScan() {

        logger.info("stopScan:");

        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {

        logger.info("startBackgroundDiscovery:");

        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {

        logger.info("stopBackgroundDiscovery:");

        super.stopBackgroundDiscovery();
    }

    private void doDiscovery(ChannelUID channelUID) {

        logger.debug("doDiscovery:");

        try {
            aladdinConnectClient.login();

            List<Door> doors = aladdinConnectClient.getDoors();

            for (Door door : doors) {
                addDoor(door);
            }
        } catch (Exception e) {
            logger.error("Discovery error. error=", e);
        }

        bridgeHandler.updateState(channelUID, OnOffType.OFF);
    }

    public void addDoor(Door door) {

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        Map<String, Object> properties = new HashMap<>();

        properties.put(AladdinConnectBindingConstants.PROP_OH_ID, door.getOhId());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_ID, door.getDevice_id());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_NAME, door.getName());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_NUMBER, door.getDoor_index());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_ENABLED, door.is_enabled());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_DATE_CREATED, door.getCreated_at());

        ThingUID uid = new ThingUID(AladdinConnectBindingConstants.THING_TYPE_GARAGE_DOOR, bridgeUID, door.getOhId());

        logger.debug("addDoor: bridgeUID={}, type={}, uid={}", bridgeUID.getId(),
                AladdinConnectBindingConstants.GARAGE_DOOR, uid);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(door.getName())
                .withProperties(properties).withRepresentationProperty(AladdinConnectBindingConstants.PROP_OH_ID)
                .build();

        logger.debug("addDoor: result={}", result);

        thingDiscovered(result);
    }
}
