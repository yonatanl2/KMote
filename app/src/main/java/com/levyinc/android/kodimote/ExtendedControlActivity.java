package com.levyinc.android.kodimote;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;


public class ExtendedControlActivity extends Fragment {


    View rootView;
    TextView connecting;
    ImageView extendedImage;
    SharedPreferences sharedPreferences;
    TextView contentInfo;
    TextView contentInfoNums;
    TextView subtitleText;
    ImageButton extendedActions;
    SeekBar seekBar;
    TextView currentProgress;
    TextView totalTime;
    LinearLayout playerActionsLayout;
    View separator;
    View separator2;
    TextView plotText;
    NetworkInfo activeNetwork;
    NestedScrollView nestedScrollView;
    ExpandableHeightGridView castGrid;
    CastAdapter castAdapter;
    TextView scoreText;
    Context mContext;

    private ArrayList<String> currentContent = null;
    static long elaspedTime;
    static long contentTime;
    /*private ProgressSetter progressSetter = new ProgressSetter();
    final Thread progressThread = new Thread(new Runnable() {
        @Override
        public void run() {
            progressSetter.run();
        }
    });*/
    //private ProgressHandler extendedHandler;
    private Handler extendedHandler = new Handler();
    private long currentElaspedTime;


    //TODO remove all progressRunners booleans
    private double setScore;
    private boolean visibility;
    private boolean successCast;
    Thread imageThread;


    private static ArrayList<ArrayList<String>> subtitleInfo = new ArrayList<>();
    private static ArrayList<ArrayList<String>> audioStreamInfo = new ArrayList<>();
    private static ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private static ArrayList<String> castArray = new ArrayList<>();
    private static ArrayList<Integer> videoDetailsNums = new ArrayList<>();
    private static boolean subtitleEnabled = false;
    private static String episodeName;
    private static String seriesName;
    private static String plot;
    private static double score;

