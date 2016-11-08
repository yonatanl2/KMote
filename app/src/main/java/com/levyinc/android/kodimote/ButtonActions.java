package com.levyinc.android.kodimote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


class ButtonActions {

    private static boolean status = false;
    private static String request;
    private static ArrayList<String> playerInfo = new ArrayList<>();
    private static ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private static ArrayList<Integer> videoDetailsNums = new ArrayList<>();
    private static ArrayList<ArrayList<String>> subtitleInfo = new ArrayList<>();
    private static ArrayList<ArrayList<String>> audioStreamInfo = new ArrayList<>();
    private static String videoDetails;
    private static Long contentTime;
    private static Long elaspedTime;
    private static String episodeName;
    private static String seriesName;
    private static int speed = 1;
    private static boolean isPaused = false;
    private static boolean subtitleEnabled = false;
    private static String plot = null;
    private static boolean isShuffled;
    private static boolean isRepeat;

    public static Handler buttonActionsHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            System.out.println(msg.obj);
        }
    };
    private static Lock lock = new ReentrantLock();

    static boolean getStatus() {
        return status;
    }


    private static class AsynchConnect extends AsyncTask <Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
                    try {
                        URL url = new URL(request);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        status = (conn.getResponseCode() == 200);
                    } catch (IOException exception) {
                        String stringException = exception.toString();
                        System.out.println(stringException);
                        if (stringException.contains("java.net.UnknownHostException")) {
                            status = false;
                        }
                    }
            return status;
        }
    }

    static void connect(String piIp, String piPort) {
        request = "http://" + piIp + ":" + piPort + "/jsonrpc?request=";
        new AsynchConnect().execute();
    }

    static void disconnect() {
        try {
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.disconnect();
        } catch (IOException exception) {
            System.out.println(exception.toString());
        }
    }


    private static class AsynchFastForward implements Runnable {
        @Override
        public void run() {
            if (speed == 0) {
                speed = 2;
            } else if (speed == -(2)) {
                speed = 1;
            } else if (speed >= 1 && speed < 36) {
                speed = speed * 2;
            } else if (speed < 0 ) {
                speed = speed / 2;
            }

            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.SetSpeed");
                jsonParam.put("id", 1);
                System.out.println(playerInfo);
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));
                if (matcher2.find()) {
                    System.out.println(matcher2.group());
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    jsonParam2.put("speed", speed);
                    System.out.println(jsonParam2);
                    jsonParam.put("params", jsonParam2);
                }
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void fastForward() {
     Thread thread = new Thread(new AsynchFastForward());
        thread.start();
    }

    private static class AsynchRollBack implements Runnable {
        @Override
        public void run() {
            if (speed == 0) {
                speed = -(2);
            } else if (speed > 1) {
                speed = speed / 2;
            } else if (speed == 1) {
                speed = speed * (-2);
            } else if (speed > (-36)) {
                speed = speed * 2;
            }

            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.SetSpeed");
                jsonParam.put("id", 1);
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                if (matcher2.find()) {
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    jsonParam2.put("speed", speed);
                    jsonParam.put("params", jsonParam2);
                }
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void rollBack() {
       Thread thread = new Thread(new AsynchRollBack());
        thread.start();
    }

    private static class AsynchPlayPause implements Runnable {
        @Override
        public void run() {
            speed = 1;
            if (!playerInfo.isEmpty()) {
                try {

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.PlayPause");
                    jsonParam.put("id", 1);
                    Pattern pattern2 = Pattern.compile("\\d$");
                    Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                    if (matcher2.find()) {
                        JSONObject jsonParam2 = new JSONObject();
                        jsonParam2.put("playerid", parseInt(matcher2.group()));
                        jsonParam.put("params", jsonParam2);
                    }
                    new URL(request + jsonParam).openStream();

                } catch (IOException | JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    static void playPause() {
       Thread thread = new Thread(new AsynchPlayPause());
        thread.start();
    }

    private static class AsynchStop implements Runnable {
        @Override
        public void run() {
            speed = 1;
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.Stop");
                jsonParam.put("id", 1);
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                if (matcher2.find()) {
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    jsonParam.put("params", jsonParam2);
                }
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void stop() {
        Thread thread = new Thread(new AsynchStop());
        thread.start();
    }

    private static class AsynchUp implements Runnable {
        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Up");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void up() {
        Thread thread = new Thread(new AsynchUp());
        thread.start();
    }

    private static class AsynchDown implements Runnable {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Down");
                jsonParam.put("id", 1);

                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void down() {
        Thread thread = new Thread(new AsynchDown());
        thread.start();
    }

    private static class AsynchSelect implements Runnable {
        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Select");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void select() {
        Thread thread = new Thread(new AsynchSelect());
        thread.start();
    }

    private static class AsynchLeft implements Runnable {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Left");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void left() {
        Thread thread = new Thread(new AsynchLeft());
        thread.start();
    }

    private static class AsynchRight implements Runnable {
        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Right");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void right() {
        Thread thread = new Thread(new AsynchRight());
        thread.start();
    }

    private static class AsynchBack implements Runnable {
        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Back");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void back () {
        Thread thread = new Thread(new AsynchBack());
        thread.start();
    }

    private static class AsynchInfoChecker extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Log.println(Log.WARN, "log time", sdf.format(cal.getTime()));
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.GetActivePlayers");
                jsonParam.put("id", 1);

                InputStream inputStream = new URL(request + jsonParam).openStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }

                Pattern pattern = Pattern.compile("\"playerid\":(\\d)");
                Matcher matcher = pattern.matcher(jsonString);
                if (matcher.find()) {
                    playerInfo.add(0, matcher.group());
                } else {
                    playerInfo = new ArrayList<>();
                }
                Pattern pattern2 = Pattern.compile("\"type\":\"(\\D*)\"");
                Matcher matcher2 = pattern2.matcher((jsonString));
                if (matcher2.find()) {
                    playerInfo.add(1, matcher2.group());
                } else {
                    playerInfo = new ArrayList<>();
                }
                br.close();

                jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.getProperties");
                jsonParam.put("id", 1);

                if (playerInfo.toArray().length > 0) {
                    Pattern pattern3 = Pattern.compile("\\d$");
                    Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                    if (matcher3.find()) {
                        JSONObject jsonParam2 = new JSONObject();
                        jsonParam2.put("playerid", parseInt(matcher3.group()));

                        String properties = "[\"speed\", \"time\", \"percentage\", \"subtitles\", \"subtitleenabled\", \"audiostreams\", \"totaltime\", \"type\"]";
                        jsonParam2.put("properties", properties);
                        jsonParam.put("params", jsonParam2);

                        String jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "["));
                        jsonParamString = jsonParamString.replaceAll("]\"", "]");
                        jsonParamString = jsonParamString.replaceAll("\\\\", "");
                        String query = URLEncoder.encode(jsonParamString, "UTF-8");

                        inputStream = new URL(request + query).openStream();
                        br = new BufferedReader(new InputStreamReader(inputStream));
                        jsonString = new StringBuffer();
                        while ((line = br.readLine()) != null) {
                            jsonString.append(line);
                        }
                        br.close();

                        isPaused = !(jsonString.toString().contains("\"speed\":1"));

                        Pattern totalTimePattern = Pattern.compile("(?<=totaltime\":\\{\").*(?=\\},\")");
                        Matcher totalTimeMatcher = totalTimePattern.matcher(jsonString);

                        Pattern elaspedTimePattern = Pattern.compile("(?<=time\":\\{\").*(?=\\},\")");
                        Matcher elaspedTimeMatcher = elaspedTimePattern.matcher(jsonString);

                        if (totalTimeMatcher.find()) {
                            Pattern timePattern = Pattern.compile("\\d+");
                            Matcher timeMatcher = timePattern.matcher(totalTimeMatcher.group());

                            ArrayList<Integer> timeArray = new ArrayList<>();
                            while (timeMatcher.find()){
                                timeArray.add(parseInt(timeMatcher.group()));
                            }
                            if (timeArray.toArray().length > 0) {
                                contentTime = TimeUnit.MILLISECONDS.convert(timeArray.get(0), TimeUnit.HOURS) +
                                        TimeUnit.MILLISECONDS.convert(timeArray.get(2), TimeUnit.MINUTES) +
                                        TimeUnit.MILLISECONDS.convert(timeArray.get(3), TimeUnit.SECONDS);
                            }
                            if (elaspedTimeMatcher.find()) {
                                timeMatcher = timePattern.matcher(elaspedTimeMatcher.group());

                                timeArray = new ArrayList<>();
                                while (timeMatcher.find()){
                                    timeArray.add(parseInt(timeMatcher.group()));
                                }
                                if (timeArray.toArray().length > 0) {
                                    elaspedTime = TimeUnit.MILLISECONDS.convert(timeArray.get(0), TimeUnit.HOURS) +
                                            TimeUnit.MILLISECONDS.convert(timeArray.get(2), TimeUnit.MINUTES) +
                                            TimeUnit.MILLISECONDS.convert(timeArray.get(3), TimeUnit.SECONDS);
                                }
                            }

                                Pattern subtitlePattern = Pattern.compile("(?<=subtitles\":\\[\\{).*?(?=\"\\}\\])");
                                Matcher subtitleMatcher = subtitlePattern.matcher(jsonString);

                                if (subtitleMatcher.find()) {
                                    String SubtitleString = subtitleMatcher.group() + "\"}";

                                    Pattern subtitleIndexPattern = Pattern.compile("(?<=index\":)\\d*");
                                    Matcher subtitleIndexMatch = subtitleIndexPattern.matcher(SubtitleString);

                                    Pattern subtitleLanguagePattern = Pattern.compile("(?<=language\":\")\\D*(?=\",\"name)");
                                    Matcher subtitleLanguageMatcher = subtitleLanguagePattern.matcher(SubtitleString);

                                    Pattern subtitleNamePattern = Pattern.compile("(?<=name\":\").*?(?=\"\\})");
                                    Matcher subtitleNameMatcher = subtitleNamePattern.matcher(SubtitleString);

                                    int matches = 0;

                                    while (subtitleIndexMatch.find() && subtitleLanguageMatcher.find() && subtitleNameMatcher.find()) {
                                        ArrayList<String> tempArrayList = new ArrayList<>();
                                        tempArrayList.add(subtitleIndexMatch.group());
                                        tempArrayList.add(subtitleLanguageMatcher.group());
                                        tempArrayList.add(subtitleNameMatcher.group());
                                        if (subtitleInfo.toArray().length <= matches) {
                                            subtitleInfo.add(matches, tempArrayList);
                                        } else if (subtitleInfo.toArray().length > matches) {
                                            subtitleInfo.set(matches, tempArrayList);
                                        }
                                        matches++;
                                    }

                                    Pattern subtitleEnabledPattern = Pattern.compile("(?<=subtitleenabled\":)\\D*?(?=,\")");
                                    Matcher subtitleEnabledMatcher = subtitleEnabledPattern.matcher(jsonString);

                                    if (subtitleEnabledMatcher.find()) {
                                        subtitleEnabled = (subtitleEnabledMatcher.group().equals("true"));
                                    }

                                    Pattern audioStreamPattern = Pattern.compile("(?<=audiostreams\":\\[\\{).*?(?=\"\\}\\])");
                                    Matcher audioStreamMatcher = audioStreamPattern.matcher(jsonString);


                                    if (audioStreamMatcher.find()) {

                                        String audioStreamString = audioStreamMatcher.group() + "\"}";

                                        Pattern audioStreamLanguagePattern = Pattern.compile("(?<=language\":\")\\D*?(?=\",\"name)");
                                        Matcher audioStreamLanguageMatcher = audioStreamLanguagePattern.matcher(audioStreamString);

                                        Pattern audioStreamNamePattern = Pattern.compile("(?<=name\":\").*?(?=\"\\})");
                                        Matcher audioStreamNameMatcher = audioStreamNamePattern.matcher(audioStreamString);

                                        Pattern audioStreamIndexPattern = Pattern.compile("(?<=index\":)\\d*");
                                        Matcher audioStreamIndexMatch = audioStreamIndexPattern.matcher(audioStreamString);

                                        matches = 0;

                                        while (audioStreamLanguageMatcher.find() && audioStreamNameMatcher.find() && audioStreamIndexMatch.find()) {
                                            ArrayList<String> tempArrayList = new ArrayList<>();
                                            tempArrayList.add(audioStreamIndexMatch.group());
                                            tempArrayList.add(audioStreamLanguageMatcher.group());
                                            tempArrayList.add(audioStreamNameMatcher.group());
                                            if (audioStreamInfo.toArray().length <= matches) {
                                                audioStreamInfo.add(matches, tempArrayList);
                                            } else if (audioStreamInfo.toArray().length > matches) {
                                                audioStreamInfo.set(matches, tempArrayList);
                                            }
                                            matches++;
                                        }

                                        if (playerInfoGotten()){

                                            try {
                                                jsonParam = new JSONObject();

                                                jsonParam.put("jsonrpc", "2.0");
                                                jsonParam.put("method", "Player.getItem");
                                                jsonParam.put("id", "VideoGetItem");

                                            if (playerInfo.toArray().length > 0) {

                                                pattern2 = Pattern.compile("\\d$");
                                                matcher2 = pattern2.matcher(playerInfo.get(0));

                                                if (matcher2.find()) {

                                                    jsonParam2 = new JSONObject();
                                                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                                                    properties = "[plot\", \"rating\", \"art\", \"title\" ,\"album\", \"artist\", \"season\", \"episode\", \"duration\", \"showtitle\", \"tvshowid\", \"thumbnail\", \"file\", \"fanart\", \"streamdetails]";
                                                    jsonParam2.put("properties", properties);
                                                    jsonParam.put("params", jsonParam2);
                                                }
                                            }

                                            jsonParamString = (jsonParam.toString().replaceAll("\"\\[" , "[\""));
                                            jsonParamString = (jsonParamString.replaceAll("\\]\"" , "\"]"));
                                            jsonParamString = (jsonParamString.replaceAll("\\\\",""));
                                            query = URLEncoder.encode(jsonParamString, "UTF-8");

                                             inputStream = new URL(request + query).openStream();

                                             br = new BufferedReader(new InputStreamReader(inputStream));
                                            jsonString = new StringBuffer();
                                            while ((line = br.readLine()) != null) {
                                                jsonString.append(line);
                                            }

                                            br.close();

                                            Pattern seasonPattern = Pattern.compile("(?<=season\":)\\d*");
                                            Matcher seasonMatcher = seasonPattern.matcher(jsonString);

                                            Pattern episodePattern = Pattern.compile("(?<=episode\":)\\d*");
                                            Matcher episodeMatcher = episodePattern.matcher(jsonString);

                                            Pattern showPattern = Pattern.compile("(?<=showtitle\":\").*(?=\",\"stream)");
                                            Matcher showMatcher = showPattern.matcher(jsonString);

                                            Pattern episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
                                            Matcher episodeNameMatcher = episodeNamePattern.matcher(jsonString);

                                            Pattern plotPattern = Pattern.compile("(?<=plot\":\").*(?=\",\"rating)");
                                            Matcher plotMatcher = plotPattern.matcher(jsonString);

                                            Pattern seriesImagePattern = Pattern.compile("(?<=\"poster\":\"image://)http.*(?=/\",\"season)");
                                            Matcher seriesMatch = seriesImagePattern.matcher(jsonString);

                                            Pattern imagePattern = Pattern.compile("(?<=thumbnail\":\"image://)http.*(?=/\")");
                                            Matcher imageMatcher = imagePattern.matcher(jsonString);

                                            if (episodeNameMatcher.find()) {
                                                if (episodeNameMatcher.group().equals(episodeName) || episodeName == null) {
                                                    if (seriesMatch.find() && imageMatcher.find()) {
                                                        try {
                                                            bitmapArrayList = new ArrayList<>();
                                                            URL url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                                                            Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                                            bitmapArrayList.add(decodedImage);

                                                            url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));
                                                            Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                                                            bitmapArrayList.add(decodedImage2);

                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }
                                                    } else if (seriesMatch.find()) {
                                                        try {
                                                            bitmapArrayList = new ArrayList<>();
                                                            URL url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                                                            Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                                            bitmapArrayList.add(decodedImage);
                                                            bitmapArrayList.add(null);
                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }

                                                    } else if (imageMatcher.find()) {
                                                        try {
                                                            bitmapArrayList = new ArrayList<>();
                                                            bitmapArrayList.add(null);
                                                            URL url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));
                                                            Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                                                            bitmapArrayList.add(decodedImage2);
                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }
                                                    } else {
                                                        bitmapArrayList = new ArrayList<>();
                                                        bitmapArrayList.add(null);
                                                        bitmapArrayList.add(null);
                                                    }
                                                }
                                            }

                                            if (showMatcher.find()) {
                                                seriesName = showMatcher.group();
                                            }

                                            episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
                                            episodeNameMatcher = episodeNamePattern.matcher(jsonString);

                                            if (episodeNameMatcher.find()) {
                                                episodeName = (episodeNameMatcher.group());
                                            }


                                            if (seasonMatcher.find() && episodeMatcher.find()) {
                                                try {
                                                    videoDetailsNums = new ArrayList<>();
                                                    videoDetailsNums.add(parseInt(seasonMatcher.group()));
                                                    videoDetailsNums.add(parseInt(episodeMatcher.group()));
                                                } catch (NumberFormatException exception) {
                                                    exception.printStackTrace();
                                                }
                                            }
                                            if (plotMatcher.find()) {
                                                plot = plotMatcher.group();
                                            }

                                        } catch (IOException | JSONException exception) {
                                            exception.printStackTrace();
                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            buttonActionsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AsynchInfoChecker().execute();
                }
            }, 1500);
        }
    }



        static void getInfo() {
            new AsynchInfoChecker().execute();
        }

        static boolean isPaused() {
            return isPaused;
    }

    static boolean playerInfoGotten() {
        return (!(playerInfo.isEmpty()) && playerInfo.toArray().length > 0);
    }


    private static class SubtitleMenu implements Runnable {

        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "GUI.ActivateWindow");
                jsonParam.put("id", 1);

                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("window", "subtitlesearch");

                jsonParam.put("params", jsonParam2);
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static boolean extendedInfoGotten() {
        return (bitmapArrayList.toArray().length > 0);
    }

    static boolean extendedInfoGottenNums() {
        return !(videoDetailsNums.isEmpty());
    }


    static ArrayList<String> getExtendedInfoString(){
        ArrayList<String> tempArrayList = new ArrayList<>();
        tempArrayList.add(seriesName);
        tempArrayList.add(episodeName);
        return tempArrayList;
    }

    static ArrayList<Integer> getVideoDetailsNums() {
        return videoDetailsNums;
    }

    static ArrayList<Bitmap> getExtendedInfoBitmaps(){
        return bitmapArrayList;

    }

    static String getPlot () {
        return plot;
    }

    static void getSubs() {
        Thread thread = new Thread(new SubtitleMenu());
        thread.start();
    }

    static Long getContentTime() {
        return contentTime;
    }

    static  Long getElaspedTime() {
        return elaspedTime;
    }

    private static class Seek implements Runnable {

        Float percentage;
        Seek (Float percentage) {
            this.percentage = percentage;
        }

        @Override
        public void run() {
            try {
                    Pattern pattern3 = Pattern.compile("\\d$");
                    Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                    if (matcher3.find()) {
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("jsonrpc", "2.0");
                        jsonParam.put("method", "Player.Seek");
                        jsonParam.put("id", 1);

                        JSONObject jsonParam2 = new JSONObject();
                        jsonParam2.put("playerid", parseInt(matcher3.group()));
                        jsonParam2.put("value", percentage);

                        jsonParam.put("params", jsonParam2);

                        new URL(request + jsonParam).openStream();

                    }
                } catch (IOException | JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }

    static void playerSeek (float percentage) {
        Thread thread = new Thread(new Seek(percentage));
        thread.start();
    }

    private static class PowerAction implements Runnable {

        String action;
        PowerAction (String action) {
            this.action = action;
        }

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", action);
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }


    static void powerButton(String method) {
        if (method.equals("shutdown")) {
            Thread thread = new Thread(new PowerAction("System.Shutdown"));
            thread.start();
        } else if (method.equals("reboot")) {
            Thread thread = new Thread(new PowerAction("System.Reboot"));
            thread.start();
        }
    }

    static ArrayList<String> getSubtitleInfo() {
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

    private static class AsynchSubtitleAction implements Runnable {

        int index;

        AsynchSubtitleAction (int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {

                Pattern pattern3 = Pattern.compile("\\d$");
                Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                if (matcher3.find()) {
                    if (!subtitleEnabled) {
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("jsonrpc", "2.0");
                        jsonParam.put("method", "Player.SetSubtitle");
                        jsonParam.put("id", 1);

                        JSONObject jsonParam2 = new JSONObject();
                        jsonParam2.put("playerid", parseInt(matcher3.group()));
                        jsonParam2.put("subtitle", "on");

                        jsonParam.put("params", jsonParam2);
                        new URL(request + jsonParam);
                    }

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.SetSubtitle");
                    jsonParam.put("id", 1);

                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher3.group()));
                    if (index > (-1)) {
                        jsonParam2.put("subtitle", index);
                    } else {
                        jsonParam2.put("subtitle", "off");
                    }

                    jsonParam.put("params", jsonParam2);
                    new URL(request + jsonParam).openStream();

                }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void subtitleAction(int index) {
        Thread thread = new Thread(new AsynchSubtitleAction(index));
        thread.start();
    }

    private static class ExecuteAction implements Runnable {

        String action;

        ExecuteAction (String action) {
            this.action = action;
        }

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.ExecuteAction");
                jsonParam.put("id", 1);

                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("action", action);
                jsonParam.put("params", jsonParam2);
                new URL(request + jsonParam).openStream();


            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void sync(String action) {
        Thread thread = new Thread(new ExecuteAction(action));
        thread.start();
    }

    static ArrayList<String> getAudioStreamInfo() {
        ArrayList<String> tempList = new ArrayList<>();
        if (audioStreamInfo.toArray().length > 0) {
            tempList.add("Sync Audio Stream");
        }
        for (int i = 0; i < audioStreamInfo.toArray().length; i++){
            tempList.add(audioStreamInfo.get(i).get(2) + " : " + audioStreamInfo.get(i).get(1));
        }
        return tempList;
    }

    private static class AsynchAudioStreamAction implements Runnable {

        int index;

        AsynchAudioStreamAction (int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {

                Pattern pattern3 = Pattern.compile("\\d$");
                Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                if (matcher3.find()) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.SetAudioStream");
                    jsonParam.put("id", 1);

                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher3.group()));
                    jsonParam2.put("stream", index);

                    jsonParam.put("params", jsonParam2);

                    new URL(request + jsonParam).openStream();

                }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void audiStreamAction(int index) {
        Thread thread = new Thread(new AsynchAudioStreamAction(index));
        thread.start();
    }

    static void volumeAction(String action){
        Thread thread = new Thread(new ExecuteAction(action));
        thread.start();
    }

    private static class HomeButton implements Runnable {
        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Home");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void homeButton() {
        Thread thread = new Thread(new HomeButton());
        thread.start();
    }

    private static class SetVolumePercentage implements Runnable {

        int percentage;

        SetVolumePercentage (int percentage) {
            this.percentage = percentage;
        }

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Application.SetVolume");
                jsonParam.put("id", 1);

                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("volume", percentage);
                jsonParam.put("params", jsonParam2);

                new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void volumePercentageSetter(int percentage){
        Thread thread = new Thread(new SetVolumePercentage(percentage));
        thread.start();
    }

    private static class MuteThread implements Runnable {

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.ExecuteAction");
                jsonParam.put("id", 1);

                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("action", "mute");
                jsonParam.put("params", jsonParam2);
                new URL(request + jsonParam).openStream();


            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void muteButton() {
        Thread thread = new Thread(new MuteThread());
        thread.start();
    }
    private static class ShuffleThread implements Runnable {

        @Override
        public void run() {
            try {
                Pattern pattern3 = Pattern.compile("\\d$");
                Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                if (matcher3.find()) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.SetShuffle");
                    jsonParam.put("id", 1);


                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playedid", parseInt(matcher3.group()));
                    jsonParam2.put("shuffle", !isShuffled);
                    jsonParam.put("params", jsonParam2);
                    new URL(request + jsonParam).openStream();

                    isShuffled = !isShuffled;
                }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void setIsShuffled() {
        Thread thread = new Thread(new ShuffleThread());
        thread.start();
    }

    private static class RepeatThread implements Runnable {

        @Override
        public void run() {
            try {
                Pattern pattern3 = Pattern.compile("\\d$");
                Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                if (matcher3.find()) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.SetShuffle");
                    jsonParam.put("id", 1);


                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playedid", parseInt(matcher3.group()));
                    jsonParam2.put("shuffle", !isRepeat);
                    jsonParam.put("params", jsonParam2);
                    new URL(request + jsonParam).openStream();

                    isRepeat = !isRepeat;
                }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void setIsRepeat() {
        Thread thread = new Thread(new RepeatThread());
        thread.start();
    }
}


