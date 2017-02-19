package com.levyinc.android.kodimote;


import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ProgressSetter implements Runnable {

    private ProgressHandler progressHandler;

    private long currentElaspedTime;

        public void run() {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault());

            System.out.println("Measuring progress: " + format.format(cal.getTime()));

            long elaspedTime = ExtendedControlActivity.getElaspedTime();
            long contentTime = ExtendedControlActivity.getContentTime();


            Message progressMessage = new Message();
            if (!(TimeUnit.SECONDS.toSeconds(currentElaspedTime) >= TimeUnit.SECONDS.toSeconds(contentTime))) {
                if (currentElaspedTime > elaspedTime && currentElaspedTime < (elaspedTime + 1000) && (!ButtonActions.isPaused() || !Main2Activity.webSocketEndpoint.pauseStatus())) {
                    currentElaspedTime += 1000;
                    System.out.println("1");
                } else {
                    currentElaspedTime = elaspedTime;
                    System.out.println("2");
                }


                progressMessage.obj = new ProgressObject(elaspedTime, contentTime);
                progressHandler.sendMessage(progressMessage);

                if (currentElaspedTime > contentTime) {
                    currentElaspedTime = 0;
                }

                try {
                    synchronized (this) {
                        wait(500);
                    }
                } catch (InterruptedException exception){
                    exception.printStackTrace();
                }
                if (Main2Activity.getWebSocketStatus()) {
                    if (!(Main2Activity.webSocketEndpoint.pauseStatus() || !Main2Activity.webSocketEndpoint.playerInfoGotten())) {
                        progressMessage = new Message();
                        progressMessage.obj = "RUN";
                        progressHandler.sendMessage(progressMessage);
                    }
                } else {
                    if (!(ButtonActions.isPaused() || !ButtonActions.playerInfoGotten())) {
                        progressMessage = new Message();
                        progressMessage.obj = "RUN";
                        progressHandler.sendMessage(progressMessage);
                    }
                }
            }

        }

    void setProgressHandler(ProgressHandler handler){
        this.progressHandler = handler;
    }
}
