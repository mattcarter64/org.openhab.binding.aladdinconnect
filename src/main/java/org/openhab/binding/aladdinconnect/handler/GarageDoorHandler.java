/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.aladdinconnect.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.aladdinconnect.http.AladdinConnectClient;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.internal.config.GarageDoorConfig;
import org.openhab.binding.aladdinconnect.model.AuthenticationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GarageDoorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matt Carter - Initial contribution
 */
@NonNullByDefault
public class GarageDoorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GarageDoorHandler.class);

    private @Nullable GarageDoorConfig config;
    private @Nullable AladdinBridgeHandler bridgeHandler;
    private final AladdinConnectClient aladdinConnectClient;

    public GarageDoorHandler(Thing thing) {
        super(thing);

        aladdinConnectClient = AladdinConnectClient.instance(bridgeHandler);
    }

    public String getId() {
        String s = thing.getProperties().get(AladdinConnectBindingConstants.PROP_OH_ID);
        return s != null ? s : "";
    }

    @Override
    public void initialize() {

        config = getConfigAs(GarageDoorConfig.class);

        bridgeHandler = (AladdinBridgeHandler) getBridge().getHandler();

        logger.debug("initialize: config=[{}], bridge=[{}]", config, bridgeHandler);

        bridgeHandler.registerHandler(this);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly, i.e. any network access must be done in
        // the background initialization below.
        // Also, before leaving this method a thing status from one of ONLINE, OFFLINE or UNKNOWN must be set. This
        // might already be the real thing status in case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");
        //
        // Logging to INFO should be avoided normally.
        // See https://www.openhab.org/docs/developer/guidelines.html#f-logging

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        logger.debug("handleCommand: channel id={}", channelUID.getAsString());

        if (AladdinConnectBindingConstants.CHANNEL_DOOR_POSITION.equalsIgnoreCase(channelUID.getId())) {
            if (command instanceof DecimalType) {
                setDoorPosition((DecimalType) command);
            }
        }
    }

    private void setDoorPosition(DecimalType command) {

        String deviceId = this.getThing().getProperties().get(AladdinConnectBindingConstants.PROP_DOOR_ID);
        String number = this.getThing().getProperties().get(AladdinConnectBindingConstants.PROP_DOOR_NUMBER);

        try {
            aladdinConnectClient.setDoorState(deviceId, number, command);
        } catch (AuthenticationException e) {
            try {
                logger.info("Authentication failed. Attempting to login and re-try...");

                // try to login once, and then set state again
                aladdinConnectClient.login();
                aladdinConnectClient.setDoorState(deviceId, number, command);
            } catch (Exception ex) {
                logger.error("Unable to set door state. error={}", ex.getMessage());
            }
        } catch (Exception ee) {
            logger.error("Unable to set door state. error={}", ee.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        logger.info("dispose:");
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        logger.info("bridgeStatusChanged: status={}", bridgeStatusInfo);
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }
}
