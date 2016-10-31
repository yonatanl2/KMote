package com.levyinc.android.kodimote;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends Fragment {

    private View rootView;
    private Handler connectHandler = new Handler();

    EditText inputIp;
    EditText inputPort;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    StringBuffer jsonString;
    ConnectivityManager cm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.settings_activity, container, false);



        cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();



        inputIp = (EditText) rootView.findViewById(R.id.input_ip_edit_text);
        inputIp.setText(sharedPreferences.getString("input_ip", ""));
        inputPort = (EditText) rootView.findViewById(R.id.input_port_edit_text);
        inputPort.setText(sharedPreferences.getString("input_port", ""));

        connectHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String mString = (String) msg.obj;
                Toast.makeText(getContext(), mString, Toast.LENGTH_SHORT).show();
                if (mString.equals("Success")) {
                    editor.putString("successful_connection", "y");
                    editor.apply();
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
                editor.putString("input_ip", inputIp.getText().toString());
                editor.apply();
                editor.putString("input_port", inputPort.getText().toString());
                editor.apply();

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    Thread thread = new Thread(new Connecter(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", "")));
                    thread.start();
                } else {
                    Toast.makeText(getActivity(), "Wifi connection not detected", Toast.LENGTH_SHORT).show();
                    editor.putString("successful_connection", "n");
                    editor.apply();
                }
            }
        });



        return rootView;
    }


    public class AsyncScan extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.newDialog);

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
                            if (addr.getHostAddress().indexOf(':')<0) {
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
                        try {
                            URL url = new URL("http://" + (tempString + "." + i) + ":8080/jsonrpc");
                            System.out.println("attempting: " + url);
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
                                System.out.println(url);
                                status = true;
                                sharedPreferences.edit().putString("input_ip", tempString + "." + i).apply();
                                sharedPreferences.edit().putString("input_port", "8080").apply();
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
            progressDialog.setTitle("Scanning");
            progressDialog.setMessage("Wait while KodiMote is scanning for available devices...");
            progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean response) {
            progressDialog.dismiss();
            if (response){
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No Device Found...", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button tcpButton = (Button) rootView.findViewById(R.id.scan_button);
        tcpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    new AsyncScan().execute();
                } else {
                    Toast.makeText(getContext(), "No WIFI connnection detected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class Connecter implements Runnable {

        String IP;
        String port;

        Connecter (String ip, String port){
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
                if (exception.toString().contains("java.net.UnknownHostException") || exception.toString().contains("java.net.ConnectException")) {
                    Message msg=new Message();
                    msg.obj="Failed";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();

                }
            }
        }
    }
}
