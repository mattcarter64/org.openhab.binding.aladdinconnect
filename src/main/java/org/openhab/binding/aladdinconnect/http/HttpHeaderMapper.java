/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link HttpHeaderMapper} is
 *
 * @author matt - Initial contribution
 */
public final class HttpHeaderMapper {

    private static Map<String, String> normalizedHeader = new HashMap<String, String>();

    static {
        normalizedHeader.put("content-type", "Content-Type");
        normalizedHeader.put("connection", "Connection");
        normalizedHeader.put("expires", "Expires");

        normalizedHeader.put("x-framerate", "X-FrameRate");
        normalizedHeader.put("x-starttime", "X-StartTime");
        normalizedHeader.put("user-agent", "User-Agent");
        normalizedHeader.put("range", "Range");
        normalizedHeader.put("icy-metadata", "Icy-MetaData");
    }

    public static String getNormalizedHeader(String key) {

        if (key != null) {

            String ret = normalizedHeader.get(key.toLowerCase());
            if (ret != null) {
                return ret;

            }
        }
        return key;
    }
}
