package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;


public class PlaylistActivity extends Fragment {

private static ArrayList<JSONObject> playlistArray;

    View rootView;
    SharedPreferences sharedPreferences;
    NetworkInfo activeNetwork;
    Handler playlistHandler = new Handler();
    ListView listView;
    Context mContext;
    ArrayList<JSONObject> currentlySetArray;
    TextView connectedText;

    private Runnable connectionChecker = new Runnable() {
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y")) {
                try {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (connectedText.getVisibility() != View.VISIBLE && currentlySetArray.isEmpty()) {
                            connectedText.setVisibility(View.VISIBLE);
                        }
                        if (connectedText.getText().toString() != "Connecting..."){
                            connectedText.setText("Connecting...");
                        }
                        if (ButtonActions.getStatus() || Main2Activity.getWebSocketStatus()) {
                            if (!connectedText.getText().toString().equals("Connected")){
                                connectedText.setText("Connected");
                            }
                            if (playlistArray.size() > 0 && playlistArray != currentlySetArray) {
                                ArrayList<PlaylistArrayObject> objectArrayList = new ArrayList<>();
                                for (int i = 0; i < playlistArray.size(); i++) {
                                    JSONObject tempJSON = playlistArray.get(i);
                                    int[] nums = new int[2];
                                    nums[0] = tempJSON.getInt("season");
                                    nums[1] = tempJSON.getInt("episode");
                                    PlaylistArrayObject object = new PlaylistArrayObject(tempJSON.getString("showtitle"), nums);
                                    objectArrayList.add(object);
                                }
                                PlaylistArrayAdapter playlistArrayAdapter = new PlaylistArrayAdapter(getContext(), objectArrayList, R.layout.playlist_items);
                                listView.setAdapter(playlistArrayAdapter);
                                connectedText.setVisibility(View.INVISIBLE);
                                currentlySetArray = playlistArray;
                            } else if (playlistArray != currentlySetArray) {
                                currentlySetArray = playlistArray;
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                playlistHandler.postDelayed(connectionChecker, 1000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.playlist_layout, container, false);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        mContext = getContext();
        listView = (ListView) rootView.findViewById(R.id.playlist_list_view);
        connectedText = (TextView) rootView.findViewById(R.id.connecting_playlist_menu);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        playlistHandler.post(connectionChecker);

    }


    @Override
    public void onStop() {
        super.onStop();
        playlistArray = null;
        currentlySetArray = null;
        playlistHandler.removeCallbacks(null);
    }

    static void setPlaylist(ArrayList<JSONObject> inputPlaylist){
        playlistArray = inputPlaylist;
    }

    static ArrayList<JSONObject> getPlaylistArray(){
        return playlistArray;
    }
}

