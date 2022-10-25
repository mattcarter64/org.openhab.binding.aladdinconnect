package org.openhab.binding.aladdinconnect.http;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.aladdinconnect.handler.AladdinBridgeHandler;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.internal.config.AladdinBridgeConfig;
import org.openhab.binding.aladdinconnect.model.*;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class AladdinConnectClient {

    private final Logger logger = LoggerFactory.getLogger(AladdinConnectClient.class);

    private static final List<String> DOOR_TARGET_STATE = new ArrayList<String>(Arrays.asList("CloseDoor", "OpenDoor"));

    private static final Gson gson = new Gson();
    private final AladdinBridgeHandler bridgeHandler;
    private final AladdinBridgeConfig config;

    private static AladdinConnectClient instance;

    public static AladdinConnectClient instance(AladdinBridgeHandler bridgeHandler) {

        if (instance == null) {
            instance = new AladdinConnectClient(bridgeHandler);
        }

        return instance;
    }

    private AladdinConnectClient(AladdinBridgeHandler bridgeHandler) {

        this.bridgeHandler = bridgeHandler;
        this.config = bridgeHandler.getConfigAs(AladdinBridgeConfig.class);
    }

    // {
    // "refresh_token": "840C761CEA82AD92",
    // "token_type": "bearer",
    // "user_id": 728447,
    // "expires_in": 86400,
    // "access_token": "F8E45DBD3D9E6F4F",
    // "scope": "operator"
    // }

    public void login() throws IOException, AuthenticationException {

        logger.info("login:");

        String url = config.getApiUrlBase() + AladdinConnectBindingConstants.LOGIN_ENDPOINT;

        HttpPOSTRequest request = new HttpPOSTRequest(url, TimeUnit.SECONDS.toMillis(5));

        request.setHeaders(setupOauthHeaders());

        OauthRequestBody body = new OauthRequestBody();

        body.setUsername(config.getUserId());
        body.setPassword(config.getPassword());

        logger.debug("login: OAUTH data=[{}]", body);

        request.setContentType("application/x-www-form-urlencoded");
        request.setContentProvider(body.getFormContent());

        HttpResponse response = JettyUtil.executeHttpRequest(request);

        logger.debug("login: OAUTH response={}", response);

        if (response.getResponseStr() != null) {
            OauthResponse oresp = gson.fromJson(response.getResponseStr(), OauthResponse.class);

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("authToken", oresp.getAccess_token());

            logger.debug("Updating bridge configuration with new auth token");

            bridgeHandler.handleConfigurationUpdate(parameters);
        }
    }

    public List<Door> getDoors() throws IOException, AuthenticationException {

        logger.info("getDevices:");

        String url = bridgeHandler.getConfigAs(AladdinBridgeConfig.class).getApiUrlBase()
                + AladdinConnectBindingConstants.DEVICE_ENDPOINT;

        HttpGETRequest request = new HttpGETRequest(url, TimeUnit.SECONDS.toMillis(5));

        //
        request.setHeaders(setupOauthHeaders());
        request.getHeaders().put("Authorization", "Bearer " + config.getAuthToken());

        HttpResponse response = JettyUtil.executeHttpRequest(request);

        return processDevicesResponse(response);
    }

    // private self._session.call_rpc(f"/devices/{device_id}/door/{door_number}/command", payload)

    public void setDoorState(String deviceId, String deviceNumber, DecimalType command) throws AuthenticationException {

        String targetState = DOOR_TARGET_STATE.get(command.intValue());

        AladdinBridgeConfig bridgeConfig = bridgeHandler.getConfigAs(AladdinBridgeConfig.class);

        String url = bridgeConfig.getApiUrlBase() + AladdinConnectBindingConstants.DEVICE_ENDPOINT + "/" + deviceId
                + "/door/" + deviceNumber + "/command";

        logger.info("setDoorState: sending '{}' to url [{}] ...", targetState, url);

        HttpPOSTRequest request = new HttpPOSTRequest(url, TimeUnit.SECONDS.toMillis(5));

        request.setHeaders(setupHeaders());

        request.setContentType("application/json");

        Map<String, String> payload = new HashMap<>();

        payload.put("command_key", targetState);

        String json = gson.toJson(payload);

        logger.debug("setDoorState: payload={}", json);

        request.setContentProvider(new StringContentProvider(json));

        try {
            HttpResponse response = JettyUtil.executeHttpRequest(request);

            logger.debug("setDoorState: response={}", response);

            switch (response.getStatusCode()) {
                case 200:
                case 204:
                    return;
                case 400:
                    if (response.getResponseStr().contains("Door is already")) {
                        return;
                    }
                    break;
            }

            logger.warn("Received status code {} from HTTP call", response.getStatusCode());
        } catch (IOException e) {
            logger.error("Error sending command.", e);
        } catch (AuthenticationException e) {
            throw e;
        }
    }

    private Properties setupOauthHeaders() {

        Properties headers = new Properties();

        headers.put(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
        headers.put("AppVersion", AladdinConnectBindingConstants.AUTH_APP_VERSION);
        headers.put("BundleName", AladdinConnectBindingConstants.AUTH_BUNDLE_NAME);
        headers.put("BuildVersion", AladdinConnectBindingConstants.AUTH_BUILD_VERSION);
        headers.put("X-Api-Key", config.getApiKey());

        return headers;
    }

    private List<Door> processDevicesResponse(HttpResponse response) {

        logger.info("processDevicesResponse: response=[{}]", response);

        List<Door> doors = new ArrayList<>();

        if (response.getResponseStr() != null) {
            DevicesResponse dresp = gson.fromJson(response.getResponseStr(), DevicesResponse.class);

            logger.debug("processDevicesResponse: hydrated POJO=[{}]", dresp);

            for (Device device : dresp.getDevices()) {
                if (device.getDoors() != null) {
                    doors.addAll(device.getDoors());
                }
            }
        }

        return doors;
    }

    private Properties setupHeaders() {

        Properties headers = new Properties();

        headers.put("Authorization", bridgeHandler.getBearerHeader());
        headers.put(HttpHeader.CONTENT_TYPE.asString(), "application/json");
        headers.put("AppVersion", AladdinConnectBindingConstants.AUTH_APP_VERSION);
        headers.put("BundleName", AladdinConnectBindingConstants.AUTH_BUNDLE_NAME);
        headers.put("BuildVersion", AladdinConnectBindingConstants.AUTH_BUILD_VERSION);
        headers.put("X-Api-Key", bridgeHandler.getApiKey());

        return headers;
    }
}
