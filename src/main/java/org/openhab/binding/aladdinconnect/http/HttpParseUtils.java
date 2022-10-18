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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpParseUtils} is
 *
 * @author matt - Initial contribution
 */
public final class HttpParseUtils {

    private final static Logger logger = LoggerFactory.getLogger(HttpParseUtils.class);

    private HttpParseUtils() {
    }

    private static final Pattern VID_PROX_Q_PATTERN = Pattern.compile("(videoProxy/.+?/Q/)");
    private static Pattern RTSP_VIDEOPROXY_PATTERN = Pattern.compile("rtsp[s]?://(.*)[:\\d]?.*/Q");
    private static final Pattern BAD_AUTH = Pattern.compile("(\r\nAuthorization.*)");
    private static final String HEADER_END = "\r\n\r\n";

    private static final Pattern HEADER_END_PATTERN = Pattern.compile("(" + HEADER_END + ")");

    private static final Pattern PATTERN_CLOSE = Pattern.compile("Connection: +?[Cc]lose");

    private static final Pattern IIWC_REQUEST_PATTERN = Pattern.compile("200 OK[\r\n]+(.*)",
            Pattern.DOTALL | Pattern.MULTILINE);

    private static Pattern rtspTunnelVproxyPattern = Pattern.compile("(.*)rtsp[s]?://(.*[:\\d]?).*/Q(.*)",
            Pattern.DOTALL | Pattern.MULTILINE);
    private static Pattern iiwcrtspVideoProxyPattern = Pattern.compile("(.*) (.* RTSP)(.*)",
            Pattern.DOTALL | Pattern.MULTILINE);

    public static final String BASE64_PATTERN = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";

    // private static final Pattern VIDEO_REQUEST_PATTERN =
    // Pattern.compile("videoProxy/.+?/Q(/.*) ");

    public static String mangleHttpHeader(String deviceIp, String basicAuth, String request) {

        String noQ = "";
        if (request.contains("rtsp://")) {

            noQ = RTSP_VIDEOPROXY_PATTERN.matcher(request).replaceAll("rtsp://" + deviceIp);
        } else {

            noQ = VID_PROX_Q_PATTERN.matcher(request).replaceAll("");
        }
        String noQnoH = BAD_AUTH.matcher(noQ).replaceAll("");// delete client
                                                             // auth
        // look for header end and add auth
        return HEADER_END_PATTERN.matcher(noQnoH).replaceAll("\r\n" + basicAuth + HEADER_END);
    }

    public static boolean containsHeaderEnd(CharSequence request) {

        Matcher matcher = HEADER_END_PATTERN.matcher(request);

        return matcher.find();
    }

    public static String getRequestURL(String request) {

        Matcher m = VID_PROX_Q_PATTERN.matcher(request);
        String body = "";

        if (m.find()) {
            body = m.group();

            return body;
        } else {
            return "";// no string matching pattern
        }
    }

    public static boolean isPatternClose(String request) {
        if (PATTERN_CLOSE.matcher(request).find()) {
            // non-http 1.1

            return true;

        } else {
            return false;
        }
    }

