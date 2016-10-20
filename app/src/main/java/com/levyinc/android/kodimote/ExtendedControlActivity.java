package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class ExtendedControlActivity extends Fragment {


    View rootView;
    TextView connecting;
    RelativeLayout relativeBottom;
    ImageView extendedImage;
    ArrayList<String> extendedInfo;
    ArrayList<Bitmap> extendedInfoBitmaps;
    SharedPreferences sharedPreferences;
    private Handler extendedHandler = new Handler();


public Runnable extendedInfoChecker = new Runnable() {
    @Override
    public void run() {
        if (ButtonActions.getStatus()) {
            if (ButtonActions.extendedInfoGotten() && ButtonActions.playerInfoGotten()) {
                extendedInfo = ButtonActions.getExtendedInfoStrings();
                extendedInfoBitmaps = ButtonActions.getExtendedInfoBitmaps();
                if (extendedInfoBitmaps.toArray().length > 1 && extendedInfoBitmaps.get(1) != null) {
                    extendedImage.setVisibility(View.VISIBLE);
                    connecting.setVisibility(View.INVISIBLE);
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 1);
                    extendedHandler.postDelayed(extendedInfoChecker, 5000);

                } else if (extendedInfoBitmaps.toArray().length > 0 && extendedInfoBitmaps.get(0) != null) {
                    extendedImage.setVisibility(View.VISIBLE);
                    connecting.setHeight(0);
                    connecting.setWidth(0);
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 0);
                    extendedHandler.postDelayed(extendedInfoChecker, 5000);

                } else {
                    extendedHandler.postDelayed(extendedInfoChecker, 2000);
                }
            } else {
                extendedHandler.postDelayed(extendedInfoChecker, 2000);
            }
        } else {
            extendedImage.setVisibility(View.INVISIBLE);
            extendedHandler.postDelayed(connectionChecker, 2000);
        }
    }
};

    public class imageGetter extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... params) {
            return Bitmap.createScaledBitmap(extendedInfoBitmaps.get(params[2]), params[0] , params[1] , true);
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            extendedImage.setImageBitmap(bitmap);
        }
    }

    private Runnable connectionChecker = new Runnable() {
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y") || ButtonActions.getStatus()) {
                final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    connecting.setVisibility(View.VISIBLE);
                    connecting.setText("Connecting...");
                    if (ButtonActions.getStatus()) {
                        connecting.setText("Connected");
                        extendedHandler.postDelayed(extendedInfoChecker, 2000);

                    } else {
                        extendedHandler.postDelayed(connectionChecker, 1000);
                    }
                }
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.extended_controls, container, false);
        connecting = (TextView) rootView.findViewById(R.id.connecting_extended_controls);
        connecting.setText("No device connected");
        relativeBottom = (RelativeLayout) rootView.findViewById(R.id.relative_extended_bottom);
        extendedImage = (ImageView) rootView.findViewById(R.id.image_main_extended);
        relativeBottom.setVisibility(View.INVISIBLE);
        extendedImage.setVisibility(View.INVISIBLE);

        return rootView;
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      /*  Button subButton = (Button) rootView.findViewById(R.id.subtitle_button);
        subButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ButtonActions.getSubs();
            }
        });*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        extendedHandler.postDelayed(connectionChecker, 1000);
    }
}

