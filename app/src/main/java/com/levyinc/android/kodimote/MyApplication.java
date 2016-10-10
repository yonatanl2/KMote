package com.levyinc.android.kodimote;

import android.app.Application;
import android.content.Context;

/**
 * Created by yonatan on 01/10/16.
 */

public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}