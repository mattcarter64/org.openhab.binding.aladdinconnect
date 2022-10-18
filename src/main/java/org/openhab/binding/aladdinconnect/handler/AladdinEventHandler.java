package org.openhab.binding.aladdinconnect.handler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.aladdinconnect.internal.config.AladdinBridgeConfig;
import org.openhab.binding.aladdinconnect.util.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AladdinEventHandler {

    private final Logger logger = LoggerFactory.getLogger(AladdinEventHandler.class);

    private static final String WSURI = "wss://event-caster.st1.gdocntl.net/updates";

    private AladdinBridgeConfig config;

    private WebSocketClient client;

    public AladdinEventHandler(AladdinBridgeConfig config) {
        this.config = config;
    }

    public void start() throws Exception {

        logger.info("start:");

        HttpClient httpClient = new HttpClient(new SslContextFactory.Client());

        client = new WebSocketClient(httpClient);

        client.start();

        setup();
    }

    private void setup() throws Exception {

        URI destUri = new URI(WSURI);

        ClientUpgradeRequest request = new ClientUpgradeRequest();

        request = setupHeaders(request);

        logger.info("setup: connecting to: {}", destUri);

        client.connect(new MyWebSocket(), destUri, request);
    }

    private ClientUpgradeRequest setupHeaders(ClientUpgradeRequest request) {

        request.setHeader("Authorization", "Bearer " + config.getAuthToken());

        return request;
    }

    public void stop() throws Exception {

        logger.info("stop:");

        client.stop();

        client = null;
    }

    @WebSocket
    public class MyWebSocket {

        private final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

        private final CountDownLatch closeLatch = new CountDownLatch(1);

        private ScheduledFuture<?> pingTask;

        @OnWebSocketConnect
        public void onConnect(Session session) throws IOException {

            logger.info("onConnect: session={}", session);

            startPingTask(session);
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) {

            logger.info("onMessage: message={}", message);

            updateStatus(message);
        }

        /*
         * for door in self._doors:
         * if all(key in json_msg for key in ('device_status','serial')):
         * # Server is reporting state change disconnection. Need to restart web socket
         * if json_msg['serial'] == door['serial'] and json_msg['device_status'] == 0:
         * _LOGGER.info(f"Reconnecting because we Received socket disconnect message {json_msg}")
         * return False
         *
         * # There are multiple messages from the websocket for the same value - filter this off
         * if all(key in json_msg for key in ('serial','door','door_status')):
         * if json_msg['serial'] == door['serial'] and json_msg['door'] == door['door_number'] and
         * self.DOOR_STATUS[json_msg['door_status']] != door['status']:
         * door.update({'status': self.DOOR_STATUS[json_msg["door_status"]]})
         * _LOGGER.info(f"Status Updated {self.DOOR_STATUS[json_msg['door_status']]}")
         * if self._attr_changed: # There is a callback
         * for serial in self._attr_changed:
         * lookup = f"{json_msg['serial']}-{json_msg['door']}"
         * if lookup == serial: #the door is registered as a callback
         * await self._attr_changed[lookup]() # callback the door triggered
         * else:
         * _LOGGER.info(f"Status NOT updated {self.DOOR_STATUS[json_msg['door_status']]}")
         * return True
         */
        private void updateStatus(String message) {
            // TODO Auto-generated method stub
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {

            logger.info("onClose: status={}, reason={}", statusCode, reason);

            stopPingTask(session);
        }

        @OnWebSocketFrame
        public void onFrame(Session sesison, Frame frame) {

            logger.info("onFrame: message={}", frame);
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }

        @OnWebSocketError
        public void onError(Session session, Throwable error) {

            logger.error("onError: message={}", error.getMessage());
            logger.error("onError: session={}", session, error);

            stopPingTask(session);
        }

        private void startPingTask(Session session) {

            logger.info("startPingTask:");

            pingTask = ThreadManager.scheduleFixedRateTask(new SendPingTask(session), 10, 45, TimeUnit.SECONDS);
        }

        private void stopPingTask(Session session) {

            if (pingTask != null) {
                logger.info("stopPingTask:");

                pingTask.cancel(false);
                pingTask = null;
            }
        }
    }

    public class SendPingTask implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(SendPingTask.class);

        private Session session;

        public SendPingTask(Session session) {
            this.session = session;
        }

        @Override
        public void run() {

            logger.debug("run: sendPing()");

            try {
                session.getRemote().sendPing(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).flip());
            } catch (IOException e) {
                logger.error("Error sending ping.", e);
            }
        }
    }

    // private static final String charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // private static Random random = new Random(System.currentTimeMillis());
    //
    // private byte[] createKey(int length) {
    //
    // StringBuffer sb = new StringBuffer();
    //
    // for (int i = 0; i < length; i++) {
    //
    // int pos = random.nextInt(charset.length());
    //
    // sb.append(charset.charAt(pos));
    // }
    // return sb.toString().getBytes();
    // }
}
