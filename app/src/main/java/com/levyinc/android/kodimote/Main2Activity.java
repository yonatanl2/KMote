package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Main2Activity extends Fragment {
    View myView;
    private Handler remoteHandler = new Handler();
    boolean upLongPressed = false;
    boolean downLongPressed = false;
    boolean leftLongPressed = false;
    boolean rightLongPressed = false;
    boolean paused = false;

    LinearLayout videoLayout;
    static TextView connecting;
    SharedPreferences sharedPreferences;
    ImageButton playPause;
    ImageButton select;
    ImageButton buttonDown;
    ImageButton buttonUp;
    ImageButton buttonLeft;
    ImageButton buttonRight;
    ImageButton stop;
    ImageButton rollBack;
    ImageButton fastForward;
    static boolean connected = false;


    Thread scalingThread = new Thread(new Runnable() {
        @Override
        public void run() {


            select.setBackgroundResource(R.drawable.centerround);
            buttonUp.setBackgroundResource(R.drawable.arrowup);
            buttonDown.setBackgroundResource(R.drawable.arrowdown);
            buttonLeft.setBackgroundResource(R.drawable.arrowleft);
            buttonRight.setBackgroundResource(R.drawable.arrowright);
            stop.setBackgroundResource(R.drawable.stop_button);
            playPause.setBackgroundResource(R.drawable.pause_button);
            rollBack.setBackgroundResource(R.drawable.roll_back);
            fastForward.setBackgroundResource(R.drawable.roll_forward);

        }
    });


    private Thread connectionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y")) {
                final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    connecting.setVisibility(View.VISIBLE);
                    ButtonActions.connect(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", ""));
                }
            }
        }
    });

    public static void statusUpdater(boolean connection) {
        if (connection) {
            connected = true;
            connecting.setText("Connected");
        }
    }


    private Thread playCheck = new Thread(new Runnable() {
            @Override
            public void run () {
                ButtonActions.getInfo();
                if (ButtonActions.playerInfoGotten()) {
                    if (connected) {
                        videoLayout.setVisibility(View.VISIBLE);
                        connecting.setVisibility(View.INVISIBLE);
                        if (ButtonActions.isPaused() && playPause.getTag() == "pause") {
                            playPause.setBackgroundResource(R.drawable.play_button);
                            playPause.setTag("play");
                            paused = true;
                        } else if (!ButtonActions.isPaused() && playPause.getTag() == "play") {
                            playPause.setBackgroundResource(R.drawable.pause_button);
                            playPause.setTag("pause");
                            paused = false;
                        }
                    }
                } else {
                    videoLayout.setVisibility(View.INVISIBLE);
                    connecting.setVisibility(View.VISIBLE);
                }
                remoteHandler.postDelayed(playCheck, 2555);
            }
        });


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.remote_activity, container, false);
        connecting = (TextView) myView.findViewById(R.id.connect_message);
        playPause = (ImageButton) myView.findViewById(R.id.play_pause_button);
        playPause.setTag("pause");

        select = (ImageButton) myView.findViewById(R.id.select);
        buttonDown = (ImageButton) myView.findViewById(R.id.arrow_button_4);
        buttonUp = (ImageButton) myView.findViewById(R.id.arrow_button_2);
        buttonLeft = (ImageButton) myView.findViewById(R.id.arrow_button_1);
        buttonRight = (ImageButton) myView.findViewById(R.id.arrow_button_3);
        stop = (ImageButton) myView.findViewById(R.id.stop_button);
        rollBack = (ImageButton) myView.findViewById(R.id.roll_back);
        fastForward = (ImageButton) myView.findViewById(R.id.roll_forward);
        videoLayout = (LinearLayout) myView.findViewById(R.id.layout1);
        videoLayout.setVisibility(View.INVISIBLE);
        connecting.setVisibility(View.INVISIBLE);
        System.out.println(select.getWidth());
        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        remoteHandler.post(scalingThread);
        remoteHandler.postDelayed(connectionThread, 100);
        remoteHandler.postDelayed(playCheck, 500);

        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);


        Button back = (Button) myView.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.back();
            }
        });

        select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.select();

            }
        });

        class RepetitiveActions implements Runnable {

            @Override
            public void run() {
                if (upLongPressed) {
                    ButtonActions.up();
                    remoteHandler.postDelayed(new RepetitiveActions(), 120);
                } else if (downLongPressed) {
                    ButtonActions.down();
                    remoteHandler.postDelayed(new RepetitiveActions(), 120);
                }
            }
        }

        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonActions.up();
            }
        });

        buttonUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                upLongPressed = true;
                remoteHandler.post(new RepetitiveActions());
                return true;
            }
        });
        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && upLongPressed) {
                    upLongPressed = false;
                }
                return false;
            }
        });

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonActions.down();
            }
        });
        buttonDown.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                downLongPressed = true;
                remoteHandler.post(new RepetitiveActions());
                return false;
            }
        });
        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && downLongPressed) {
                    downLongPressed = false;
                }
                return false;
            }
        });

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonActions.left();
            }
        });
        buttonLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                leftLongPressed = true;
                remoteHandler.post(new RepetitiveActions());
                return false;
            }
        });
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && leftLongPressed) {
                    leftLongPressed = false;
                }
                return false;
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonActions.right();
            }
        });
        buttonRight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                rightLongPressed = true;
                remoteHandler.post(new RepetitiveActions());
                return false;
            }
        });
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && rightLongPressed) {
                    rightLongPressed = false;
                }
                return false;
            }
        });


        playPause = (ImageButton) myView.findViewById(R.id.play_pause_button);
        playPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ButtonActions.playPause();
                paused = !paused;
                if (paused) {
                    playPause.setBackgroundResource(R.drawable.play_button);
                    playPause.setTag("play");
                } else {
                    playPause.setBackgroundResource(R.drawable.pause_button);
                    playPause.setTag("pause");

                }

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

    }


    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences.getString("successful_connection", "").equals("y")) {
            remoteHandler.postDelayed(connectionThread, 50);
            remoteHandler.postDelayed(playCheck, 100);
        }
    }
    

    @Override
    public void onPause() {
        super.onPause();
        ButtonActions.disconnect();
        remoteHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ButtonActions.disconnect();
        remoteHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        ButtonActions.disconnect();
        remoteHandler.removeCallbacksAndMessages(null);
    }
}

