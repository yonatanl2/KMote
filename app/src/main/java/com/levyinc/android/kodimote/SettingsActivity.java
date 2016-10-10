package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends Fragment {

    private View rootView;
    private Handler connectHandler = new Handler();

    EditText inputIp;
    EditText inputPort;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.settings_activity, container, false);



        final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
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




    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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

                if (conn.getResponseCode() == 200) {
                    Message msg=new Message();
                    msg.obj="Success";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();
                } else {
                    Message msg=new Message();
                    msg.obj="Failed";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();
                }
            }

            catch (IOException exception) {
                String stringException = exception.toString();
                System.out.println(stringException);
                if (stringException.contains("java.net.UnknownHostException") || stringException.contains("java.net.ConnectException")) {
                    Message msg=new Message();
                    msg.obj="Failed";
                    connectHandler.sendMessage(msg);
                    connectHandler.obtainMessage();

                }
            }
        }
    }

}



/*    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.settings_activity, container,
                false);
       // addPreferencesFromResource(R.xml.pref_general);
       // setContentView(R.layout.settings_activity);

        getChildFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment())


                .replace(android.R.id.list, new SettingsFragment())
                .commit();


        final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        Button connect = (Button) rootView.findViewById(R.id.connect_button);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    String connIP = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("kodi_ip", null);
                    String connPort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("kodi_port", null);

                    boolean connection = ButtonActions.connect(connIP, connPort);
                    if (connection) {
                        Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getActivity(), "Wifi connection not detected", Toast.LENGTH_SHORT).show();

                }
            }
        });
        return rootView;
    }

    public static class SettingsFragment extends PreferenceActivity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            @Override
            public void onBuildHeaders(List<Header> target) {
                loadHeadersFromResource(R.xml.preference_headers, target);
            }        }
    }
}*/
