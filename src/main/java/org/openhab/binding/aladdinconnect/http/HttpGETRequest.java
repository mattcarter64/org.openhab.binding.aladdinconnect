package org.openhab.binding.aladdinconnect.http;

import java.net.URI;

import lombok.ToString;

@ToString(callSuper = true)
public class HttpGETRequest extends HttpRequest {

    public HttpGETRequest(URI uri, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {
        super("GET", uri, ipAddress, userName, password, isSSL, socketTimeoutMillis);
    }

    public HttpGETRequest(String url, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {
        super("GET", url, ipAddress, userName, password, isSSL, socketTimeoutMillis);
    }

    public HttpGETRequest(String url, long socketTimeoutMillis) {
        super("GET", url, null, null, null, false, socketTimeoutMillis);
    }
}
