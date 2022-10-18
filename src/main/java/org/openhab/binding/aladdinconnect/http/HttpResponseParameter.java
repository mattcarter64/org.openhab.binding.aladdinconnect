/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

/**
 * The {@link HttpResponseParameter} is
 *
 * @author matt - Initial contribution
 */
public class HttpResponseParameter {

    public int statusCode;
    public String statusText;
    public String responseStr;
    public byte[] responseBytes;

    /**
     *
     * Default Constructor
     */
    public HttpResponseParameter() {

        this.statusCode = 0;
        this.statusText = "";
        this.responseBytes = null;
        this.responseStr = "";
    }
}
