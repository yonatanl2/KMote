package com.levyinc.android.kodimote;

import android.os.Handler;
import android.os.Message;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;


class WebSocketEndpoint {

    private String IP;
    private String port;
    private WebSocketClient webSocketClient;

    Handler msgHandler = new Handler();

    WebSocketEndpoint (String ip, String port) {
        this.IP = ip;
        this.port = port;

        try {
            URI uri = new URI("ws://" + this.IP + ":" + this.port + "/jsonrpc");
            System.out.println(uri);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println(handshakedata);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println(code + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }

                @Override
                public void send(String text) throws NotYetConnectedException {
                    super.send(text);
                }
            };
            webSocketClient.connect();
        } catch (Exception exception) {
            exception.printStackTrace();
            if (exception.toString().contains("java.net.UnknownHostException") || exception.toString().contains("java.net.ConnectException")) {
                Message msg = new Message();
                msg.obj = "Failed";
                msgHandler.sendMessage(msg);
                msgHandler.obtainMessage();

            }
        }
    }
    void sendMessage() {
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("jsonrpc", "2.0");
            jsonParam.put("method", "GUI.ShowNotification");
            jsonParam.put("id", 1);
            JSONObject jsonParam2 = new JSONObject();
            jsonParam2.put("title", "New remote connection");
            jsonParam2.put("message", "WEBSOCKET");
            jsonParam.put("params", jsonParam2);
            //webSocketClient.send(jsonParam.toString());
            byte[] bytes = jsonParam.toString().getBytes();

            webSocketClient.send(bytes);
            webSocketClient.close();

        } catch (Exception exception){
            exception.printStackTrace();
        }

    }
}
