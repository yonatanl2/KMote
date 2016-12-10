package com.levyinc.android.kodimote;

import android.os.Handler;
import android.os.Message;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;


class WebSocketEndpoint {

    private WebSocketClient webSocketClient;
    private Handler msgHandler = new Handler();
    private String responseMessage;
    private String exceptionMessage;
    private String closeMessage;
    private String onOpenMessage;


    WebSocketEndpoint (String ip, String port) {
        try {
            URI uri = new URI("ws://" + ip + ":" + port + "/jsonrpc");
            System.out.println(uri);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    onOpenMessage = handshakedata.toString();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println(message);
                    responseMessage = message;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println(reason);
                    closeMessage = reason;
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                    exceptionMessage = ex.toString();

                }

                @Override
                public void send(String text) throws NotYetConnectedException {
                    super.send(text);
                    System.out.println("I GOT THIS: " + text);
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
            byte[] bytes = jsonParam.toString().getBytes();

            webSocketClient.send(bytes);
            webSocketClient.close();

        } catch (Exception exception){
            exception.printStackTrace();
        }

    }

    private Runnable downRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Down");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void down() {
        msgHandler.post(downRun);
    }

    private Runnable upRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Up");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void up() {
        msgHandler.post(upRun);
    }

    private Runnable rightRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Right");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void right() {
        msgHandler.post(rightRun);
    }


    private Runnable leftRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Left");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void left() {
        msgHandler.post(leftRun);
    }

    private Runnable selectRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Select");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };


    void select() {
        msgHandler.post(selectRun);
    }


    private Runnable backRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Back");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void back() {
        msgHandler.post(backRun);
    }

    private Runnable homeRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Home");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };


    void home(){
        msgHandler.post(homeRun);
    }

    String getCloseMessage() {
        return closeMessage;
    }

    String getOnOpenMessage() {
        return onOpenMessage;
    }

    void disconnect() {
        webSocketClient.close();
    }

}
