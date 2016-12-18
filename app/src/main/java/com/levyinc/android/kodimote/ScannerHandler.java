package com.levyinc.android.kodimote;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.View;


class ScannerHandler extends Handler{

    private View currentView;
    private ProgressDialog dialog;

    ScannerHandler(View view, ProgressDialog dialog){
        this.currentView = view;
        this.dialog = dialog;
    }

    @Override
    public void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {

        if (msg.obj.equals("success")) {
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
        }
    }
}
