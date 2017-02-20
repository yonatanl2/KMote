package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;


public class Main2Activity extends Fragment {
    View myView;
    private Handler remoteHandler = new Handler();
    boolean upLongPressed = false;
    boolean downLongPressed = false;
    boolean leftLongPressed = false;
    boolean rightLongPressed = false;
    boolean paused = false;
    static private boolean wsActive;

    Lock lock = new ReentrantLock();

    static WebSocketEndpoint webSocketEndpoint;
    LinearLayout videoLayout;
    TextView connecting;
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
    ImageButton homeButton;
    ImageView upArrowDrawer;
    ImageView downArrowDrawer;
    ImageButton setShuffleButton;
    ImageButton setRepeatButton;
    RelativeLayout additionalButtonsLayout;
    RelativeLayout remoteLayout;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageButton volumeSeek;
    ImageButton popKeyBoard;


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

    private Runnable statusChecker = new Runnable() {
        public void run() {
            if (!sharedPreferences.getString("WS", "").equals("y")) {
                if (ButtonActions.getStatus()) {
                    connecting.setText("Connected");
                } else {
                    remoteHandler.postDelayed(connectionThread, 1000);
                }
            } else {
                if (wsActive) {
                    connecting.setText("Connected");
                } else {
                    remoteHandler.postDelayed(connectionThread, 1000);
                }
            }
        }
    };


    private Thread connectionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            lock.lock();
            if (sharedPreferences.getString("successful_connection", "").equals("y")) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        connecting.setVisibility(View.VISIBLE);
                        if (!sharedPreferences.getString("WS", "").equals("y")) {
                            wsActive = false;
                            ButtonActions.connect(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", ""));
                            remoteHandler.postDelayed(intialHandlerSetter, 300);
                            if (ButtonActions.getStatus()) {
                                connecting.setText("Connected");
                            } else {
                                if (connecting.getText() == "Connected"){
                                    connecting.setText("Connecting...");
                                }
                                remoteHandler.postDelayed(statusChecker, 1000);
                            }
                        } else {
                            ButtonActions.connect(sharedPreferences.getString("input_ip", ""), "8080");
                            if (wsActive) {
                                connecting.setText("Connected");
                            } else {
                                boolean success = false;
                                try {
                                    if (webSocketEndpoint.getOnOpenMessage() != null) {
                                        wsActive = true;
                                        connecting.setText("Connected");
                                        remoteHandler.postDelayed(intialHandlerSetter, 300);
                                        success = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!success) {
                                    webSocketEndpoint = new WebSocketEndpoint(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", ""));
                                    if (connecting.getText() == "Connected"){
                                        connecting.setText("Connecting...");
                                    }
                                    remoteHandler.postDelayed(statusChecker, 3000);
                                }
                            }
                        }
                    } else {
                        remoteHandler.postDelayed(connectionThread, 3500);
                        connecting.setText("Wifi not detected");
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            lock.unlock();
        }
    });

    private Thread playCheck = new Thread(new Runnable() {
            @Override
            public void run () {
                if (wsActive) {
                    if (webSocketEndpoint.playerInfoGotten()) {
                        if (videoLayout.getVisibility() != View.VISIBLE || connecting.getVisibility() != View.INVISIBLE) {
                            videoLayout.setVisibility(View.VISIBLE);
                            connecting.setVisibility(View.INVISIBLE);
                        }
                        if (webSocketEndpoint.pauseStatus() && playPause.getTag() != "play") {
                            playPause.setBackgroundResource(R.drawable.play_button);
                            playPause.setTag("play");
                            paused = true;
                        } else if (!(webSocketEndpoint.pauseStatus()) && playPause.getTag() != "pause") {
                            playPause.setBackgroundResource(R.drawable.pause_button);
                            playPause.setTag("pause");
                            paused = false;
                        }
                    } else {
                        videoLayout.setVisibility(View.INVISIBLE);
                        connecting.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (ButtonActions.playerInfoGotten()) {
                        if (videoLayout.getVisibility() != View.VISIBLE || connecting.getVisibility() != View.INVISIBLE) {
                            videoLayout.setVisibility(View.VISIBLE);
                            connecting.setVisibility(View.INVISIBLE);
                        }
                        if (ButtonActions.isPaused() && playPause.getTag() != "play") {
                            playPause.setBackgroundResource(R.drawable.play_button);
                            playPause.setTag("play");
                            paused = true;
                        } else if (!(ButtonActions.isPaused()) && playPause.getTag() != "pause") {
                            playPause.setBackgroundResource(R.drawable.pause_button);
                            playPause.setTag("pause");
                            paused = false;
                        }
                    } else {
                        videoLayout.setVisibility(View.INVISIBLE);
                        connecting.setVisibility(View.VISIBLE);
                    }
                    remoteHandler.postDelayed(playCheck, 4000);
                }
            }
        });

    private Runnable infoChecker = new Runnable() {
        @Override
        public void run() {
            if (ButtonActions.playerInfoGotten()) {
                remoteHandler.postDelayed(playCheck, 500);
            }
            remoteHandler.postDelayed(infoChecker, 2500);
        }
    };

    private Runnable intialHandlerSetter = new Runnable() {
        @Override
        public void run() {
            if (wsActive) {
                webSocketEndpoint.getInfo();
                remoteHandler.postDelayed(intialHandlerSetter, 1000);
                if (webSocketEndpoint.playerInfoGotten()) {
                    remoteHandler.postDelayed(playCheck, 500);
                }
            } else {
                ButtonActions.getInfo();
                remoteHandler.postDelayed(infoChecker, 300);
            }
        }
    };




    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.remote_activity, container, false);
        remoteHandler.post(scalingThread);
        connecting = (TextView) myView.findViewById(R.id.connect_message);
        playPause = (ImageButton) myView.findViewById(R.id.play_pause_button);
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

