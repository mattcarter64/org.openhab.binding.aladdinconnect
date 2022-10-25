/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.client.ProxyConfiguration.Proxy;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.aladdinconnect.model.AuthenticationException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JettyUtil} is
 *
 * @author matt - Initial contribution
 */
public class JettyUtil {

    private static Logger LOG = LoggerFactory.getLogger(JettyUtil.class);

    private static final HttpClient CLIENT = new HttpClient(new SslContextFactory.Client());

    private static final long DEFAULT_HTTP_TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    // private static final int CONNECT_TIMEOUT = 60;
    // private static final int RESPONSE_TIMEOUT = 60;

    private static class ProxyParams {
        public String proxyHost = null;
        public int proxyPort = 80;
        public String proxyUser = null;
        public String proxyPassword = null;
        public String nonProxyHosts = null;
    }

    public static HttpResponse executeHttpRequest(HttpRequest request) throws IOException, AuthenticationException {

        LOG.debug("request={}", request);

        String url = request.getUrl();

        if (url == null) {
            url = request.isSSL() ? "https://" : "http://";

            // if (request.getUserName() != null) {
            // url += (request.getUserName() != null ? request.getUserName() : "") + ":";
            // url += (request.getPassword() != null ? request.getPassword() : "") + "@";
            // }

            url += request.getIpAddress() + request.getUri().toString();
            // url += request.getIpAddress() + request.getUri().getPath()
            // + (request.getUri().getQuery() != null ? "?" + request.getUri().getQuery() : "");

            // LOG.debug("path=[{}], query=[{}]", request.getUri().getPath(), request.getUri().getQuery());
        }

        final ProxyParams proxyParams = prepareProxyParams();

        HttpResponse httpResponse = new HttpResponse();

        ContentResponse response = executeUrlAndGetResponse(request, url, proxyParams);

        String encoding = response.getEncoding() != null ? response.getEncoding().replaceAll("\"", "").trim() : "UTF-8";

        if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            throw new AuthenticationException("Not authorized");
        }
        try {
            httpResponse.setStatusCode(response.getStatus());
            httpResponse.setStatusText(response.getReason());
            httpResponse.setResponseBytes(response.getContent());
            httpResponse.setResponseStr(new String(response.getContent(), encoding));
        } catch (UnsupportedEncodingException e) {
            httpResponse.setStatusCode(500);
        }

        return httpResponse;
    }

    private static ContentResponse executeUrlAndGetResponse(HttpRequest httpRequest, String url,
            ProxyParams proxyParams) throws IOException {

        startHttpClient(CLIENT);

        // TODO
        CLIENT.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);

        HttpProxy proxy = null;

        // only configure a proxy if a host is provided
        if (proxyParams != null && StringUtils.isNotBlank(proxyParams.proxyHost)
                && shouldUseProxy(url, proxyParams.nonProxyHosts)) {
            AuthenticationStore authStore = CLIENT.getAuthenticationStore();
            ProxyConfiguration proxyConfig = CLIENT.getProxyConfiguration();
            List<Proxy> proxies = proxyConfig.getProxies();

            proxy = new HttpProxy(proxyParams.proxyHost, proxyParams.proxyPort);
            proxies.add(proxy);

            authStore.addAuthentication(new BasicAuthentication(proxy.getURI(), Authentication.ANY_REALM,
                    proxyParams.proxyUser, proxyParams.proxyPassword));
        }

        HttpMethod method = HttpUtil.createHttpMethod(httpRequest.getMethod());

        Request request = null;
        try {
            request = CLIENT.newRequest(new URI(url)).method(method)
                    .timeout(httpRequest.getSocketTimeoutMillis() == -1 ? DEFAULT_HTTP_TIMEOUT
                            : httpRequest.getSocketTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        if (httpRequest.getHeaders() != null) {
            for (String httpHeaderKey : httpRequest.getHeaders().stringPropertyNames()) {
                request.header(httpHeaderKey, httpRequest.getHeaders().getProperty(httpHeaderKey));
            }
        }

        if (httpRequest.getUserName() != null) {
            String userInfo = httpRequest.getUserName() + ":"
                    + (httpRequest.getPassword() != null ? httpRequest.getPassword() : "");

            // String basicAuthentication = "Basic " + Base64.getEncoder().encode(userInfo.getBytes());
            String basicAuthentication = "Basic " + new String(Base64.getEncoder().encode(userInfo.getBytes()));

            request.header(HttpHeader.AUTHORIZATION, basicAuthentication);
        }

        // add content if a valid method is given ...
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            if (httpRequest.getContent() != null) {
                request.content(new InputStreamContentProvider(httpRequest.getContent()), httpRequest.getContentType());
            } else if (httpRequest.getContentProvider() != null) {
                request.content(httpRequest.getContentProvider());
            }
        }

        LOG.debug("About to execute: HTTP request [{}], url [{}]", httpRequest, url);

        try {
            ContentResponse response = request.send();

            String statusLine = response.getStatus() + " " + response.getReason();

            LOG.debug("{} method returned: {}", method, statusLine);

            return response;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (proxy != null) {
                // Remove the proxy, that has been added for this request
                CLIENT.getProxyConfiguration().getProxies().remove(proxy);
            }
        }
    }

    private static void startHttpClient(HttpClient client) {

        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                LOG.warn("Cannot start HttpClient!", e);
            }
        }
    }

    private static ProxyParams prepareProxyParams() {

        final ProxyParams proxyParams = new ProxyParams();

        String proxySet = System.getProperty("http.proxySet");

        if ("true".equalsIgnoreCase(proxySet)) {
            proxyParams.proxyHost = System.getProperty("http.proxyHost");
            String proxyPortString = System.getProperty("http.proxyPort");

            if (proxyPortString != null) {
                try {
                    proxyParams.proxyPort = Integer.parseInt(proxyPortString);
                } catch (NumberFormatException e) {
                    LOG.warn("'{}' is not a valid proxy port - using default port ({}) instead", proxyPortString,
                            proxyParams.proxyPort);
                }
            }
            proxyParams.proxyUser = System.getProperty("http.proxyUser");
            proxyParams.proxyPassword = System.getProperty("http.proxyPassword");
            proxyParams.nonProxyHosts = System.getProperty("http.nonProxyHosts");
        }

        return proxyParams;
    }

    private static boolean shouldUseProxy(String urlString, String nonProxyHosts) {

        if (StringUtils.isNotBlank(nonProxyHosts)) {
            String givenHost = urlString;

            try {
                URL url = new URL(urlString);
                givenHost = url.getHost();
            } catch (MalformedURLException e) {
                LOG.error("the given url {} is malformed", urlString);
            }

            String[] hosts = nonProxyHosts.split("\\|");
            for (String host : hosts) {
                if (host.contains("*")) {
                    // the nonProxyHots-pattern allows wildcards '*' which must
                    // be masked to be used with regular expressions
                    String hostRegexp = host.replaceAll("\\.", "\\\\.");
                    hostRegexp = hostRegexp.replaceAll("\\*", ".*");
                    if (givenHost.matches(hostRegexp)) {
                        return false;
                    }
                } else {
                    if (givenHost.equals(host)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
