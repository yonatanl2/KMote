package com.levyinc.android.kodimote;

import android.content.SharedPreferences;
import android.os.Message;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class SocketScanner {

    private WebSocketClient webSocketClient;
    private SharedPreferences sharedPreferences;
    private boolean response;
    private String handShake;
    private String closeMessage;
    private boolean status;
    private String ip;
    private boolean success;
    private Thread checkerThread;

    SocketScanner(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    void scan(ScannerHandler handler) {
        try {
            String tempString = null;
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        if (addr.getHostAddress().indexOf(':') < 0) {
                            tempString = addr.getHostAddress();
                        }
                    }
                }
            }
            if (tempString != null) {
                Pattern ipPattern = Pattern.compile("\\.\\d*$");
                Matcher ipMatcher = ipPattern.matcher(tempString);
                if (ipMatcher.find()) {
                    tempString = ipMatcher.replaceAll("");
                    for (int i = 0; i < 30; i++) {
                        ip = (tempString + "." + i);
                        System.out.println("Attempting: " + ip);
                        connect();
                        if (!checkerThread.isAlive()){
                            sleep(150);
                        }
                        synchronized (this) {
                            notify();
                            wait();
                        }
                        if (success) {
                            sharedPreferences.edit().putString("input_ip", tempString + "." + i).apply();
                            sharedPreferences.edit().putString("input_port", "9090").apply();
                            sharedPreferences.edit().putString("successful_connection", "y").apply();
                            sharedPreferences.edit().putString("WS", "y").apply();
                            status = true;
                            break;
                        } else {
                            sharedPreferences.edit().putString("successful_connection", "n").apply();
                            sharedPreferences.edit().putString("WS", "n").apply();
                        }
                    }
                }
            } else {
                sharedPreferences.edit().putString("successful_connection", "n").apply();
                sharedPreferences.edit().putString("WS", "n").apply();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (status) {
            Message statusMessage = new Message();
            statusMessage.obj = "successfulConnection";
            handler.sendMessage(statusMessage);
        } else {
            Message statusMessage = new Message();
            statusMessage.obj = "failedConnection";
            handler.sendMessage(statusMessage);
        }
    }

    public void connect() {
        try {
            response = false;
            handShake = null;
            closeMessage = null;
            URI uri = new URI("ws://" + ip + ":9090/jsonrpc");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    handShake = handshakedata.toString();
                    System.out.println(handshakedata);
                }

                @Override
                public void onMessage(String message) {

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    closeMessage = reason;
                }

                @Override
                public void onError(Exception ex) {

                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }
    }

    public void scanWebSocket(ScannerHandler currentHandler){
        try {
            synchronized (this) {
                wait();
            }
            Long startTime = Calendar.getInstance().getTimeInMillis();
            while (startTime + 5000 > Calendar.getInstance().getTimeInMillis()) {
                if (!response) {
                    if (handShake != null) {
                        webSocketClient.close();
                        response = true;
                        success = true;
                        Message wsMessage = new Message();
                        wsMessage.obj = "success";
                        currentHandler.sendMessage(wsMessage);
                        currentHandler.obtainMessage();
                        synchronized (this) {
                            notify();
                        }
                        break;

                    } else if (closeMessage != null) {
                        if (closeMessage.equals("No route to host")) {
                            Message wsMessage = new Message();
                            wsMessage.obj = "failure";
                            currentHandler.sendMessage(wsMessage);
                            currentHandler.obtainMessage();
                            response = true;

                            synchronized (this) {
                                notify();
                            }
                            break;
                        } else {
                            response = true;
                            synchronized (this) {
                                notify();
                            }
                            break;
                        }
                    } else {
                        sleep(300);
                    }
                } else {
                    synchronized (this) {
                        notify();
                    }
                    break;
                }
            }
            if (!success) {
                checkerThread.start();
                synchronized (this) {
                    notify();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    void setCheckerThread(Thread checkerThread) {
        this.checkerThread = checkerThread;
    }
}
