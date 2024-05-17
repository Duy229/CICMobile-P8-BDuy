package com.umut.soysal.util;

import static com.umut.soysal.util.LocalStorageUtil.readUrlWebsocket;

import android.content.Context;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class WebSocketClientService {

    private WebSocketClient webSocketClient;

    public void send(String response) {
        webSocketClient.send(response);
    }

    public void createWebSocketClient(Context context, String response) {
        URI uri;
        try {
            String URLWebsocket = readUrlWebsocket(context);
            uri = new URI(URLWebsocket);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                if(response != null) {
                    webSocketClient.send(response);
                }
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                Log.e("WebSocket", Objects.requireNonNull(e.getMessage()));
            }

            @Override
            public void onCloseReceived(int a, String b) {

            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.disableAutomaticReconnection();
        webSocketClient.connect();
    }
}
