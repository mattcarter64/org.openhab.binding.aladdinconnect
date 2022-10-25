package org.openhab.binding.aladdinconnect.internal.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AladdinBridgeConfig {

    private String userId;
    private String password;
    private String authToken;
    private boolean notifications;
    private String apiUrlBase;
    private String apiKey;
}
