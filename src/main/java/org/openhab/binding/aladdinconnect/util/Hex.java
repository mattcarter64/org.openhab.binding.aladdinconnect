/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.aladdinconnect.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The {@link Hex} is a collection of utility methods. Most of the methods deal with hexadecimal
 * representation of byte and integer variables.
 *
 * @author matt - Initial contribution
 */
public class Hex {

    public static String hex(byte[] data) {
        if (data == null) {
            return null;
        }
        return hex(data, 0, data.length, true);
    }

    public static String hex(byte[] data, boolean withSpaces) {
        return hex(data, 0, data.length, withSpaces);
    }

    public static String hex(byte[] data, int off, int len) {
        return hex(data, off, len, true);
    }

    /**
     * Returns a string with hexadecimal representation of data of input array
     *
     * @param data
     *            input data
     * @param off
     *            offset
     * @param len
     *            how many bytes to process, offset + len has to be <= data.length or you'll get an array out of bounds.
     * @param withSpaces
     *            set to true to include spaces separating bytes (like in "4A B6")
     */
    public static String hex(byte[] data, int off, int len, boolean withSpaces) {
        if (data == null) {
            return null;
        }
        int n = data.length;
        StringBuffer s = new StringBuffer(3 * n);
        for (int i = off, end = off + len; i < end; i++) {
            s.append(hex(data[i]));
            if (i < n - 1 && withSpaces) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    public static String hex(byte data) {
        String s = "";
        byte n = (byte) ((byte) 0x0f & (data >> 4));
        if (n <= 9) {
            s += n;
        } else {
            s += (char) ('A' + (n - 10));
        }
        n = (byte) ((byte) 0x0f & data);
        if (n <= 9) {
            s += n;
        } else {
            s += (char) ('A' + (n - 10));
        }
        return s;
    }

    public static String hex(Byte[] data) {
        if (data == null) {
            return null;
        }
        int n = data.length;
        StringBuffer s = new StringBuffer(3 * n);
        for (int i = 0; i < n; i++) {
            s.append(hex(data[i]));
            if (i < n - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    public static String hex(Byte data) {
        if (data == null) {
            return null;
        }
        return hex(data.byteValue());
    }

    /**
     * same as hex(data, true)
     */
    public static String hex(Integer data) {
        if (data == null) {
            return null;
        }
        return hex((int) data);
    }

    /**
     * same as hex(data, true)
     */
    public static String hex(int data) {
        return hex(data, true);
    }

    /**
     * @param skipLeadingZeros
     *            if true leading zeros be trimmed while leaving at least two characters (like 00, 0A)
     */
    public static String hex(int data, boolean skipLeadingZeros) {
        String out = "";
        for (int i = 24; i >= 0; i -= 8) {
            out += hex((byte) (data >> i));
        }
        if (!skipLeadingZeros) {
            return out;
        }
        int i;
        for (i = 0; i < out.length() - 2; i++) {
            if (!out.substring(i, i + 1).equals("0")) {
                break;
            }
        }
        return out.substring(i);
    }

    /**
     * same as hex(data, true)
     */
    public static String hex(short data) {

        return hex(data, true);
    }

    /**
     * @param skipLeadingZeros
     *            if true leading zeros be trimmed while leaving at least two characters (like 00, 0A)
     */
    public static String hex(short data, boolean skipLeadingZeros) {

        String out = "";

        for (int i = 8; i >= 0; i -= 8) {

            out += hex((byte) (data >> i));
        }

        if (!skipLeadingZeros) {
            return out;
        }

        int i;

        for (i = 0; i < out.length() - 2; i++) {

            if (!out.substring(i, i + 1).equals("0")) {
                break;
            }
        }

        return out.substring(i);
    }

    /**
     * same as hex(data, true)
     */
    public static String hex(Long data) {
        if (data == null) {
            return null;
        }
        return hex((long) data);
    }

    /**
     * same as hex(data, true)
     */
    public static String hex(long data) {
        return hex(data, true);
    }

    /**
     * @param skipLeadingZeros
     *            if true leading zeros be trimmed while leaving at least two characters (like 00, 0A)
     */
    public static String hex(long data, boolean skipLeadingZeros) {
        StringBuffer sb = new StringBuffer(16);
        for (int i = 56; i >= 0; i -= 8) {
            sb.append(hex((byte) (data >> i)));
        }
        if (!skipLeadingZeros) {
            return sb.toString();
        }
        for (int i = 0, n = sb.length() - 2; i < n; i++) {
            if (sb.charAt(i) != '0') {
                return sb.substring(i);
            }
        }
        return sb.toString();
    }

    public static String hex(int[] data) {
        if (data == null) {
            return null;
        }
        int n = data.length;
        StringBuffer s = new StringBuffer(n * 9);
        for (int i = 0; i < n; i++) {
            s.append(hex(data[i]));
            if (i < n - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    public static String hex(long[] data) {
        if (data == null) {
            return null;
        }
        int n = data.length;
        StringBuffer s = new StringBuffer(n * 9);
        for (int i = 0; i < n; i++) {
            s.append(hex((int) data[i]));
            if (i < n - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    public static final String multilineHex(byte[] buf, int length) {
        StringBuffer sb = new StringBuffer(8000);
        for (int i = 0, n = length; i < n; i += 16) {
            sb.append("\r\n").append(Integer.toHexString(i)).append(":\t");
            for (int j = i; j < i + 16 && j < n; j++) {
                sb.append(Hex.hex(buf[j])).append(' ');
            }
        }
        return sb.toString();
    }

    public static String hex(byte[][] data) {
        return hex(data, true);
    }

    public static String hex(byte[][] data, boolean withSpaces) {
        if (data == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(Hex.hex(data[i], withSpaces));
        }
        return sb.toString();
    }

    public static byte[] byteArray(String in) {
        if (in == null) {
            return null;
        }
        in = in.trim();
        if (in.length() > 2 && in.indexOf(' ') < 0) {// no spaces
            int len = in.length();
            if (len % 2 != 0) {
                throw new NumberFormatException(
                        "must have even number of characters in input string, called with: " + in);
            }
            byte[] out = new byte[len / 2];
            for (int i = 0; i < out.length; i++) {
                out[i] = (byte) Integer.parseInt(in.substring(2 * i, 2 * i + 2), 16);
            }
            return out;
        }
        // spaces between bytes, split input on whitespace:
        Vector<Byte> v = new Vector<Byte>();
        byte[] data = new byte[0];
        try {
            byte b;
            StringTokenizer st = new StringTokenizer(in);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                int dataByte = Integer.parseInt(s, 16);
                if (dataByte < 0 || dataByte > 255) {
                    throw new java.lang.NumberFormatException("value out or byte range: " + s);
                }
                b = (byte) dataByte;
                v.addElement(Byte.valueOf(b));
            }
            data = new byte[v.size()];
            for (int i = 0; i < v.size(); i++) {
                data[i] = v.elementAt(i).byteValue();
            }
        } catch (NumberFormatException fe) {
        }
        return data;
    }

    /**
     * Converts from signed bytes to unsigned int value
     */
    public static int byte2int(int n) {
        return 0xFF & n;
    }

    public static byte BCD(byte decimal) {
        byte b1 = (byte) (decimal % 10);
        byte b2 = (byte) ((decimal - b1) / 10);
        return (byte) ((b2 << 4) | b1);
    }

    public static int bcd2int(byte bcd) {
        int d1 = bcd & 0x0F;
        int d2 = (bcd >> 4) & 0x0F;
        return 10 * d2 + d1;
    }

    /**
     * Reads text file and return its content as a String
     */
    public static String file2string(String file) throws IOException {
        return new String(file2buffer(file));
    }

    public static byte[] file2buffer(String file) throws IOException {
        FileInputStream f = new FileInputStream(file);
        byte buf[] = new byte[f.available()];
        f.read(buf);
        f.close();
        return buf;
    }

    public static byte[] file2buffer(File file) throws IOException {
        FileInputStream f = new FileInputStream(file);
        byte buf[] = new byte[f.available()];
        f.read(buf);
        f.close();
        return buf;
    }

    public static String toString(byte[] buf, int off, int len) {
        byte[] chars = new byte[len];
        for (int i = off; i < off + len; i++) {
            chars[i - off] = (buf[i] == 0x00 ? 32 : buf[i]);
        }
        return new String(chars);
    }

    public static long bytes2long(byte[] buf, int from, int len) {
        if (len > 8) {
            throw new IllegalArgumentException("len can be up to 8, called with: " + len);
        }
        long ret = 0;
        for (int i = from; i < from + len; i++) {
            ret = ret << 8;
            ret |= (0xff & buf[i]);
        }
        return ret;
    }

    public static int bytes2int(byte[] buf, int from, int len) {
        if (len > 4) {
            throw new IllegalArgumentException("len can be up to 4, called with: " + len);
        }
        return (int) bytes2long(buf, from, len);
    }

    /** Printout for longer byte arrays with line breaks after every 16 bytes */
    public static final String printNice(byte[] buf) {
        return printNice(buf, buf.length);
    }

    /** Printout for longer byte arrays with line breaks after every 16 bytes */
    public static final String printNice(byte[] buf, int length) {
        StringBuffer sb = new StringBuffer(8000);
        for (int i = 0, n = length; i < n; i += 16) {
            if (sb.length() > 0) {
                sb.append("\r\n");
            }
            sb.append(hex(i, false)).append(":\t");
            for (int j = i; j < i + 16 && j < n; j++) {
                sb.append(Hex.hex(buf[j])).append(' ');
            }
        }
        return sb.toString();
    }

    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        return sb.toString();
    }
}
