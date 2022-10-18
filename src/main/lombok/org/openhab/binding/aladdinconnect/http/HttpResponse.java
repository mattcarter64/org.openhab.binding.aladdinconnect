package org.openhab.binding.aladdinconnect.http;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HttpResponse {

    private int statusCode;
    private String statusText;
    private String responseStr;
    @ToString.Exclude
    private byte[] responseBytes;

    public HttpResponse() {
    }

    public HttpResponse(int statusCode) {
        this.statusCode = statusCode;
    }
}
