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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AladdinConnectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matt Carter - Initial contribution
 */
@NonNullByDefault
public class AladdinConnectBindingConstants {

    public static final String BINDING_ID = "aladdinconnect";

    public static final String ALADDIN_BRIDGE_ID = "aladdinconnectbridge";
    public static final String GARAGE_DOOR = "garagedoor";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ALADDIN_BRIDGE = new ThingTypeUID(BINDING_ID, ALADDIN_BRIDGE_ID);

    public static final ThingTypeUID THING_TYPE_GARAGE_DOOR = new ThingTypeUID(BINDING_ID, GARAGE_DOOR);

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = Stream
            .of(THING_TYPE_ALADDIN_BRIDGE, THING_TYPE_GARAGE_DOOR).collect(Collectors.toSet());

    // List of all Channel ids
    public static final String CHANNEL_DISCOVERY_ID = "discovery";

    public static final String CHANNEL_DOOR_POSITION = "door-position";
    public static final String CHANNEL_DOOR_STATUS = "door-status";

    // property IDs
    public final static String PROP_OH_ID = "id";
    public final static String PROP_DOOR_ID = "door_id";
    public final static String PROP_DOOR_NUMBER = "door_number";
    public final static String PROP_DOOR_NAME = "name";
    public final static String PROP_DOOR_ENABLED = "enabled";
    public final static String PROP_DOOR_DATE_CREATED = "datecreated";

    // public final static String PROP_MODEL = "model";
    // public final static String PROP_VERSION = "version";

    // API foo

    public final static String LOGIN_ENDPOINT = "/oauth/token";
    public final static String LOGOUT_ENDPOINT = "/session/logout";
    public final static String CONFIGURATION_ENDPOINT = "/configuration";
    public final static String DEVICE_ENDPOINT = "/devices";

    public final static String AUTH_APP_VERSION = "5.30";
    public final static String AUTH_BUNDLE_NAME = "com.geniecompany.AladdinConnect";
    public final static String AUTH_BUILD_VERSION = "131";

    //
    public final static String DOOR_STATUS_OPEN = "open";
    public final static String DOOR_STATUS_CLOSED = "closed";
    public final static String DOOR_STATUS_OPENING = "opening";
    public final static String DOOR_STATUS_CLOSING = "closing";
    public final static String DOOR_STATUS_UNKNOWN = "unknown";
    public final static String DOOR_STATUS_TIMEOUT_CLOSE = "open"; // If it timedout opening, it's still closed?
    public final static String DOOR_STATUS_TIMEOUT_OPEN = "closed"; // If it timedout closing, it's still open?
    public final static String DOOR_COMMAND_CLOSE = "CloseDoor";
    public final static String DOOR_COMMAND_OPEN = "OpenDoor";

    public final static String STATUS_CONNECTED = "Connected";
    public final static String STATUS_NOT_CONFIGURED = "NotConfigured";
    public static final String THREAD_POOL_NAME = "aladdinthreads";
    public static final String SCHED_THREAD_POOL_NAME = "aladdinschedthreads";
}
