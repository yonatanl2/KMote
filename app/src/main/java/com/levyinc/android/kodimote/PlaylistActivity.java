package com.levyinc.android.kodimote;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;


public class PlaylistActivity extends Fragment {

private static ArrayList<JSONObject> playlistArray;

    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.playlist_layout, container, false);

        return rootView;
    }

    // TODO http://10.0.0.14:8080/jsonrpc?request={ "jsonrpc": "2.0", "method": "Playlist.GetItems", "params":{"properties":["title","album","artist","duration"],"playlistid":1},"id":1}
    //

    static void setPlaylist(ArrayList<JSONObject> inputPlaylist){
        playlistArray = inputPlaylist;
    }

    static ArrayList<JSONObject> getPlaylistArray(){
        return playlistArray;
    }
}

