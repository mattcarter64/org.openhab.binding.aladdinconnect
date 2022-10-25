package org.openhab.binding.aladdinconnect.handler;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.openhab.binding.aladdinconnect.internal.AladdinConnectBindingConstants;
import org.openhab.binding.aladdinconnect.util.ThreadManager;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AladdinEventHandler {

    private final Logger logger = LoggerFactory.getLogger(AladdinEventHandler.class);

    private static final String WSURI = "wss://event-caster.st1.gdocntl.net/updates";

    private WebSocketClient client;
    private MyWebSocket websocket;

    private final AladdinBridgeHandler bridgeHandler;

    private boolean stayRunning = true;

    private boolean connected = false;

    private static final ReentrantLock connectingLock = new ReentrantLock();
    private static final Condition connectingCondition = connectingLock.newCondition();

    public AladdinEventHandler(AladdinBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public void start() throws Exception {

        logger.info("start:");

        HttpClient httpClient = new HttpClient(new SslContextFactory.Client());

        client = new WebSocketClient(httpClient);

        client.start();

        while (stayRunning) {
            if (!connected) {
                try {
                    connectingLock.lock();

                    connect();

                    try {
                        connectingCondition.await(20, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                    }
                } finally {
                    connectingLock.unlock();
                }
            }

            try {
                Thread.sleep(5000);
            } catch (Throwable ignored) {
            }
        }
    }

    private void connect() throws Exception {

        URI destUri = new URI(WSURI);

        ClientUpgradeRequest request = new ClientUpgradeRequest();

        request = setupHeaders(request);

        logger.info("connect: connecting to: {}", destUri);

        websocket = new MyWebSocket();

        client.connect(websocket, destUri, request);
    }

    private ClientUpgradeRequest setupHeaders(ClientUpgradeRequest request) {

        request.setHeader("Authorization", bridgeHandler.getBearerHeader());

        return request;
    }

    public void stop() throws Exception {

        logger.info("stop:");

        client.stop();

        client = null;

        websocket.stopPingTask(null);

        websocket = null;

        stayRunning = false;
    }

    @WebSocket
    public class MyWebSocket {

        private final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

        // private final CountDownLatch closeLatch = new CountDownLatch(1);

        private ScheduledFuture<?> pingTask;

        @OnWebSocketConnect
        public void onConnect(Session session) throws IOException {

            try {
                connectingLock.lock();

                logger.info("onConnect: session={}", session);

                startPingTask(session);

                logger.info("onConnect: upgrade response={}, batch mode={}", session.getUpgradeResponse(),
                        ((WebSocketSession) session).getBatchMode());

                connected = true;

                connectingCondition.signal();
            } finally {
                connectingLock.unlock();
            }
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) {

            logger.info("onMessage(String): message={}", message);

            updateStatus(message);
        }

        // @OnWebSocketMessage
        // public void onMessage(Session session, Reader reader) {
        //
        // logger.info("onMessage(Reader):");
        // }

        @OnWebSocketMessage
        public void onMessage(Session session, byte buf[], int offset, int length) {

            logger.info("onMessage(buf[]): buf={}", new String(buf));
        }

        // @OnWebSocketMessage
        // public void onMessage(Session session, InputStream stream) {
        //
        // logger.info("onMessage(InputStream):");
        // }

        @OnWebSocketFrame
        public void onFrame(Session sesison, Frame frame) {

            logger.info("onFrame: message={}", frame);
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {

            logger.info("onClose: status={}, reason={}", statusCode, reason);

            try {
                connectingLock.lock();

                stopPingTask(session);

                connected = false;

                connectingCondition.signal();
            } finally {
                connectingLock.unlock();
            }
        }

        //
        @OnWebSocketError
        public void onError(Session session, Throwable error) {

            try {
                connectingLock.lock();

                logger.error("onError: message={}, session={}", error.getMessage(), session);

                stopPingTask(session);

                connected = false;

                connectingCondition.signal();
            } finally {
                connectingLock.unlock();
            }
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

        // public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        // return this.closeLatch.await(duration, unit);
        // }
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
                // String token = bridgeHandler.getAuthToken();

                session.getRemote().sendPing(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).flip());
                // session.getRemote().sendPing(ByteBuffer.allocate(token.length()).put(token.getBytes()));

                logger.debug("scheduling door update task ...");

                ScheduledFuture<?> task = ThreadPoolManager
                        .getScheduledPool(AladdinConnectBindingConstants.SCHED_THREAD_POOL_NAME)
                        .schedule(new UpdateDoorStatusTask(bridgeHandler), 10, TimeUnit.SECONDS);
                task.get();
            } catch (Exception e) {
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
