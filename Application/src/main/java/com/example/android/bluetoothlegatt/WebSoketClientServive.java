package com.example.android.bluetoothlegatt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.http2.Http2Connection;
import okio.ByteString;

public class WebSoketClientServive extends Service {

    private OkHttpClient client;
    private Request request;
    private MyWebSocketListener listener;
    private static WebSocket webSocket;

    private final IBinder mBinder = new WebSocketsBinder();

    public void onCreate() {
        this.request = new Request.Builder().url("ws://192.168.1.135").build();

        this.listener = new MyWebSocketListener();

        this.client = new OkHttpClient();
        this.webSocket = client.newWebSocket(request, listener);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public final class WebSocketsBinder extends Binder {
        public WebSoketClientServive getService() {

            return WebSoketClientServive.this;
        }
    }

        private final class MyWebSocketListener extends WebSocketListener {
            private static final int NORMAL_CLOSURE_STATUS = 1000;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send("What's up ?");
                //webSocket.send(ByteString.decodeHex("deadbeef"));
                //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                broadcastUpdate("Receiving : " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                broadcastUpdate("Receiving bytes : " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                broadcastUpdate("Closing : " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                broadcastUpdate("Error : " + t.getMessage());
                //this.client.dispatcher().executorService().shutdown();

            }
        }
}
