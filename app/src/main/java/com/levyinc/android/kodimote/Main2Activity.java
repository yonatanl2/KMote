package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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


public class Main2Activity extends Fragment {
    View myView;
    private Handler remoteHandler = new Handler();
    boolean upLongPressed = false;
    boolean downLongPressed = false;
    boolean leftLongPressed = false;
    boolean rightLongPressed = false;
    boolean paused = false;
    static boolean gotInfo = false;

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
            if (ButtonActions.getStatus()) {
                connecting.setText("Connected");
            } else {
                remoteHandler.postDelayed(connectionThread, 1000);
            }
        }
    };


    private Thread connectionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y")) {
                final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                try {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        connecting.setVisibility(View.VISIBLE);
                        ButtonActions.connect(sharedPreferences.getString("input_ip", ""), sharedPreferences.getString("input_port", ""));
                        if (ButtonActions.getStatus()) {
                            connecting.setText("Connected");
                        } else {
                            remoteHandler.postDelayed(statusChecker, 1000);
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    });



    private Thread playCheck = new Thread(new Runnable() {
            @Override
            public void run () {
                if (ButtonActions.playerInfoGotten()) {
                        videoLayout.setVisibility(View.VISIBLE);
                        connecting.setVisibility(View.INVISIBLE);
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
            ButtonActions.getInfo();
            remoteHandler.postDelayed(infoChecker, 300);
        }
    };




    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.remote_activity, container, false);
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

        playPause.setTag("pause");

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.muteButton();
            }
        });

        ImageButton getInfoButton = (ImageButton) myView.findViewById(R.id.get_info_button);
        getInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.getInfoActino();
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

        remoteHandler.post(scalingThread);
        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);
        if (sharedPreferences.getString("successful_connection", "").equals("y")) {
            remoteHandler.postDelayed(connectionThread, 100);
            remoteHandler.postDelayed(intialHandlerSetter, 300);
            remoteHandler.postDelayed(playCheck, 500);

        }

        setRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.setIsRepeat();
            }
        });

        setShuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.setIsShuffled();
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
                        ButtonActions.setText(textToSend.getText().toString());
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
                            ButtonActions.volumePercentageSetter(progress);
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
                    remoteHandler.postDelayed(new RepetitiveActions(), 50);
                } else if (downLongPressed) {
                    ButtonActions.down();
                    remoteHandler.postDelayed(new RepetitiveActions(), 50);
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
                ButtonActions.down();
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
                ButtonActions.left();
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
                ButtonActions.right();
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
                ButtonActions.playPause();
                paused = !paused;
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonActions.homeButton();
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
        playPause.setTag("pause");
        if (sharedPreferences.getString("successful_connection", "").equals("y")) {
            remoteHandler.post(scalingThread);
            remoteHandler.postDelayed(connectionThread, 100);
            remoteHandler.postDelayed(intialHandlerSetter, 300);
            remoteHandler.postDelayed(playCheck, 500);

        }
    }
    

    @Override
    public void onPause() {
        super.onPause();
        ButtonActions.disconnect();
        ButtonActions.buttonActionsHandler.removeCallbacksAndMessages(null);
        remoteHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ButtonActions.disconnect();
        ButtonActions.buttonActionsHandler.removeCallbacksAndMessages(null);
        remoteHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        ButtonActions.disconnect();
        ButtonActions.buttonActionsHandler.removeCallbacksAndMessages(null);
        remoteHandler.removeCallbacksAndMessages(null);
    }
}

