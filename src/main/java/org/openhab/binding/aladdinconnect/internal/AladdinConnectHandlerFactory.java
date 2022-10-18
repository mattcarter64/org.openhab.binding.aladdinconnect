/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.aladdinconnect.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.aladdinconnect.discovery.AladdinDiscoveryService;
import org.openhab.binding.aladdinconnect.handler.AladdinBridgeHandler;
import org.openhab.binding.aladdinconnect.handler.GarageDoorHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AladdinConnectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matt Carter - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.aladdinconnect", service = ThingHandlerFactory.class)
public class AladdinConnectHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AladdinConnectHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return AladdinConnectBindingConstants.DISCOVERABLE_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        logger.info("createHandler: thing={}", thing.toString());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (AladdinConnectBindingConstants.THING_TYPE_ALADDIN_BRIDGE.equals(thingTypeUID)) {
            AladdinBridgeHandler handler = new AladdinBridgeHandler((Bridge) thing);

            return handler.setDiscoveryService(registerDeviceDiscoveryService(handler));
        } else if (AladdinConnectBindingConstants.THING_TYPE_GARAGE_DOOR.equals(thingTypeUID)) {
            return new GarageDoorHandler(thing);
        }

        return null;
    }

    private AladdinDiscoveryService registerDeviceDiscoveryService(AladdinBridgeHandler handler) {

        logger.debug("registerDeviceDiscoveryService: UID={}", handler.getThing().getUID());

        AladdinDiscoveryService discoveryService = new AladdinDiscoveryService(handler);

        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

        return discoveryService;
    }
}
