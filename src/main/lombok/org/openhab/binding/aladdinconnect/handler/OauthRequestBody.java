package org.openhab.binding.aladdinconnect.handler;

import java.util.Base64;

import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.util.Fields;

import lombok.Data;
import lombok.ToString;

// Content-Type: application/x-www-form-urlencoded

//grant_type: "password"
//client_id: "1000"
//username: "mattcarter64@gmail.com"
//password: "cVY2QktIY3V1cG1eSHJoN2Fh"
//model: "Google Pixel 6"
//app_version: "5.25"
//build_number: "2038"
//os_version: "12.0.0"

@Data
@ToString
public class OauthRequestBody {

    private String grant_type = "password";
    private String client_id = "1000";
    private String brand = "ALADDIN";
    private String username;
    private String password;
    private String app_version = "5.30";
    private String model = "Google Pixel 6";
    private String build_number = "2038";
    private String os_version = "12.0.0";

    public FormContentProvider getFormContent() {

        Fields fields = new Fields();

        fields.put("grant_type", grant_type);
        fields.put("client_id", client_id);
        fields.put("brand", brand);
        fields.put("username", username);
        fields.put("password", new String(encodePassword(password)));
        fields.put("app_version", app_version);
        fields.put("model", model);
        fields.put("build_number", build_number);
        fields.put("os_version", os_version);

        return new FormContentProvider(fields);
    }

    private byte[] encodePassword(String password) {

        return Base64.getEncoder().encode(password.getBytes());
    }

}
