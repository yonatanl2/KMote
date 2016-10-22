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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
    LinearLayout playerActionsLayout;
    private Boolean imageSet = false;
    int elaspedTime;
    int contentTime;
    private Handler extendedHandler = new Handler();
    private Lock lock = new ReentrantLock();


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
            playerActionsLayout.setVisibility(View.INVISIBLE);
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
            playerActionsLayout.setVisibility(View.VISIBLE);
            connecting.setVisibility(View.INVISIBLE);
        }
    }


public Runnable extendedInfoChecker = new Runnable() {
    @Override
    public void run() {
        if (ButtonActions.getStatus()) {
            System.out.println("This is running");
            if (ButtonActions.extendedInfoGotten() && ButtonActions.playerInfoGotten()) {

                extendedInfoBitmaps = ButtonActions.getExtendedInfoBitmaps();
                if (ButtonActions.extendedInfoGottenNums()) {
                    ArrayList<Integer> nums = ButtonActions.getVideoDetailsNums();
                    contentInfoNums.setText("Season: " + nums.get(0) + "\n" + "Episode: " + nums.get(1));
                }
                contentInfo.setText(ButtonActions.getExtendedInfoString());
                visibilityChanger(false);
                if (extendedInfoBitmaps.toArray().length > 1 && extendedInfoBitmaps.get(1) != null && !imageSet) {
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 1);
                    extendedHandler.postDelayed(extendedInfoChecker, 5000);
                } else {
                    extendedHandler.postDelayed(extendedInfoChecker, 5000);}

                if (elaspedTime == 0) {
                    extendedHandler.post(progressSetter);}


                if (ButtonActions.isPaused() && rootView.findViewById(R.id.play_pause_button_extended).getTag() != "play") {
                    rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.play_button);
                    rootView.findViewById(R.id.play_pause_button_extended).setTag("play");

                } else if (!(ButtonActions.isPaused()) && rootView.findViewById(R.id.play_pause_button_extended).getTag() != "pause") {
                    rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
                    rootView.findViewById(R.id.play_pause_button_extended).setTag("pause");
                }
            } else {
                visibilityChanger(true);
                extendedHandler.postDelayed(extendedInfoChecker, 2000);}
        } else {
            visibilityChanger(true);
            extendedHandler.postDelayed(connectionChecker, 2000);}
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
            imageSet = true;
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
                } else {
                    extendedHandler.postDelayed(connectionChecker, 1000);
                }
            }
        }
    };

    private Runnable progressSetter = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            DecimalFormat formatter = new DecimalFormat("00");
            if (contentTime != ButtonActions.getContentTime().intValue()) {
                contentTime = ButtonActions.getContentTime().intValue();
                seekBar.setMax(contentTime);
                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0){
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                } else {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(contentTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                }
            }
            if (!(TimeUnit.SECONDS.toSeconds(elaspedTime) >= TimeUnit.SECONDS.toSeconds(contentTime))) {
                if (TimeUnit.SECONDS.toSeconds(elaspedTime) >= TimeUnit.SECONDS.toSeconds(ButtonActions.getElaspedTime().intValue()) && !ButtonActions.isPaused()) {
                    elaspedTime = elaspedTime + 1000;
                } else {
                    elaspedTime = ButtonActions.getElaspedTime().intValue();
                }

                seekBar.setProgress(elaspedTime);

                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                    currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(elaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(elaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elaspedTime))));
                } else {
                    currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(elaspedTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(elaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(elaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elaspedTime))));
                }
                lock.unlock();
                extendedHandler.postDelayed(progressSetter, 1000);

            }
        }
    };


    Thread scalingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            rootView.findViewById(R.id.stop_button_extended).setBackgroundResource(R.drawable.stop_button);
            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
            rootView.findViewById(R.id.roll_back_extended).setBackgroundResource(R.drawable.roll_back);
            rootView.findViewById(R.id.roll_forward_extended).setBackgroundResource(R.drawable.roll_forward);
        }
    });

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
        playerActionsLayout = (LinearLayout) rootView.findViewById(R.id.player_actions_layout);

        extendedHandler.post(scalingThread);
        visibilityChanger(true);
        connecting.setText("No device connected");

        ImageButton playPause = (ImageButton) rootView.findViewById(R.id.play_pause_button_extended);
        ImageButton stop = (ImageButton) rootView.findViewById(R.id.stop_button_extended);
        ImageButton rollBack = (ImageButton) rootView.findViewById(R.id.roll_back_extended);
        ImageButton fastForward = (ImageButton) rootView.findViewById(R.id.roll_forward_extended);


        playPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.playPause();
            }
        });


        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.stop();
            }
        });

        rollBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ButtonActions.rollBack();
            }
        });

        fastForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.fastForward();
            }
        });


        return rootView;
}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        extendedHandler.postDelayed(connectionChecker, 1000);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    float progressPercentage = (progress * 100.0f) / contentTime;
                    ButtonActions.playerSeek(progressPercentage);
                    elaspedTime = 1;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


       /* subtitleSpinner.seto(new View.OnClickListener(){
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

