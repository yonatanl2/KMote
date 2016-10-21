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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class ExtendedControlActivity extends Fragment {


    View rootView;
    TextView connecting;
    ImageView extendedImage;
    ArrayList<Bitmap> extendedInfoBitmaps;
    SharedPreferences sharedPreferences;
    TextView contentInfo;
    TextView contentInfoNums;
    TextView subtitleText;
    Spinner subtitleSpinner;
    SeekBar seekBar;
    TextView currentProgress;
    TextView totalTime;
    int elaspedTime;
    private Handler extendedHandler = new Handler();


    public void visibilityChanger(boolean setVisible) {
        if (setVisible) {
            extendedImage.setVisibility(View.INVISIBLE);
            contentInfo.setVisibility(View.INVISIBLE);
            subtitleSpinner.setVisibility(View.INVISIBLE);
            subtitleText.setVisibility(View.INVISIBLE);
            contentInfoNums.setVisibility(View.INVISIBLE);
            currentProgress.setVisibility(View.INVISIBLE);
            totalTime.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
            connecting.setVisibility(View.VISIBLE);
        } else {
            contentInfo.setVisibility(View.VISIBLE);
            contentInfoNums.setVisibility(View.VISIBLE);
            subtitleSpinner.setVisibility(View.VISIBLE);
            subtitleText.setVisibility(View.VISIBLE);
            extendedImage.setVisibility(View.VISIBLE);
            currentProgress.setVisibility(View.VISIBLE);
            totalTime.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            connecting.setVisibility(View.INVISIBLE);
        }
    }


public Runnable extendedInfoChecker = new Runnable() {
    @Override
    public void run() {
        if (ButtonActions.getStatus()) {
            if (ButtonActions.extendedInfoGotten() && ButtonActions.playerInfoGotten()) {
                extendedHandler.post(progressSetter);
                extendedInfoBitmaps = ButtonActions.getExtendedInfoBitmaps();
                if (ButtonActions.extendedInfoGottenNums()) {
                    ArrayList<Integer> nums = ButtonActions.getVideoDetailsNums();
                    contentInfoNums.setText("Season: " + nums.get(0) + "\n" + "Episode: " + nums.get(1));
                }
                contentInfo.setText(ButtonActions.getExtendedInfoString());
                visibilityChanger(false);

                if (extendedInfoBitmaps.toArray().length > 1 && extendedInfoBitmaps.get(1) != null) {
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 1);
                    extendedHandler.postDelayed(extendedInfoChecker, 50000);

                } else {
                    extendedHandler.postDelayed(extendedInfoChecker, 5000);
                }
            } else {
                extendedHandler.postDelayed(extendedInfoChecker, 2000);
            }
        } else {
            visibilityChanger(true);
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

    private Runnable progressSetter = new Runnable() {

        @Override
        public void run() {
            int contentTime = ButtonActions.getContentTime().intValue();

            if (elaspedTime == ButtonActions.getElaspedTime().intValue() && !ButtonActions.isPaused()) {
                elaspedTime = elaspedTime + 1000;
            } else {
                elaspedTime = ButtonActions.getElaspedTime().intValue();
            }

            seekBar.setMax(contentTime);
            seekBar.setProgress(elaspedTime);

            if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                currentProgress.setText((TimeUnit.MILLISECONDS.toMinutes(elaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elaspedTime))) + ":" + (TimeUnit.MILLISECONDS.toSeconds(elaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elaspedTime))));
                totalTime.setText((TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + (TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));

            } else {
                currentProgress.setText(TimeUnit.MILLISECONDS.toHours(elaspedTime) + ":" + (TimeUnit.MILLISECONDS.toMinutes(elaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elaspedTime))) + ":" + (TimeUnit.MILLISECONDS.toSeconds(elaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elaspedTime))));
                totalTime.setText(TimeUnit.MILLISECONDS.toHours(contentTime) + ":" + (TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + (TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
            }

            extendedHandler.postDelayed(progressSetter, 950);
        }
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.extended_controls, container, false);
        connecting = (TextView) rootView.findViewById(R.id.connecting_extended_controls);
        extendedImage = (ImageView) rootView.findViewById(R.id.image_main_extended);
        contentInfo = (TextView) rootView.findViewById(R.id.content_info_text);
        contentInfoNums = (TextView) rootView.findViewById(R.id.content_info_nums);
        subtitleText = (TextView) rootView.findViewById(R.id.subtitle_text);
        subtitleSpinner = (Spinner) rootView.findViewById(R.id.subtitle_button);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        currentProgress = (TextView) rootView.findViewById(R.id.current_progress);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);


        visibilityChanger(true);

        connecting.setText("No device connected");

        return rootView;
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        extendedHandler.postDelayed(connectionChecker, 1000);



      /*  Button subButton = (Button) rootView.findViewById(R.id.subtitle_button);
        subButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ButtonActions.getSubs();
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        extendedHandler.postDelayed(connectionChecker, 1000);

    }

    @Override
    public void onPause() {
        super.onPause();
        extendedHandler.removeCallbacks(connectionChecker);
        extendedHandler.removeCallbacks(extendedInfoChecker);
    }

    @Override
    public void onStop() {
        super.onStop();
        extendedHandler.removeCallbacks(connectionChecker);
        extendedHandler.removeCallbacks(extendedInfoChecker);
    }
}

