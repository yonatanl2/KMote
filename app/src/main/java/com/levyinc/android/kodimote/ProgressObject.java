package com.levyinc.android.kodimote;

/**
 * Created by yonatan on 2/18/17.
 */

public class ProgressObject {

    private long elaspedTime;
    private long contentTime;

        ProgressObject(long elaspedTime, long contentTime) {
            this.elaspedTime = elaspedTime;
            this.contentTime = contentTime;
        }

        public long getElaspedTime() {
            return elaspedTime;
        }

        public long getContentTime() {
            return contentTime;
        }

        public int getContentTimeInt(){
            return  (int) contentTime;
    }

}
