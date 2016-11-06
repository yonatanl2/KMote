package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
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
    ImageButton extendedActions;
    SeekBar seekBar;
    TextView currentProgress;
    TextView totalTime;
    LinearLayout playerActionsLayout;
    View separator;
    TextView plotText;
    NetworkInfo activeNetwork;
    NestedScrollView nestedScrollView;


    private ArrayList<String> currentContent = null;
    int elaspedTime;
    int contentTime;
    private Handler extendedHandler = new Handler();
    private Lock lock = new ReentrantLock();


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
            plotText.setVisibility(View.INVISIBLE);
            connecting.setVisibility(View.VISIBLE);
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
            plotText.setVisibility(View.VISIBLE);
            connecting.setVisibility(View.INVISIBLE);
        }
    }


public Runnable extendedInfoChecker = new Runnable() {
    @Override
    public void run() {
        if (ButtonActions.getStatus()) {
            if (ButtonActions.playerInfoGotten()) {
                extendedInfoBitmaps = new ArrayList<>();
                extendedInfoBitmaps = ButtonActions.getExtendedInfoBitmaps();
                if (ButtonActions.extendedInfoGottenNums()) {
                    ArrayList<Integer> nums = ButtonActions.getVideoDetailsNums();
                    contentInfoNums.setText("Season: " + nums.get(0) + "\n" + "Episode: " + nums.get(1));
                }


                ArrayList<String> tempArrayList = ButtonActions.getExtendedInfoString();
                if (tempArrayList.toArray().length > 1) {
                    contentInfo.setText(tempArrayList.get(0)+ ": " + tempArrayList.get(1));
                } else {
                    contentInfo.setText(tempArrayList.get(1));
                }

                visibilityChanger(false);
                if (extendedInfoBitmaps.toArray().length > 2) {
                    System.out.println("Bigger than 2!!!!!!" + extendedInfoBitmaps);
                }
                else if (extendedInfoBitmaps.toArray().length > 1 && extendedInfoBitmaps.get(1) != null && currentContent != ButtonActions.getExtendedInfoString()) {
                    System.out.println(extendedInfoBitmaps);
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 1);
                    extendedHandler.postDelayed(extendedInfoChecker, 2000);
                } else if (extendedInfoBitmaps.toArray().length > 1 && extendedInfoBitmaps.get(0) != null && currentContent != ButtonActions.getExtendedInfoString()) {
                    System.out.println(extendedInfoBitmaps);
                    new imageGetter().execute(extendedImage.getWidth(), extendedImage.getHeight(), 0);
                    extendedHandler.postDelayed(extendedInfoChecker, 2000);
                } else {
                    extendedHandler.postDelayed(extendedInfoChecker, 2000);}

                if (elaspedTime == 0) {
                    extendedHandler.post(progressSetter);}


                if (ButtonActions.isPaused() && rootView.findViewById(R.id.play_pause_button_extended).getTag() != "play") {
                    rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.play_button);
                    rootView.findViewById(R.id.play_pause_button_extended).setTag("play");

                } else if (!(ButtonActions.isPaused()) && rootView.findViewById(R.id.play_pause_button_extended).getTag() != "pause") {
                    rootView.findViewById(R.id.play_pause_button_extended).setBackgroundResource(R.drawable.pause_button);
                    rootView.findViewById(R.id.play_pause_button_extended).setTag("pause");
                }

                plotText.setText(ButtonActions.getPlot());


            } else {
                visibilityChanger(true);
                extendedHandler.postDelayed(extendedInfoChecker, 2500);}
        } else {
            visibilityChanger(true);
            extendedHandler.postDelayed(connectionChecker, 3500);}
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
            currentContent = ButtonActions.getExtendedInfoString();
        }
    }

    private Runnable connectionChecker = new Runnable() {
        public void run() {
            if (sharedPreferences.getString("successful_connection", "").equals("y") || ButtonActions.getStatus()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    connecting.setVisibility(View.VISIBLE);
                    connecting.setText("Connecting...");
                    if (ButtonActions.getStatus()) {
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
            }
        }
    };

    private Runnable progressSetter = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            DecimalFormat formatter = new DecimalFormat("00");
            if (ButtonActions.getContentTime() != null) {
                if (ButtonActions.getContentTime() > 0) {
                    if (contentTime != ButtonActions.getContentTime().intValue()) {
                        contentTime = ButtonActions.getContentTime().intValue();
                        seekBar.setMax(contentTime);
                        if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                            totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                        } else {
                            totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(contentTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                        }
                    }
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
        extendedActions = (ImageButton) rootView.findViewById(R.id.subtitle_button);
        seekBar = (SeekBar) rootView.findViewById(R.id.seek_bar);
        currentProgress = (TextView) rootView.findViewById(R.id.current_progress);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        playerActionsLayout = (LinearLayout) rootView.findViewById(R.id.player_actions_layout);
        separator = rootView.findViewById(R.id.extended_separator);
        plotText = (TextView) rootView.findViewById(R.id.plot_text);
        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.scrollView_extended);


        sharedPreferences = getActivity().getSharedPreferences("connection_info", Context.MODE_PRIVATE);

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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


        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                System.out.println("scroll x: " + scrollX);
                System.out.println("scroll y: " + scrollY);
                System.out.println("old - scroll x: " + oldScrollX);
                System.out.println("old - scroll y: " + oldScrollY);


                float scrollYdouble = (float) scrollY;
                extendedImage.setAlpha(1.0f - (scrollYdouble/350));


            }
        });

        extendedActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.extended_menu, popupMenu.getMenu());
                final ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.remote_page_viewer);

                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.toString()){
                            case ("Subtitles"):
                                final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_view, ButtonActions.getSubtitleInfo());
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                                builder.setTitle("Subtitles");
                                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (adapter.getItem(0).equals("Remove Subtitles")) {
                                            switch (which){
                                                case 0:
                                                    ButtonActions.subtitleAction(which - 3);
                                                    break;
                                                case 1:
                                                    ButtonActions.sync("subtitledelay");
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                case 2:
                                                    ButtonActions.getSubs();
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                default:
                                                    ButtonActions.subtitleAction(which - 3);
                                                    break;
                                           }
                                        } else  {
                                            switch (which){
                                                case 0:
                                                    ButtonActions.getSubs();
                                                    break;
                                                default:
                                                    ButtonActions.subtitleAction(which - 1);
                                                    break;
                                            }
                                        }
                                    }
                                });
                                builder.show();
                                break;
                            case ("Audio Streams"):
                                final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), R.layout.list_view, ButtonActions.getAudioStreamInfo());
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity(), R.style.myAlertDialog);
                                if (adapter2.getCount() > 0) {
                                    builder2.setTitle("Audio Streams");
                                    builder2.setAdapter(adapter2, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    ButtonActions.sync("audiodelay");
                                                    viewPager.setCurrentItem(1);
                                                    break;
                                                case 1:
                                                    ButtonActions.audiStreamAction(which - 1);
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
                                System.out.println(adapter2.getCount());
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
    }

    @Override
    public void onStop() {
        super.onStop();
        extendedHandler.removeCallbacks(null);
    }
}

