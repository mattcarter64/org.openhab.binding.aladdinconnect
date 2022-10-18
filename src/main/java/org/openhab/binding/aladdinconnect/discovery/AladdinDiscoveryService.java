package org.openhab.binding.aladdinconnect.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.aladdinconnect.handler.AladdinBridgeHandler;
import org.openhab.binding.aladdinconnect.handler.Device;
import org.openhab.binding.aladdinconnect.handler.DevicesResponse;
import org.openhab.binding.aladdinconnect.handler.Door;
import org.openhab.binding.aladdinconnect.handler.OauthRequestBody;
import org.openhab.binding.aladdinconnect.handler.OauthResponse;
import org.openhab.binding.aladdinconnect.http.HttpGETRequest;
import org.openhab.binding.aladdinconnect.http.HttpPOSTRequest;
import org.openhab.binding.aladdinconnect.http.HttpResponse;
import org.openhab.binding.aladdinconnect.http.JettyUtil;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.internal.config.AladdinBridgeConfig;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.aladdin")
public class AladdinDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(AladdinDiscoveryService.class);

    private static Gson gson = new Gson();

    private AladdinBridgeHandler bridgeHandler;
    private AladdinBridgeConfig config;

    public AladdinDiscoveryService() {
        super(AladdinConnectBindingConstants.DISCOVERABLE_DEVICE_TYPES_UIDS, 0);
    }

    public AladdinDiscoveryService(AladdinBridgeHandler bridgeHandler) {
        super(AladdinConnectBindingConstants.DISCOVERABLE_DEVICE_TYPES_UIDS, 0);

        this.bridgeHandler = bridgeHandler;
    }

    public void startDiscovery(@NonNull ChannelUID channelUID) {

        logger.info("startDiscovery:");

        config = bridgeHandler.getConfigAs(AladdinBridgeConfig.class);

        doDiscovery(channelUID);
    }

    public void stopDiscovery() {

        logger.info("stopDiscovery:");
    }

    @Override
    protected void startScan() {

        logger.info("startScan:");
    }

    @Override
    protected synchronized void stopScan() {

        logger.info("stopScan:");

        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {

        logger.info("startBackgroundDiscovery:");

        super.startBackgroundDiscovery();
    }

    @Override
    protected void stopBackgroundDiscovery() {

        logger.info("stopBackgroundDiscovery:");

        super.stopBackgroundDiscovery();
    }

    private void doDiscovery(ChannelUID channelUID) {

        logger.debug("doDiscovery:");

        try {
            getOauthToken();

            getDevices();
        } catch (Exception e) {
            logger.error("Discovery error. error=", e);
        }

        bridgeHandler.updateState(channelUID, OnOffType.OFF);
    }

    private void getOauthToken() throws IOException {

        logger.info("getOauthToken:");

        String url = config.getApiUrlBase() + AladdinConnectBindingConstants.LOGIN_ENDPOINT;

        HttpPOSTRequest request = new HttpPOSTRequest(url, TimeUnit.SECONDS.toMillis(5));
        //
        request.setHeaders(setupOauthHeaders());

        OauthRequestBody body = new OauthRequestBody();

        body.setUsername(config.getUserId());
        body.setPassword(config.getPassword());

        logger.debug("getOauthToken: OAUTH data=[{}]", body);

        request.setContentType("application/x-www-form-urlencoded");
        request.setContentProvider(body.getFormContent());

        HttpResponse response = JettyUtil.executeHttpRequest(request);

        logger.debug("getOauthToken: response={}", response);

        proccessOauthResponse(response);
    }

    // {
    // "refresh_token": "840C761CEA82AD92",
    // "token_type": "bearer",
    // "user_id": 728447,
    // "expires_in": 86400,
    // "access_token": "F8E45DBD3D9E6F4F",
    // "scope": "operator"
    // }

    private void proccessOauthResponse(HttpResponse response) {

        if (response.getResponseStr() != null) {
            OauthResponse oresp = gson.fromJson(response.getResponseStr(), OauthResponse.class);

            logger.debug("proccessOauthResponse: OAUTH response=[{}]", oresp);

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("authToken", oresp.getAccess_token());

            bridgeHandler.handleConfigurationUpdate(parameters);
        }
    }

    private void getDevices() throws IOException {

        logger.info("getDevices:");

        String url = bridgeHandler.getConfigAs(AladdinBridgeConfig.class).getApiUrlBase()
                + AladdinConnectBindingConstants.DEVICE_ENDPOINT;

        HttpGETRequest request = new HttpGETRequest(url, TimeUnit.SECONDS.toMillis(5));

        //
        request.setHeaders(setupOauthHeaders());
        request.getHeaders().put("Authorization", "Bearer " + config.getAuthToken());

        HttpResponse response = JettyUtil.executeHttpRequest(request);

        processDevicesResponse(response);
    }

    private void processDevicesResponse(HttpResponse response) {

        logger.info("processDevicesResponse: response=[{}]", response);

        if (response.getResponseStr() != null) {
            DevicesResponse dresp = gson.fromJson(response.getResponseStr(), DevicesResponse.class);

            logger.debug("processDevicesResponse: hydrated POJO=[{}]", dresp);

            for (Device device : dresp.getDevices()) {
                for (Door door : device.getDoors()) {
                    addDoor(door);
                }
            }
        }
    }

    public void addDoor(Door door) {

        ThingUID bridgeUID = bridgeHandler.getThing().getUID();

        Map<String, Object> properties = new HashMap<>();

        properties.put(AladdinConnectBindingConstants.PROP_OH_ID, door.getOhId());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_ID, door.getDevice_id());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_NAME, door.getName());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_NUMBER, door.getDoor_index());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_ENABLED, door.is_enabled());
        properties.put(AladdinConnectBindingConstants.PROP_DOOR_DATE_CREATED, door.getCreated_at());

        ThingUID uid = new ThingUID(AladdinConnectBindingConstants.THING_TYPE_GARAGE_DOOR, bridgeUID, door.getOhId());

        logger.debug("addDoor: bridgeUID={}, type={}, uid={}", bridgeUID.getId(),
                AladdinConnectBindingConstants.GARAGE_DOOR, uid);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(door.getName())
                .withProperties(properties).withRepresentationProperty(AladdinConnectBindingConstants.PROP_OH_ID)
                .build();

        logger.debug("addDoor: result={}", result);

        thingDiscovered(result);
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
}