        ImageButton muteButton = (ImageButton) myView.findViewById(R.id.mute_button);
        ImageButton getInfoButton = (ImageButton) myView.findViewById(R.id.get_info_button);
        homeButton = (ImageButton) myView.findViewById(R.id.home_button);
        upArrowDrawer = (ImageView) myView.findViewById(R.id.drawer_arrow);
        downArrowDrawer = (ImageView) myView.findViewById(R.id.drawer_arrow_down);
        additionalButtonsLayout = (RelativeLayout) myView.findViewById(R.id.additional_buttons_layout);
        slidingUpPanelLayout = (SlidingUpPanelLayout) myView.findViewById(R.id.sliding_up_pane);
        remoteLayout = (RelativeLayout) myView.findViewById(R.id.remote_activity);
        volumeSeek = (ImageButton) myView.findViewById(R.id.set_volume);
        setRepeatButton = (ImageButton) myView.findViewById(R.id.set_repeat);
        setShuffleButton = (ImageButton) myView.findViewById(R.id.set_shuffle);
        popKeyBoard = (ImageButton) myView.findViewById(R.id.pop_keyboard);

        downArrowDrawer.setVisibility(View.INVISIBLE);
        playPause.setTag("");

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wsActive) {
                    ButtonActions.muteButton();
                } else {
                    webSocketEndpoint.muteButton();
                }
            }
        });

        getInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wsActive) {
                    ButtonActions.getInfoActino();
                } else {
                    webSocketEndpoint.getInfoAction();
                }
            }
        });

        myView.setFocusableInTouchMode(true);
        myView.requestFocus();
        myView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    return true;
                }
                return false;
            }
        });
        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);

        setRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wsActive) {
                    if (ButtonActions.isIsRepeat() && ButtonActions.playerInfoGotten()){
                        Snackbar.make(myView, "Playlist Repeat Disabled", Snackbar.LENGTH_SHORT).show();
                    } else if (ButtonActions.playerInfoGotten()) {
                        Snackbar.make(myView, "Playlist Repeat Enabled", Snackbar.LENGTH_SHORT).show();
                    }
                    ButtonActions.setIsRepeat();
                } else {
                    if (webSocketEndpoint.isIsRepeat() && webSocketEndpoint.playerInfoGotten()){
                        Snackbar.make(myView, "Playlist Shuffle Disabled", Snackbar.LENGTH_SHORT).show();
                    } else if (webSocketEndpoint.playerInfoGotten()) {
                        Snackbar.make(myView, "Playlist Shuffle Enabled", Snackbar.LENGTH_SHORT).show();
                    }
                    webSocketEndpoint.setIsRepeat();
                }
            }
        });

        setShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wsActive) {
                    if (ButtonActions.isIsShuffled() && ButtonActions.playerInfoGotten()){
                        Snackbar.make(myView, "Playlist Shuffle Disabled", Snackbar.LENGTH_SHORT).show();
                    } else if (ButtonActions.playerInfoGotten()) {
                        Snackbar.make(myView, "Playlist Shuffle Enabled", Snackbar.LENGTH_SHORT).show();
                    }
                    ButtonActions.setIsShuffled();
                } else {
                    if (webSocketEndpoint.isIsShuffled() && webSocketEndpoint.playerInfoGotten()){
                        Snackbar.make(myView, "Playlist Shuffle Disabled", Snackbar.LENGTH_SHORT).show();
                    } else if (webSocketEndpoint.playerInfoGotten()) {
                        Snackbar.make(myView, "Playlist Shuffle Enabled", Snackbar.LENGTH_SHORT).show();
                    }
                    webSocketEndpoint.setIsShuffled();
                }
            }
        });

        popKeyBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.sendtext_layout, (ViewGroup) myView.findViewById(R.id.send_text_layout));
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                builder.setView(layout);
                final AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();

                Button sendButton = (Button) layout.findViewById(R.id.send_button);
                final EditText textToSend = (EditText) layout.findViewById(R.id.text_to_send);
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!wsActive) {
                            ButtonActions.setText(textToSend.getText().toString());
                        } else {
                            webSocketEndpoint.setText(textToSend.getText().toString());
                        }
                        alertDialog.dismiss();
                    }
                });
            }
        });

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                additionalButtonsLayout.setAlpha(slideOffset);
                if (slideOffset == 1.0) {
                    downArrowDrawer.setVisibility(View.VISIBLE);
                    upArrowDrawer.setVisibility(View.INVISIBLE);
                } else if (slideOffset == 0) {
                    upArrowDrawer.setVisibility(View.VISIBLE);
                    downArrowDrawer.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        remoteLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return true;
            }
        });


        volumeSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.seekbar_layout, (ViewGroup) myView.findViewById(R.id.seek_bar_layout));
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                builder.setTitle("Set Volume");
                builder.setView(layout);
                builder.create();
                builder.show();
                SeekBar volumeBar = (SeekBar) layout.findViewById(R.id.volume_seek_bar);
                volumeBar.setMax(100);
                volumeBar.setProgress(100);
                volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            if (!wsActive) {
                                ButtonActions.volumePercentageSetter(progress);
                            } else {
                                webSocketEndpoint.volumePercentageSetter(progress);
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }
        });

        Button back = (Button) myView.findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!wsActive) {
                    ButtonActions.back();
                } else {
                    webSocketEndpoint.back();
                }
            }
        });

        select.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!wsActive) {
                    ButtonActions.select();
                } else {
                    webSocketEndpoint.select();
                }

            }
        });

        class RepetitiveActions implements Runnable {

            @Override
            public void run() {
                if (upLongPressed) {
                    if (!wsActive) {
                        ButtonActions.up();
                    } else {
                        webSocketEndpoint.up();
                    }
                    remoteHandler.postDelayed(new RepetitiveActions(), 50);
                } else if (downLongPressed) {
                    if (!wsActive) {
                        ButtonActions.down();
                    } else {
                        webSocketEndpoint.down();
                    }
                    remoteHandler.postDelayed(new RepetitiveActions(), 50);
                }
            }
        }

        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!wsActive) {
                    ButtonActions.up();
                } else {
                    webSocketEndpoint.up();
                }
            }
        });

        buttonUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                upLongPressed = true;
                remoteHandler.postDelayed(new RepetitiveActions(), 120);
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
                if (!wsActive) {
                    ButtonActions.down();
                } else {
                    webSocketEndpoint.down();
                }
            }
        });
        buttonDown.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                downLongPressed = true;
                remoteHandler.postDelayed(new RepetitiveActions(), 120);
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
                if (!wsActive) {
                    ButtonActions.left();
                } else {
                    webSocketEndpoint.left();
                }
            }
        });
        buttonLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                leftLongPressed = true;
                remoteHandler.postDelayed(new RepetitiveActions(), 120);
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
                if (!wsActive) {
                    ButtonActions.right();
                } else {
                    webSocketEndpoint.right();
                }
            }
        });
        buttonRight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                rightLongPressed = true;
                remoteHandler.postDelayed(new RepetitiveActions(), 120);
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
                if (!wsActive) {
                    ButtonActions.playPause();
                    paused = !paused;
                } else {
                    webSocketEndpoint.startPlayPause();
                    paused = !paused;
                }
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wsActive) {
                    ButtonActions.homeButton();
                } else {
                    webSocketEndpoint.home();
                }
            }
        });

        select.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!wsActive) {
                    ButtonActions.pressContextMenu();
                } else {
                    webSocketEndpoint.pressContextMenu();
                }
                return true;
            }
        });


        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!wsActive) {
                    ButtonActions.stop();
                } else {
                    webSocketEndpoint.startStop();
                }
            }
        });

        rollBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (!wsActive) {
                    ButtonActions.rollBack();
                } else {
                    webSocketEndpoint.startRollBack();
                }
            }
        });

        fastForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!wsActive) {
                    ButtonActions.fastForward();
                } else {
                    webSocketEndpoint.startFastForward();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        playPause.setTag("");
        wsActive = false;
        if (sharedPreferences.getString("successful_connection", "").equals("y")) {
            remoteHandler.postDelayed(connectionThread, 100);
            remoteHandler.postDelayed(playCheck, 500);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wsActive) {
            webSocketEndpoint.disconnect();
            wsActive = !wsActive;
        }
        remoteHandler.removeCallbacksAndMessages(null);
        ButtonActions.stopAsynchTask();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (wsActive){
            webSocketEndpoint.disconnect();
            wsActive = !wsActive;

        }
        remoteHandler.removeCallbacksAndMessages(null);
        ButtonActions.stopAsynchTask();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (wsActive){
            webSocketEndpoint.disconnect();
            wsActive = !wsActive;
        }
        remoteHandler.removeCallbacksAndMessages(null);
        ButtonActions.stopAsynchTask();

    }

    static boolean getWebSocketStatus() {
        return wsActive;
    }

    static void setWsActive() {
        wsActive = false;
    }

    static void setWsInactive(){
        wsActive = true;
    }
}

