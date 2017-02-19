package com.levyinc.android.kodimote;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends Fragment {

    private View rootView;
    private Handler connectHandler = new Handler();
    ProgressDialog socketDialog;

    EditText inputIp;
    EditText inputPort;
    SharedPreferences sharedPreferences;
    StringBuffer jsonString;
    ConnectivityManager cm;
    WebSocketEndpoint webSocketEndpoint;
    Boolean response = false;
    Long connectTime;
    ScannerHandler scannerHandler;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.settings_activity, container, false);

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);

        inputIp = (EditText) rootView.findViewById(R.id.input_ip_edit_text);
        inputIp.setText(sharedPreferences.getString("input_ip", ""));
        inputPort = (EditText) rootView.findViewById(R.id.input_port_edit_text);
        inputPort.setText(sharedPreferences.getString("input_port", ""));
        final SwitchCompat socketSwitch = (SwitchCompat) rootView.findViewById(R.id.connect_ws_switch);
        if (sharedPreferences.getString("WS", "").equals("y")) {
            socketSwitch.setChecked(true);
        }


        connectHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String mString = (String) msg.obj;
                Snackbar.make(rootView, mString, Snackbar.LENGTH_SHORT).show();
                if (mString.equals("Success")) {
                    editor.putString("successful_connection", "y").apply();
                    editor.putString("WS", "n").apply();
                } else {
                    editor.putString("successful_connection", "n");
                    editor.apply();
                }
            }
        };


        Button button = (Button) rootView.findViewById(R.id.connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main2Activity.setWsActive();
                sharedPreferences.edit().putString("input_ip", inputIp.getText().toString()).apply();
                sharedPreferences.edit().putString("input_port", inputPort.getText().toString()).apply();
                if (socketSwitch.isChecked()) {
                    socketDialog = new ProgressDialog(getContext(), R.style.newDialog);
                    socketDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            connectHandler.removeCallbacksAndMessages(socketRunnable);
                        }
                    });
                    socketDialog.setTitle("Connecting to WebSocket");
                    socketDialog.setMessage("Attempting to connect to WebSocket\n" + sharedPreferences.getString("input_ip", "") + ":" + sharedPreferences.getString("input_port", ""));
                    socketDialog.setCancelable(true); // disable dismiss by tapping outside of the dialog
                    socketDialog.show();
                    Calendar cal = Calendar.getInstance();
                    connectTime = cal.getTimeInMillis();
                    webSocketEndpoint = new WebSocketEndpoint(inputIp.getText().toString(), inputPort.getText().toString());
                    response = false;
                    socketRunnable.run();

                } else {
                    sharedPreferences.edit().putString("WS", "n").apply();
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    try {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            Thread thread = new Thread(new HTTPConnector(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", "")));
                            thread.start();
                        } else {
                            Snackbar.make(view, "Wifi connection not detected", Snackbar.LENGTH_SHORT).show();
                            sharedPreferences.edit().putString("successful_connection", "n").apply();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        Button scanButton = (Button) rootView.findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.stopAsynchTask();
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (socketSwitch.isChecked()) {
                        final SocketScanner socketScanner = new SocketScanner(sharedPreferences);
                        final Thread t1 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                socketScanner.scan(scannerHandler);
                            }
                        });
                        final Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                socketScanner.scanWebSocket(scannerHandler);
                            }
                        });
                        socketScanner.setCheckerThread(t2);
                        socketDialog = new ProgressDialog(getContext(), R.style.newDialog);
                        socketDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                connectHandler.removeCallbacksAndMessages(socketRunnable);
                            }
                        });
                        socketDialog.setTitle("Scanning for WebSocket");
                        socketDialog.setMessage("Wait while KodiMote is scanning for available WebSocket...");
                        socketDialog.setCancelable(true); // disable dismiss by tapping outside of the dialog
                        socketDialog.show();
                        socketDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                t1.interrupt();
                                t2.interrupt();
                                socketDialog.dismiss();
                            }
                        });
                        scannerHandler = new ScannerHandler(v, socketDialog);
                        t1.setName("scanning thread");
                        t2.setName("running thread");
                        t2.start();
                        t1.start();

                    }
                    else {
                        new AsyncScan(v, getContext()).execute();
                    }
                } else {
                    Snackbar.make(v, "No WIFI connnection detected", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        return rootView;
    }

    Runnable socketRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (!response) {
                    if (webSocketEndpoint.getOnOpenMessage() != null) {
                        Snackbar.make(getView(), "Success", Snackbar.LENGTH_SHORT).show();
                        sharedPreferences.edit().putString("WS", "y").apply();
                        sharedPreferences.edit().putString("input_ip", inputIp.getText().toString()).apply();
                        sharedPreferences.edit().putString("input_port", inputPort.getText().toString()).apply();
                        sharedPreferences.edit().putString("successful_connection", "y").apply();
                        webSocketEndpoint.disconnect();
                        response = true;
                        socketDialog.dismiss();
                    } else if (webSocketEndpoint.getCloseMessage() != null) {
                        if (webSocketEndpoint.getCloseMessage().equals("No route to host")) {
                            Snackbar.make(getView(), "Failed", Snackbar.LENGTH_SHORT).show();
                            sharedPreferences.edit().putString("WS", "n").apply();
                            sharedPreferences.edit().putString("successful_connection", "n").apply();

                        }
                        response = true;
                        socketDialog.dismiss();
                    } else if (connectTime + 10000 < Calendar.getInstance().getTimeInMillis()) {
                        socketDialog.dismiss();
                        sharedPreferences.edit().putString("WS", "n").apply();
                        sharedPreferences.edit().putString("successful_connection", "n").apply();
                        response = true;
                        Snackbar.make(getView(), "Failed", Snackbar.LENGTH_SHORT).show();
                    } else {
                        connectHandler.postDelayed(socketRunnable, 300);
                    }
                } else {
                    socketDialog.dismiss();
                    sharedPreferences.edit().putString("WS", "y").apply();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    };

    class AsyncScan extends AsyncTask<Void, Void, Boolean> {

        View currentView;
        ProgressDialog progressDialog;
        String port = "8080";


        AsyncScan(View view, Context context) {
            this.currentView = view;
            progressDialog = new ProgressDialog(context, R.style.newDialog);
            progressDialog.setTitle("Scanning");
            progressDialog.setMessage("Wait while KodiMote is scanning for available devices...");
            progressDialog.setCancelable(true); // disable dismiss by tapping outside of the dialog
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean status = false;
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
                            if (isCancelled()) {
                                break;
                            }
                            try {
                                URL url = new URL("http://" + (tempString + "." + i) + ":" + port + "/jsonrpc");
                                Log.println(Log.WARN, "Scanner", "Scanning Network: " + url.toString());
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setDoOutput(true);
                                conn.setConnectTimeout(1500);
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json");
                                JSONObject jsonParam = new JSONObject();
                                jsonParam.put("jsonrpc", "2.0");
                                jsonParam.put("method", "GUI.ShowNotification");
                                jsonParam.put("id", 1);
                                JSONObject jsonParam2 = new JSONObject();
                                jsonParam2.put("title", "New remote connection");
                                jsonParam2.put("message", "KodiMote is connected");
                                jsonParam.put("params", jsonParam2);
                                byte[] bytes = jsonParam.toString().getBytes("UTF-8");
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                conn.setUseCaches(false);
                                conn.connect();
                                OutputStream printout = conn.getOutputStream();
                                printout.write(bytes);
                                printout.flush();
                                printout.close();
                                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                jsonString = new StringBuffer();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    jsonString.append(line);
                                }
                                br.close();
                                if (conn.getResponseCode() == 200) {
                                    System.out.println(url);
                                    status = true;
                                    sharedPreferences.edit().putString("input_ip", tempString + "." + i).apply();
                                    sharedPreferences.edit().putString("input_port", port).apply();
                                    sharedPreferences.edit().putString("successful_connection", "y").apply();
                                    break;
                                } else {
                                    sharedPreferences.edit().putString("successful_connection", "n").apply();
                                }

                            } catch (IOException | JSONException exception) {
                                Log.i("connection exceptions", exception.toString());
                            }
                        }
                    }
                } else {
                    sharedPreferences.edit().putString("successful_connection", "n").apply();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPreExecute() {

            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean response) {
            progressDialog.dismiss();
            if (response) {
                Snackbar.make(currentView, "Success", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(currentView, "No Device Found...", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    class HTTPConnector implements Runnable {

        String IP;
        String port;

        HTTPConnector (String ip, String port){
            this.IP = ip;
            this.port = port;
        }

        @Override
        public void run() {

            try {

                URL url = new URL("http://" + this.IP + ":" + this.port + "/jsonrpc");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "GUI.ShowNotification");
                jsonParam.put("id", 1);
                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("title", "New remote connection");
                jsonParam2.put("message", "KodiMote is connected");

                jsonParam.put("params", jsonParam2);


                byte[] bytes = jsonParam.toString().getBytes("UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.connect();
                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                if (conn.getResponseCode() == 200) {
                    conn.disconnect();
                    Message msg=new Message();
                    msg.obj="Success";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();
                } else {
                    conn.disconnect();
                    Message msg=new Message();
                    msg.obj="Failed";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();
                }
            }

            catch (IOException | JSONException exception) {
                exception.printStackTrace();
                if (exception.toString().contains("java.net.UnknownHostException") || exception.toString().contains("java.net.ConnectException") || exception.toString().contains("java.net.NoRouteToHostException")) {
                    Message msg=new Message();
                    msg.obj="Failed";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();

                }
            }
        }
    }

    //TODO ADD EVENT SERVER SUPPORT

}
