package org.openhab.binding.aladdinconnect.http;

import java.net.URI;

public class HttpPOSTRequest extends HttpRequest {

    public HttpPOSTRequest(URI uri, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {
        super("POST", uri, ipAddress, userName, password, isSSL, socketTimeoutMillis);
    }

    public HttpPOSTRequest(String url, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {
        super("POST", url, ipAddress, userName, password, isSSL, socketTimeoutMillis);
    }

    public HttpPOSTRequest(String url, long socketTimeoutMillis) {
        super("POST", url, null, null, null, false, socketTimeoutMillis);
    }

    @Override
    public String toString() {
        return "HttpPOSTRequest: " + super.toString();
    }
}
