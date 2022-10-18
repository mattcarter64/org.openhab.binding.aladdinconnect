package org.openhab.binding.aladdinconnect.handler;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OauthResponse {

    private String refresh_token;
    private String token_type;
    private String user_id;
    private String expires_in;
    private String access_token;
    private String scope;
}
