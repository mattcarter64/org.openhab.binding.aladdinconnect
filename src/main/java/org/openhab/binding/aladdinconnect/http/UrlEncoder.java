/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.http;

import org.openhab.binding.aladdinconnect.util.Hex;

/**
 * The {@link Hex} is a collection of utility methods.
 *
 * @author matt - Initial contribution
 */
public class UrlEncoder {

    public static String encode(String s) {
        StringBuffer sb = new StringBuffer(s.length() + 20);
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '/':
                    sb.append("%2F");
                    break;
                case ':':
                    sb.append("%3A");
                    break;
                case '?':
                    sb.append("%3F");
                    break;
                case '=':
                    sb.append("%3D");
                    break;
                case '&':
                    sb.append("%26");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
