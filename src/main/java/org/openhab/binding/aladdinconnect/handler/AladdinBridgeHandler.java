package org.openhab.binding.aladdinconnect.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.aladdinconnect.discovery.AladdinDiscoveryService;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.internal.config.AladdinBridgeConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AladdinBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AladdinBridgeHandler.class);

    private @Nullable AladdinBridgeConfig config;

    private AladdinDiscoveryService discoveryService;

    private AladdinEventHandler eventHandler = null;

    private final Map<String, GarageDoorHandler> doorHandlers = new HashMap<>();

    public AladdinBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public Map<String, GarageDoorHandler> getDoorHandlers() {
        return doorHandlers;
    }

    @Override
    public void initialize() {

        config = getConfigAs(AladdinBridgeConfig.class);

        logger.info("initialize: config={}", config);

        Channel channel = getThing().getChannel(AladdinConnectBindingConstants.CHANNEL_DISCOVERY_ID);

        if (channel != null) {
            updateState(channel.getUID(), OnOffType.OFF);
        }

        scheduler.execute(() -> {
            startEventHandler();
        });

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> configurationParameters) {

        logger.info("handleConfigurationUpdate: Configuration update received. thing={}, configurationParameters={}",
                getThing().getThingTypeUID(), configurationParameters);

        // save current configuration
        AladdinBridgeConfig previousConfig = config;

        // update framework configuration
        logger.info("handleConfigurationUpdate: updating parameters");

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        updateConfiguration(configuration);

        // check for changes
        config = getThing().getConfiguration().as(AladdinBridgeConfig.class);

        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            if ("authToken".equalsIgnoreCase(configurationParameter.getKey())) {
                startEventHandler();
                break;
            }
        }

        if (previousConfig == null) {
            return;
        }

        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            logger.debug("handleConfigurationUpdate: configurationParameter={}", configurationParameter);

            // TODO do the needful
            // if ("serverPort".equalsIgnoreCase(configurationParameter.getKey())) {
            // if (previousConfig.getServerPort() != config.getServerPort()) {
            // stopPingerTask();
            // startListener();
            // // startNormalPingerTask(1);
            // }
            // } else if ("pingInterval".equalsIgnoreCase(configurationParameter.getKey())) {
            // if (previousConfig.getPingInterval() != config.getPingInterval()) {
            // // startNormalPingerTask(1);
            // }
            // } else if ("wifiSid".equalsIgnoreCase(configurationParameter.getKey())) {
            // if (!previousConfig.getWifiSid().equals(config.getWifiSid())) {
            // updateAllCameraWifi();
            // }
            // } else if ("wifiPassword".equalsIgnoreCase(configurationParameter.getKey())) {
            // if (!previousConfig.getWifiPassword().equals(config.getWifiPassword())) {
            // updateAllCameraWifi();
            // }
            // }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("handleCommand: channel id={}", channelUID.getAsString());

        if (command instanceof RefreshType) {
            for (Channel channel : thing.getChannels()) {
                updateState(channel.getUID(), OnOffType.OFF);
            }

            return;
        }

        if (AladdinConnectBindingConstants.CHANNEL_DISCOVERY_ID.equalsIgnoreCase(channelUID.getId())) {
            OnOffType action = (OnOffType) command;

            if (action.equals(OnOffType.ON)) {
                stopEventHandler();

                discoveryService.startDiscovery(channelUID);
            } else if (action.equals(OnOffType.OFF)) {
                discoveryService.stopDiscovery();
            }
        }
    }

    public AladdinBridgeHandler setDiscoveryService(AladdinDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;

        return this;
    }

    @Override
    public void updateState(@NonNull ChannelUID channelUID, @NonNull State state) {

        logger.debug("updateState: channelUID={}, state={}", channelUID.getAsString(), state);

        super.updateState(channelUID, state);

        if (isInitialized()) {
            if (AladdinConnectBindingConstants.CHANNEL_DISCOVERY_ID.equalsIgnoreCase(channelUID.getId())) {
                if (state.equals(OnOffType.OFF)) {
                    startEventHandler();
                }
            }
        }
    }

    @Override
    public <T> T getConfigAs(@NonNull Class<T> configurationClass) {
        return super.getConfigAs(configurationClass);
    }

    public String getBearerHeader() {

        return "Bearer " + config.getAuthToken();
    }

    public String getAuthToken() {

        return config.getAuthToken();
    }

    public String getApiKey() {

        return config.getApiKey();
    }

    @Override
    public void dispose() {

        logger.info("dispose:");

        super.dispose();

        stopEventHandler();
    }

    private void startEventHandler() {

        logger.info("startEventHandler:");

        stopEventHandler();

        // TODO need to tighten this up a bit. Maybe retry after new auth token, etc.
        eventHandler = new AladdinEventHandler(this);

        try {
            eventHandler.start();
        } catch (Exception e) {
            logger.error("Exception starting event handler.", e);
        }
    }

    private void stopEventHandler() {

        logger.info("stopEventHandler:");

        if (eventHandler != null) {
            try {
                eventHandler.stop();

                eventHandler = null;
            } catch (Exception e) {
                logger.error("Exception stopping event handler.", e);

            }
        }
    }

    public void registerHandler(GarageDoorHandler handler) {

        doorHandlers.put(handler.getId(), handler);
    }
}