    public void visibilityChanger(boolean setVisible) {
        if (setVisible) {
            extendedImage.setVisibility(View.INVISIBLE);
            contentInfo.setVisibility(View.INVISIBLE);
            extendedActions.setVisibility(View.INVISIBLE);
            subtitleText.setVisibility(View.INVISIBLE);
            contentInfoNums.setVisibility(View.INVISIBLE);
            currentProgress.setVisibility(View.INVISIBLE);
            totalTime.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
            playerActionsLayout.setVisibility(View.INVISIBLE);
            separator.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
            plotText.setVisibility(View.INVISIBLE);
            scoreText.setVisibility(View.INVISIBLE);
            castGrid.setVisibility(View.INVISIBLE);
            connecting.setVisibility(View.VISIBLE);
            visibility = true;

        } else {
            contentInfo.setVisibility(View.VISIBLE);
            contentInfoNums.setVisibility(View.VISIBLE);
            extendedActions.setVisibility(View.VISIBLE);
            subtitleText.setVisibility(View.VISIBLE);
            extendedImage.setVisibility(View.VISIBLE);
            currentProgress.setVisibility(View.VISIBLE);
            totalTime.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            playerActionsLayout.setVisibility(View.VISIBLE);
            separator.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.VISIBLE);
            plotText.setVisibility(View.VISIBLE);
            scoreText.setVisibility(View.VISIBLE);
            castGrid.setVisibility(View.VISIBLE);
            connecting.setVisibility(View.INVISIBLE);
            visibility = false;
        }
    }

    public void castGrid() {
        if (castArray != null) {
            if (mContext == null) {
                mContext = getContext();
            }
            if (mContext != null) {
                try {
                    if (castAdapter == null){
                        try {
                            castGrid.setExpanded(true);
                            castAdapter = new CastAdapter(getContext(), castArray, R.layout.grid_layout, nestedScrollView.getWidth());
                            castGrid.setAdapter(castAdapter);
                            successCast = true;
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else if (castArray != ((CastAdapter) castGrid.getAdapter()).getArrayList()) {
                        try {
                            castGrid.setExpanded(true);
                            castAdapter = new CastAdapter(getContext(), castArray, R.layout.grid_layout, nestedScrollView.getWidth());
                            castGrid.setAdapter(castAdapter);
                            successCast = true;
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                    castAdapter = new CastAdapter(getContext(), castArray, R.layout.grid_layout, nestedScrollView.getWidth());
                    successCast = true;
                } finally {
                    if (!successCast) {
                        castGrid.setExpanded(true);
                        castGrid.setAdapter(new CastAdapter(getContext(), castArray, R.layout.grid_layout, nestedScrollView.getWidth()));
                        successCast = true;
                    }
                }
            }
        }
    }

public Runnable extendedInfoChecker = new Runnable() {
    @Override
    public void run() {
        try {
            if (!Main2Activity.getWebSocketStatus()) {
                if (ButtonActions.getStatus()) {
                    if (ButtonActions.playerInfoGotten()) {
                        if (!videoDetailsNums.isEmpty()) {
                            contentInfoNums.setText("Season: " + videoDetailsNums.get(0) + "\n" + "Episode: " + videoDetailsNums.get(1));
                        }

                        ArrayList<String> tempArrayList = getExtendedInfoString();
                        if (tempArrayList.toArray().length > 1) {
                            contentInfo.setText(tempArrayList.get(0) + ": " + tempArrayList.get(1));
                        } else {
                            contentInfo.setText(tempArrayList.get(1));
                        }
                        if (visibility) {
                            visibilityChanger(false);
                        }

                        if (!progressSetter.isDaemon()) {
                            Log.i("Progress Log", "Thread Started");
                            progressSetter.setDaemon(true);
                            progressSetter.run();
                        }

                        if (ButtonActions.isPaused()) {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.play_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("play");
                        } else {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("pause");
                        }

                        if (!plotText.getText().toString().equals(plot)) {
                            plotText.setText(plot);
                        }
                        if (setScore != score) {
                            scoreText.setText("Score: " + score);
                            setScore = score;
                        }
                        if (bitmapArrayList.size() > 1 && bitmapArrayList.get(1) != null && currentContent == null) {
                            castGrid();
                            extendedHandler.postDelayed(extendedInfoChecker, 5000);
                            imageThread = new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 1, getActivity()));
                            imageThread.start();
                        } else if (bitmapArrayList.size() > 1 && bitmapArrayList.get(1) != null && !(currentContent.equals(getExtendedInfoString()))) {
                            castGrid();
                            imageThread = new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 1, getActivity()));
                            imageThread.start();
                            extendedHandler.postDelayed(extendedInfoChecker, 5000);
                        } else if (bitmapArrayList.size() > 1 && bitmapArrayList.get(0) != null && !(currentContent.equals(getExtendedInfoString()))) {
                            castGrid();
                            imageThread = new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 0, getActivity()));
                            imageThread.start();
                            extendedHandler.postDelayed(extendedInfoChecker, 5000);
                        } else {

                            extendedHandler.postDelayed(extendedInfoChecker, 2000);
                        }
                    } else {
                        visibilityChanger(true);
                        extendedHandler.postDelayed(extendedInfoChecker, 2500);
                    }
                } else {
                    visibilityChanger(true);
                    extendedHandler.postDelayed(connectionChecker, 3500);
                }
            } else {
                if (Main2Activity.webSocketEndpoint.playerInfoGotten()) {
                    if (!videoDetailsNums.isEmpty()) {
                        contentInfoNums.setText("Season: " + videoDetailsNums.get(0) + "\n" + "Episode: " + videoDetailsNums.get(1));
                    }

                    ArrayList<String> tempArrayList = getExtendedInfoString();
                    if (tempArrayList.toArray().length > 1) {
                        contentInfo.setText(tempArrayList.get(0) + ": " + tempArrayList.get(1));
                    } else {
                        contentInfo.setText(tempArrayList.get(1));
                    }
                    if (visibility) {
                        visibilityChanger(false);
                    }

                    if (!progressSetter.isAlive()) {
                        Log.i("Progress Log", "Thread Started");
                        progressSetter.setDaemon(true);
                        progressSetter.run();

                    }
                    if (Main2Activity.getWebSocketStatus()) {
                        if (Main2Activity.webSocketEndpoint.pauseStatus()) {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.play_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("play");
                        } else {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("pause");
                        }
                    } else {
                        if (ButtonActions.isPaused()) {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.play_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("play");
                        } else {
                            rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
                            rootView.findViewById(R.id.play_pause_button_extended).setTag("pause");
                        }
                    }
                    if (!plotText.getText().toString().equals(plot)) {
                        plotText.setText(plot);
                    }
                    if (setScore != score) {
                        scoreText.setText("Score: " + score);
                        setScore = score;
                    }

                    if (bitmapArrayList.size() > 1 && bitmapArrayList.get(1) != null && currentContent == null) {
                        castGrid();
                        extendedHandler.postDelayed(extendedInfoChecker, 2000);
                        new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 1, getActivity())).start();
                    } else if (bitmapArrayList.size() > 1 && bitmapArrayList.get(1) != null && !(currentContent.equals(getExtendedInfoString()))) {
                        castGrid();
                        new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 1, getActivity())).start();
                        extendedHandler.postDelayed(extendedInfoChecker, 2000);
                    } else if (bitmapArrayList.size() > 1 && bitmapArrayList.get(0) != null && !(currentContent.equals(getExtendedInfoString()))) {
                        castGrid();
                        new Thread(new ImageGetter(extendedImage.getWidth(), extendedImage.getHeight(), 0, getActivity())).start();
                        extendedHandler.postDelayed(extendedInfoChecker, 2000);
                    } else {
                        extendedHandler.postDelayed(extendedInfoChecker, 2000);
                    }
                } else {
                    visibilityChanger(true);
                    extendedHandler.postDelayed(extendedInfoChecker, 2500);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            if (exception.toString() != null) {
                extendedHandler.postDelayed(connectionChecker, 3500);
            }
        }
    }
};

    private class ImageGetter implements Runnable {

        int index;
        int width;
        int height;
        Activity activity;
        boolean success;

        ImageGetter (int width, int height, int index, Activity activity) {
            this.index = index;
            this.width = width;
            this.height = height;
            this.activity = activity;
        }
        @Override
        public void run() {
            currentContent = getExtendedInfoString();
            final Bitmap bitmap = Bitmap.createScaledBitmap(bitmapArrayList.get(index), width , height , true);
            Message msg = new Message();
            msg.obj = "bitmap";
            try {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imageView = (ImageView) activity.findViewById(R.id.image_main_extended);
                        imageView.setImageBitmap(bitmap);
                    }
                });
                success = true;
            } catch (NullPointerException e) {
                    e.printStackTrace();
            } finally {
                if (!success) {
                    currentContent = null;
                }
            }
        }
    }

    private Runnable connectionChecker = new Runnable() {
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y") || ButtonActions.getStatus()) {
                try {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (connecting.getVisibility() != View.VISIBLE) {
                            connecting.setVisibility(View.VISIBLE);
                        }
                        connecting.setText("Connecting...");
                        if (ButtonActions.getStatus() || Main2Activity.getWebSocketStatus()) {
                            connecting.setText("Connected");
                            extendedHandler.postDelayed(extendedInfoChecker, 2000);
                        } else {
                            visibilityChanger(true);
                            extendedHandler.postDelayed(connectionChecker, 1000);
                        }
                    } else {
                        visibilityChanger(true);
                        extendedHandler.postDelayed(connectionChecker, 1000);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    };
//TODO work again on the progress setter, might need to revamp
    private Thread progressSetter = new Thread(new Runnable() {
        @Override
        public void run() {
            DecimalFormat formatter = new DecimalFormat("00");
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault());

            System.out.println("Measuring progress: " + format.format(cal.getTime()));
            if (contentTime > 0) {
                seekBar.setMax((int) contentTime); 
                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                } else {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(contentTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                }
            }
            if (!(TimeUnit.SECONDS.toSeconds(currentElaspedTime) >= TimeUnit.SECONDS.toSeconds(contentTime))) {
                if (currentElaspedTime >= elaspedTime && currentElaspedTime <= (elaspedTime + 1200) && getPauseStatus()) {
                    Log.i("Progress Log", ("1:" + String.valueOf(currentElaspedTime) + '_' + String.valueOf(elaspedTime)));
                    currentElaspedTime += 1000;
                } else {
                    Log.i("Progress Log", ("2:" + String.valueOf(currentElaspedTime) + '_' + String.valueOf(elaspedTime)));
                    currentElaspedTime = elaspedTime;
                }
                seekBar.setProgress((int) currentElaspedTime);

                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                    currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(currentElaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(currentElaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime))));
                } else {
                    currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(currentElaspedTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(currentElaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(currentElaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime))));
                }

                if (currentElaspedTime > contentTime) {
                    currentElaspedTime = 0;
                    visibilityChanger(true);
                }
               /* if (Main2Activity.getWebSocketStatus()) {
                    if (Main2Activity.webSocketEndpoint.pauseStatus() || !Main2Activity.webSocketEndpoint.playerInfoGotten()) {
                        progressSetter.run();
                    }
                } else {
                    if (ButtonActions.isPaused() || !ButtonActions.playerInfoGotten()) {
                        progressSetter.run();
                    }
                }*/
            }
            extendedHandler.postDelayed(progressSetter, 1000);
        }
    });




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

        mContext = getContext();

        rootView = inflater.inflate(R.layout.extended_controls, container, false);
        connecting = (TextView) rootView.findViewById(R.id.connecting_extended_controls);
        extendedImage = (ImageView) rootView.findViewById(R.id.image_main_extended);
        contentInfo = (TextView) rootView.findViewById(R.id.content_info_text);
        contentInfoNums = (TextView) rootView.findViewById(R.id.content_info_nums);
        subtitleText = (TextView) rootView.findViewById(R.id.subtitle_text);
        extendedActions = (ImageButton) rootView.findViewById(R.id.subtitle_button);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        currentProgress = (TextView) rootView.findViewById(R.id.current_progress);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        playerActionsLayout = (LinearLayout) rootView.findViewById(R.id.player_actions_layout);
        separator = rootView.findViewById(R.id.extended_separator);
        separator2 = rootView.findViewById(R.id.extended_separator2);
        plotText = (TextView) rootView.findViewById(R.id.plot_text);
        scoreText = (TextView) rootView.findViewById(R.id.score_text);
        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.scrollView_extended);
        castGrid = (ExpandableHeightGridView) rootView.findViewById(R.id.cast_grid);
        castGrid.setFocusable(false);

        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);

        visibilityChanger(true);
        connecting.setText("No device connected");

        ImageButton playPause = (ImageButton) rootView.findViewById(R.id.play_pause_button_extended);
        ImageButton stop = (ImageButton) rootView.findViewById(R.id.stop_button_extended);
        ImageButton rollBack = (ImageButton) rootView.findViewById(R.id.roll_back_extended);
        ImageButton fastForward = (ImageButton) rootView.findViewById(R.id.roll_forward_extended);


        playPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (ButtonActions.isPaused() && !progressSetter.isDaemon()){
                    progressSetter.setDaemon(true);
                    progressSetter.run();
                }
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.startPlayPause();
                } else {
                    ButtonActions.playPause();
                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.startStop();
                } else {
                    ButtonActions.stop();
                }
            }
        });

        rollBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.startRollBack();
                } else {
                    ButtonActions.rollBack();
                }
            }
        });

        fastForward.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.startFastForward();
                } else {
                    ButtonActions.fastForward();
                }
            }
        });

        return rootView;
}


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //extendedHandler = new ProgressHandler(seekBar, currentProgress, totalTime, progressThread, progressSetter);
        //progressSetter.setProgressHandler(extendedHandler);
        extendedHandler.post(scalingThread);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    float progressPercentage = (progress * 100.0f) / contentTime;
                    ButtonActions.playerSeek(progressPercentage);
                    elaspedTime = progress;
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {


                float scrollYdouble = (float) scrollY;
                extendedImage.setAlpha(1.0f - (scrollYdouble/550));


            }
        });

        extendedActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.extended_menu, popupMenu.getMenu());

                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.remote_page_viewer);
                        switch (item.toString()){
                            case ("Subtitles"):
                                final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_view, setSubtitleInfoArray());
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                                builder.setTitle("Subtitles");
                                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (adapter.getItem(0).equals("Remove Subtitles")) {
                                            switch (which){
                                                case 0:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.startSubtitleAction(which -3);
                                                    } else {
                                                        ButtonActions.subtitleAction(which - 3);
                                                    }
                                                    break;
                                                case 1:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.sync("subtitledelay");
                                                    } else {
                                                        ButtonActions.sync("subtitledelay");
                                                    }
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                case 2:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.getSubs();
                                                        Main2Activity.setWsActive();

                                                    } else {
                                                        ButtonActions.getSubs();
                                                    }
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                default:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.startSubtitleAction(which -3);
                                                    } else {
                                                        ButtonActions.subtitleAction(which - 3);
                                                    }
                                                    break;
                                           }
                                        } else  {
                                            switch (which){
                                                case 0:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.getSubs();
                                                    } else {
                                                        ButtonActions.getSubs();
                                                    }
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                default:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.startSubtitleAction(which -1);
                                                    } else {
                                                        ButtonActions.subtitleAction(which - 1);
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                });
                                builder.show();
                                break;
                            case ("Audio Streams"):
                                final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), R.layout.list_view, getAudioStreamInfoArray());
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                                if (adapter2.getCount() > 0) {
                                    builder2.setTitle("Audio Streams");
                                    builder2.setAdapter(adapter2, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.sync("audiodelay");
                                                    } else {
                                                        ButtonActions.sync("audiodelay");
                                                    }
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                case 1:
                                                    if (Main2Activity.getWebSocketStatus()) {
                                                        Main2Activity.webSocketEndpoint.audiStreamAction(which -1);
                                                    } else {
                                                        ButtonActions.audiStreamAction(which - 1);
                                                    }
                                                    break;
                                            }
                                        }
                                    });
                                } else {
                                    builder2.setTitle("No Audio Streams Detected");
                                    builder2.setAdapter(adapter2, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            }
                                    });
                                }
                                builder2.show();
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        extendedHandler.postDelayed(connectionChecker, 1000);
    }


    @Override
    public void onPause() {
        super.onPause();
        extendedHandler.removeCallbacks(null);
        progressSetter.interrupt();
        progressSetter.setDaemon(false);
        elaspedTime = 0;
    }



    @Override
    public void onStop() {
        super.onStop();
        extendedHandler.removeCallbacks(null);
        progressSetter.interrupt();
        progressSetter.setDaemon(false);
        elaspedTime = 0;

    }

    private ArrayList<String> setSubtitleInfoArray() {
        ArrayList<String> tempList = new ArrayList<>();
        if (subtitleInfo.toArray().length > 0) {
            tempList.add("Remove Subtitles");
            tempList.add("Sync Subtitles");
        }
        tempList.add("Get Subtitles");
        for (int i = 0; i < subtitleInfo.toArray().length; i++){
            tempList.add(subtitleInfo.get(i).get(2) + " : " + subtitleInfo.get(i).get(1));
        }
        return tempList;
    }

    static void setSubtitleInfo(int index ,ArrayList<String> inputArray, boolean add) {
        if (add) {
            subtitleInfo.add(index, inputArray);
        } else {
            subtitleInfo.set(index, inputArray);
        }
    }

    static ArrayList getSubtitleInfo() {
        return subtitleInfo;
    }

    static void setSubtitleEnabled(boolean status) {
        subtitleEnabled = status;
    }

    static ArrayList getAudioStreamInfo() {
        return audioStreamInfo;
    }

    static void setAudioStreamInfo(int index, ArrayList<String> inputArray, boolean add) {
        if (add) {
            audioStreamInfo.add(index, inputArray);
        } else {
            audioStreamInfo.set(index, inputArray);
        }
    }

    static boolean getSubtitleEnabled() {
        return subtitleEnabled;
    }

    private ArrayList<String> getAudioStreamInfoArray() {
        ArrayList<String> tempList = new ArrayList<>();
        if (audioStreamInfo.toArray().length > 0) {
            tempList.add("Sync Audio Stream");
        }
        for (int i = 0; i < audioStreamInfo.toArray().length; i++){
            tempList.add(audioStreamInfo.get(i).get(2) + " : " + audioStreamInfo.get(i).get(1));
        }
        return tempList;
    }

    static void setEpisodeName(String name){
        episodeName = name;
    }

    static String getEpisodeName() {
        return episodeName;
    }

    static void setSeriesName(String name) {
        seriesName = name;
    }

    static String getSeriesName() {
        return seriesName;
    }

    private ArrayList<String> getExtendedInfoString(){
        ArrayList<String> tempArrayList = new ArrayList<>();
        tempArrayList.add(seriesName);
        tempArrayList.add(episodeName);
        return tempArrayList;
    }

    static void setPlot(String inputPlot) {
        plot = inputPlot;
    }

    static String getPlot() {
        return plot;
    }

    static double getScore() {
        return score;
    }

    static void setScore(double score) {
        ExtendedControlActivity.score = score;
    }

    static void setBitmapArrayListEmpty() {
        bitmapArrayList = new ArrayList<>();
    }

    static void addBitmapArrayListItem(Bitmap bitmap) {
        bitmapArrayList.add(bitmap);
    }

    static ArrayList<String> getCastArray(){
        return castArray;
    }

    static void setCastArray(int index, String cast, boolean add) {
        if (add) {
            castArray.add(index, cast);
        } else {
            castArray.set(index, cast);
        }
    }

    static void setVideoDetailsNums(int season, int episode) {
        videoDetailsNums = new ArrayList<>();
        videoDetailsNums.add(season);
        videoDetailsNums.add(episode);
    }

    static long getElaspedTime(){
        return elaspedTime;
    }

    static long getContentTime(){
        return contentTime;
    }

    boolean getPauseStatus(){
        if (Main2Activity.getWebSocketStatus()){
            return !Main2Activity.webSocketEndpoint.pauseStatus();
        } else {
            return !ButtonActions.isPaused();
        }
    }
}

