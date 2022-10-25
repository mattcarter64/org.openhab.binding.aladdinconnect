package org.openhab.binding.aladdinconnect.http;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.client.api.ContentProvider;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class HttpRequest {

    private String method;
    private URI uri = null;
    private String url;
    private String ipAddress;
    private String userName;
    private String password;
    private boolean isSSL;
    private long socketTimeoutMillis = -1;
    private Map<String, String> queryParam;
    private Properties headers;
    private InputStream content;
    private String contentType;
    private ContentProvider.Typed contentProvider;

    public HttpRequest(String method, URI uri, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {
        this(method, (String) null, ipAddress, userName, password, isSSL, socketTimeoutMillis);
        this.uri = uri;
    }

    public HttpRequest(String method, String url, String ipAddress, String userName, String password, boolean isSSL,
            long socketTimeoutMillis) {

        this.method = method;
        this.url = url;
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.password = (password != null && "".equals(password)) ? null : password;
        this.isSSL = isSSL;
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.queryParam = Collections.emptyMap();
    }
}
