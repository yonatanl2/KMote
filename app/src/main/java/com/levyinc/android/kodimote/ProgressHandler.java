package com.levyinc.android.kodimote;


import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

class ProgressHandler extends Handler {

    private SeekBar seekBar;
    private TextView currentProgress;
    private TextView totalTime;
    private DecimalFormat formatter = new DecimalFormat("00");
    private Thread progressThread;

    ProgressHandler(SeekBar seekBar, TextView currentProgress, TextView totalTime, Thread progressThread, ProgressSetter progressSetter){
        this.seekBar = seekBar;
        this.currentProgress = currentProgress;
        this.totalTime = totalTime;
        this.progressThread = progressThread;
    }

    @Override
    public void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {

        if(msg.obj.toString().equals("RUN")){
            progressThread.start();
        }
        else {
            ProgressObject progressObject = (ProgressObject) msg.obj;
            long contentTime = progressObject.getContentTime();
            long currentElaspedTime = progressObject.getElaspedTime();
            if (totalTime.getTag() == null) {
                totalTime.setTag(contentTime);
                if (contentTime > 0) {
                    seekBar.setMax((int) contentTime);
                }
                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                } else {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(contentTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                }
            } else if (Long.parseLong(totalTime.getTag().toString()) != contentTime) {
                totalTime.setTag(contentTime);
                if (contentTime > 0) {
                    seekBar.setMax((int) contentTime);
                }
                if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                } else {
                    totalTime.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(contentTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(contentTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(contentTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(contentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(contentTime))));
                }
            }


            if (seekBar.getProgress() != (int) currentElaspedTime) {
                seekBar.setProgress((int) currentElaspedTime);
            }

            if (TimeUnit.MILLISECONDS.toHours(contentTime) == 0) {
                currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(currentElaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(currentElaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime))));
            } else {
                currentProgress.setText(formatter.format(TimeUnit.MILLISECONDS.toHours(currentElaspedTime)) + ":" + formatter.format(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime) - TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(currentElaspedTime))) + ":" + formatter.format(TimeUnit.MILLISECONDS.toSeconds(currentElaspedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentElaspedTime))));
            }
        }




        /*if (msg.obj.equals("success")) {
            Message successMessage = new Message();
            successMessage.obj = "Success";
            sendMessageAtFrontOfQueue(successMessage);
            synchronized (this) {
                notifyAll();
            }
        } else if (msg.obj.equals("failure")) {
            Message failureMessage = new Message();
            failureMessage.obj = "Failure";
            sendMessageAtFrontOfQueue(failureMessage);
            synchronized (this) {
                notifyAll();
            }
        } else if (msg.obj.equals("successfulConnection")) {
            Snackbar.make(currentView, "Success", Snackbar.LENGTH_SHORT).show();
            dialog.dismiss();
        } else if (msg.obj.equals("failedConnection")) {
            Snackbar.make(currentView, "No Device Found...", Snackbar.LENGTH_SHORT).show();
            dialog.dismiss();
        }*/
    }
}
