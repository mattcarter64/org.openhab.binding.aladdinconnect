/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

import java.util.Collections;
import java.util.Map;

/**
 * The {@link HttpRequestParameter} is
 *
 * @author matt - Initial contribution
 */
public class HttpRequestParameter {

    public String url;
    public String ipAddress;
    public String userName;
    public String password;
    public boolean isSSL;
    public int socketTimeoutMillis;
    public Map<String, String> queryParam;
    public Map<String, String> requestHeader;

    /**
     * @param url
     *            request url
     * @param ipAddress
     *            Host IPAddress
     * @param userName
     *            User Name
     * @param password
     *            Password
     * @param isSSL
     *            isSSL
     * @param socketTimeoutMillis
     *            readTimeout
     */
    public HttpRequestParameter(String url, String ipAddress, String userName, String password, boolean isSSL,
            int socketTimeoutMillis) {

        this.url = url;
        this.ipAddress = ipAddress;
        this.userName = userName;
        this.password = password;
        this.isSSL = isSSL;
        this.socketTimeoutMillis = socketTimeoutMillis;
        this.queryParam = Collections.emptyMap();
        this.requestHeader = Collections.emptyMap();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
        result = prime * result + (isSSL ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((queryParam == null) ? 0 : queryParam.hashCode());
        result = prime * result + ((requestHeader == null) ? 0 : requestHeader.hashCode());
        result = prime * result + socketTimeoutMillis;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HttpRequestParameter)) {
            return false;
        }
        HttpRequestParameter other = (HttpRequestParameter) obj;
        if (ipAddress == null) {
            if (other.ipAddress != null) {
                return false;
            }
        } else if (!ipAddress.equals(other.ipAddress)) {
            return false;
        }
        if (isSSL != other.isSSL) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (queryParam == null) {
            if (other.queryParam != null) {
                return false;
            }
        } else if (!queryParam.equals(other.queryParam)) {
            return false;
        }
        if (requestHeader == null) {
            if (other.requestHeader != null) {
                return false;
            }
        } else if (!requestHeader.equals(other.requestHeader)) {
            return false;
        }
        if (socketTimeoutMillis != other.socketTimeoutMillis) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpRequestParameter [url=");
        builder.append(url);
        builder.append(", ipAddress=");
        builder.append(ipAddress);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", password=");
        builder.append(password);
        builder.append(", isSSL=");
        builder.append(isSSL);
        builder.append(", socketTimeoutMillis=");
        builder.append(socketTimeoutMillis);
        builder.append(", queryParam=");
        builder.append(queryParam);
        builder.append(", requestHeader=");
        builder.append(requestHeader);
        builder.append("]");
        return builder.toString();
    }
}