    public static String buildClientResponseHeader(Map<String, List<String>> map) {
        StringBuilder header = new StringBuilder(300);

        // Get the HTTP Header
        header.append(getHeaderListValue(map.get(null))).append("\r\n");
        header.append("Connection: Keep-Alive\r\n");

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            if (entry.getKey() != null) {

                logger.debug("Key={}, Value={}", entry.getKey(), getHeaderListValue(entry.getValue()));

                header.append(entry.getKey()).append(": ").append(getHeaderListValue(entry.getValue())).append("\r\n");
            }
        }
        header.append("\r\n");
        return header.toString();
    }

    private static String getHeaderListValue(List<String> valueList) {

        StringBuilder sb = new StringBuilder();
        int size = valueList.size();
        int count = 0;
        while (true) {
            sb.append(valueList.get(count));
            count++;
            if (count < size) {
                sb.append(",");
            } else {
                break;
            }
        }

        return sb.toString();
    }

    /**
     * A simple utility method to get the URL Path.
     *
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getUrlPath(String request) throws UnsupportedEncodingException {

        if (request.startsWith("GET") || request.startsWith("POST")) {
            String[] requestArray = request.split("\\p{Space}");
            if (requestArray != null && requestArray.length > 1) {
                String[] urlParts = requestArray[1].split("\\?");
                if (urlParts.length > 1) {

                    return URLDecoder.decode(urlParts[0], "UTF-8");
                } else {

                    return requestArray[1];
                }
            }
        } else if (request.contains("?")) {
            String[] urlParts = request.split("\\?");
            if (urlParts.length > 1) {

                return URLDecoder.decode(urlParts[0], "UTF-8");
            }
        }
        return null;
    }

    public static Map<String, List<String>> getUrlParameters(String request) throws UnsupportedEncodingException {

        Map<String, List<String>> params = new HashMap<String, List<String>>();

        if (request.startsWith("GET") || request.startsWith("POST")) {
            String[] requestArray = request.split("\\p{Space}");
            if (requestArray != null && requestArray.length > 1) {

                populateQueryParms(requestArray[1], params);
            }
        } else if (request.contains("?") || request.contains("&")) {

            populateQueryParms(request, params);
        }
        return params;
    }

    public static void populateQueryParms(String request, Map<String, List<String>> params)
            throws UnsupportedEncodingException {

        String query = request;

        if (request.contains("?")) {
            String[] urlParts = request.split("\\?");

            if (urlParts.length > 1) {
                query = urlParts[1];
            }
        }

        for (String param : query.split("&")) {

            String pair[] = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = "";
            if (pair.length > 1) {

                value = URLDecoder.decode(pair[1], "UTF-8");
            }
            List<String> values = params.get(key);
            if (values == null) {

                values = new ArrayList<String>();
                params.put(key, values);
            }
            values.add(value);
        }
    }

    public static Map<String, String> getHeaders(String request) {
        Map<String, String> headerMap = new HashMap<String, String>();

        String[] inputArray = request.split("\r\n");

        for (int i = 1; i < inputArray.length; i++) {

            String input = inputArray[i];
            if (input != null && input.trim().length() > 1) {

                String[] headers = input.split(":");

                StringBuilder value = new StringBuilder();
                if (headers.length > 2) {
                    for (int j = 1; j < headers.length; j++) {

                        value.append(headers[j].trim());
                        if (j < headers.length - 1) {
                            value.append(":");
                        }

                    }
                } else {
                    value.append(headers[1].trim());
                }

                headerMap.put(headers[0].trim(), value.toString());

            }
        }

        return headerMap;
    }

    public static String parseHttpRequest(CharSequence request) {

        Matcher m = IIWC_REQUEST_PATTERN.matcher(request);
        String body = "";

        if (m.find()) {
            body = m.group(1);

        }
        return body;
    }

    public static String replaceRtspProxyUrls(String deviceIp, String basicAuth, String request) {

        // LOGGER.debug(request);
        Matcher match = rtspTunnelVproxyPattern.matcher(request);

        if (match.matches()) {
            request = match.group(1);
            request += "rtsp://" + deviceIp;
            request += match.group(3);
        }

        if (request.contains("DESCRIBE")) {
            match = iiwcrtspVideoProxyPattern.matcher(request);
            if (match.matches()) {
                request = match.group(1);
                String url = match.group(2);
                String[] urls = url.split("/");
                request += " rtsp://" + deviceIp + "/";
                for (int i = 3; i < urls.length; i++) {

                    request += urls[i];
                    if (i < urls.length - 1) {
                        request += "/";
                    }

                }
                request += match.group(3);
            }

        }

        return HEADER_END_PATTERN.matcher(request).replaceAll("\r\n" + basicAuth + HEADER_END);
    }

    public static String rtspPostMessageHandler(String deviceIp, String basicAuth, String filtered) {

        if (filtered != null && !filtered.contains("Authorization")) {
            // This is POST base64encoded string
            try {

                if (filtered.matches(BASE64_PATTERN)) {

                    String decoded = new String(Base64.getDecoder().decode(filtered.getBytes()));

                    String encodedString = replaceRtspProxyUrls(deviceIp, basicAuth, decoded);
                    // if (VideoStreamUtils.DEBUG_PROXY) {
                    //
                    // LOGGER.info("Decoded " + decoded);
                    // LOGGER.info("Sanitized " + encodedString);
                    // }

                    filtered = Base64.getEncoder().encodeToString(encodedString.getBytes());

                    // if (VideoStreamUtils.DEBUG_PROXY) {
                    // LOGGER.info("POST " + filtered);
                    // }

                }
            } catch (Exception e) {
                logger.debug("Exception thrown doing base64 encode/decode. filtered=[{}]", filtered, e);
            }
        }
        return filtered;
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
}
