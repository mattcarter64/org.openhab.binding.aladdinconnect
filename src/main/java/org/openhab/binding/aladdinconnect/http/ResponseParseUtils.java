/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ResponseParseUtils} is
 *
 * @author matt - Initial contribution
 */
public final class ResponseParseUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResponseParseUtils.class);

    public static String[] parseMotionAreaNames(String s) {

        logger.debug("parseMotionAreaNames: areaNames={}", s);

        String[] names = new String[0];

        String[] st = s.split("\\|");
        if (st.length > 1) {

            String[] tokens = st[1].split(",");
            names = new String[tokens.length];
            for (int i = 0; i < tokens.length; i++) {

                names[i] = UrlEncoder.encode(tokens[i].trim());
            }
        }
        return names;
    }

    public static Map<String, String> parseQueryResponse(String response, String delimiter) {

        if (delimiter == null || delimiter.length() == 0) {

            delimiter = ":";
        }
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] lines = response.split("[\r\n\t]+");
        for (String line : lines) {

            String[] values = line.split(delimiter);
            if (values != null) {

                if (values.length == 2) {

                    paramMap.put(values[0].trim(), values[1].trim());
                } else if (values.length > 2) {

                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < values.length; i++) {

                        sb.append(values[i]);
                        if (i < values.length - 1) {
                            sb.append(delimiter);
                        }
                    }
                    paramMap.put(values[0].trim(), sb.toString());
                }
            }
        }
        return paramMap;
    }

    public static Map<String, String> parseURLParamsHeaders(String request) throws UnsupportedEncodingException {

        Map<String, List<String>> urlParams = HttpParseUtils.getUrlParameters(request);
        Map<String, String> headers = HttpParseUtils.getHeaders(request);

        for (String key : urlParams.keySet()) {

            List<String> listVal = urlParams.get(key);
            if (listVal != null && !listVal.isEmpty()) {

                headers.put(key, listVal.get(0));
            }
        }
        return headers;
    }
}
