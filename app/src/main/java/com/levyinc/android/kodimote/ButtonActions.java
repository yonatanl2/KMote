package com.levyinc.android.kodimote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
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

enum Type {AUDIO, VIDEO, PICTURE}

class ButtonActions {

    private static boolean status = false;
    private static String request;
    private static ArrayList<String> playerInfo = new ArrayList<>();
    private static int speed = 1;
    private static boolean isPaused = false;
    private static boolean isShuffled;
    private static boolean isRepeat;
    private static AsynchInfoChecker infoChecker = new AsynchInfoChecker();
    private static Lock lock = new ReentrantLock();
    private static Type type;

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
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault());

            Log.i("Info Checker", format.format(cal.getTime()));
            lock.lock();
            Log.i("Info Checker", "Locking");
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.GetActivePlayers");
                jsonParam.put("id", 1);
                URLConnection conn = new URL(request + jsonParam).openConnection();
                conn.setReadTimeout(1500);
                InputStream inputStream = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                Log.println(Log.DEBUG, "Responses", jsonString.toString());
                Pattern pattern = Pattern.compile("\"playerid\":(\\d)");
                Matcher matcher = pattern.matcher(jsonString);
                if (matcher.find()) {
                    playerInfo.add(0, matcher.group());
                } else {
                    playerInfo = new ArrayList<>();
                }
                Pattern pattern2 = Pattern.compile("\"type\":\"(\\D*)\"");
                Matcher matcher2 = pattern2.matcher(jsonString);
                if (matcher2.find()) {
                    Pattern pattern3 = Pattern.compile("(?<=:).*");
                    Matcher matcher3 = pattern3.matcher(matcher2.group());
                    if (matcher3.find()) {
                        String typeMatch = matcher3.group().replaceAll("\"", "");
                        playerInfo.add(1, typeMatch);
                        if (playerInfo.get(1).equals("video")) {
                            type = Type.VIDEO;
                        } else if (playerInfo.get(1).equals("audio")) {
                            type = Type.AUDIO;
                        } else if (playerInfo.get(1).equals("picture")) {
                            type = Type.PICTURE;
                        }
                    }
                } else {
                    playerInfo = new ArrayList<>();
                    type = null;
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

                        conn = new URL(request + query).openConnection();
                        conn.setReadTimeout(1500);
                        inputStream = conn.getInputStream();
                        br = new BufferedReader(new InputStreamReader(inputStream));

                        jsonString = new StringBuffer();
                        while ((line = br.readLine()) != null) {
                            jsonString.append(line);
                        }
                        br.close();
                        Log.println(Log.DEBUG, "Responses", jsonString.toString());


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
                                ExtendedControlActivity.contentTime = TimeUnit.MILLISECONDS.convert(timeArray.get(0), TimeUnit.HOURS) +
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
                                    ExtendedControlActivity.elaspedTime = TimeUnit.MILLISECONDS.convert(timeArray.get(0), TimeUnit.HOURS) +
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
                                        if (ExtendedControlActivity.getSubtitleInfo().size() <= matches) {
                                            ExtendedControlActivity.setSubtitleInfo(matches, tempArrayList, true);
                                        } else if (ExtendedControlActivity.getSubtitleInfo().size() > matches) {
                                            ExtendedControlActivity.setSubtitleInfo(matches, tempArrayList, false);
                                        }
                                        matches++;
                                    }
                                }

                                Pattern subtitleEnabledPattern = Pattern.compile("(?<=subtitleenabled\":)\\D*?(?=,\")");
                                Matcher subtitleEnabledMatcher = subtitleEnabledPattern.matcher(jsonString);

                                if (subtitleEnabledMatcher.find()) {
                                    ExtendedControlActivity.setSubtitleEnabled((subtitleEnabledMatcher.group().equals("true")));
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

                                    int matches = 0;

                                        while (audioStreamLanguageMatcher.find() && audioStreamNameMatcher.find() && audioStreamIndexMatch.find()) {
                                            ArrayList<String> tempArrayList = new ArrayList<>();
                                            tempArrayList.add(audioStreamIndexMatch.group());
                                            tempArrayList.add(audioStreamLanguageMatcher.group());
                                            tempArrayList.add(audioStreamNameMatcher.group());
                                            if (ExtendedControlActivity.getAudioStreamInfo().size() <= matches) {
                                                ExtendedControlActivity.setAudioStreamInfo(matches, tempArrayList, true);
                                            } else if (ExtendedControlActivity.getAudioStreamInfo().size() > matches) {
                                                ExtendedControlActivity.setAudioStreamInfo(matches, tempArrayList, false);
                                            }
                                            matches++;
                                        }
                                 }

                                if (playerInfo.toArray().length > 0) {
                                    try {
                                        jsonParam = new JSONObject();

                                        jsonParam.put("jsonrpc", "2.0");
                                        jsonParam.put("method", "Player.getItem");
                                        jsonParam.put("id", "VideoGetItem");
                                        pattern2 = Pattern.compile("\\d$");
                                        matcher2 = pattern2.matcher(playerInfo.get(0));

                                        if (matcher2.find()) {
                                            jsonParam2 = new JSONObject();
                                            jsonParam2.put("playerid", parseInt(matcher2.group()));
                                            properties = "[plot\", \"rating\", \"art\", \"cast\", \"title\" ,\"album\", \"artist\", \"season\", \"episode\", \"duration\", \"showtitle\", \"tvshowid\", \"thumbnail\", \"file\", \"fanart\", \"streamdetails]";
                                            jsonParam2.put("properties", properties);
                                            jsonParam.put("params", jsonParam2);

                                            jsonParamString = (jsonParam.toString().replaceAll("\"\\[" , "[\""));
                                            jsonParamString = (jsonParamString.replaceAll("\\]\"" , "\"]"));
                                            jsonParamString = (jsonParamString.replaceAll("\\\\",""));
                                            query = URLEncoder.encode(jsonParamString, "UTF-8");

                                            conn = new URL(request + query).openConnection();
                                            conn.setReadTimeout(1500);
                                            inputStream = conn.getInputStream();

                                            br = new BufferedReader(new InputStreamReader(inputStream));
                                            jsonString = new StringBuffer();
                                            while ((line = br.readLine()) != null) {
                                                jsonString.append(line);
                                            }

                                            br.close();
                                            Log.println(Log.DEBUG, "Responses", jsonString.toString());

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
                                                if (!(episodeNameMatcher.group().equals(ExtendedControlActivity.getEpisodeName())) || ExtendedControlActivity.getEpisodeName() == null) {
                                                    if (seriesMatch.find() && imageMatcher.find()) {
                                                        try {
                                                            ExtendedControlActivity.setBitmapArrayListEmpty();
                                                            URL url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                                                            Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                                            ExtendedControlActivity.addBitmapArrayListItem(decodedImage);

                                                            url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));
                                                            Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                                                            ExtendedControlActivity.addBitmapArrayListItem(decodedImage2);

                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }
                                                    } else if (seriesMatch.find()) {
                                                        try {
                                                            ExtendedControlActivity.setBitmapArrayListEmpty();
                                                            URL url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));

                                                            conn = url.openConnection();
                                                            conn.setReadTimeout(3500);
                                                            Bitmap decodedImage = BitmapFactory.decodeStream(conn.getInputStream());
                                                            ExtendedControlActivity.addBitmapArrayListItem(decodedImage);
                                                            ExtendedControlActivity.addBitmapArrayListItem(null);
                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }

                                                    } else if (imageMatcher.find()) {
                                                        try {
                                                            ExtendedControlActivity.setBitmapArrayListEmpty();
                                                            ExtendedControlActivity.addBitmapArrayListItem(null);
                                                            URL url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));

                                                            conn = url.openConnection();
                                                            conn.setReadTimeout(3500);
                                                            Bitmap decodedImage2 = BitmapFactory.decodeStream(conn.getInputStream());
                                                            ExtendedControlActivity.addBitmapArrayListItem(decodedImage2);
                                                        } catch (IOException exception) {
                                                            exception.printStackTrace();
                                                        }
                                                    } else {
                                                        ExtendedControlActivity.setBitmapArrayListEmpty();
                                                        ExtendedControlActivity.addBitmapArrayListItem(null);
                                                        ExtendedControlActivity.addBitmapArrayListItem(null);
                                                    }

                                                    Pattern castPattern = Pattern.compile("(?<=cast\":\\[\\{).*?(?=\\}\\])");
                                                    Matcher castMatcher = castPattern.matcher(jsonString);
                                                    if (castMatcher.find()) {

                                                        Pattern castMemberPattern = Pattern.compile("(?<=name\":\").*?(?=\",\")");
                                                        Matcher castMemberMatcher = castMemberPattern.matcher(castMatcher.group());

                                                        int matches = 0;

                                                        while (castMemberMatcher.find()) {
                                                            if (ExtendedControlActivity.getCastArray().size() <= matches) {
                                                                ExtendedControlActivity.setCastArray(matches, castMemberMatcher.group(), true);
                                                            } else if (ExtendedControlActivity.getCastArray().size() > matches) {
                                                                ExtendedControlActivity.setCastArray(matches, castMemberMatcher.group(), false);
                                                            }
                                                            matches++;
                                                        }
                                                    }
                                            }

                                            if (showMatcher.find()) {
                                                ExtendedControlActivity.setSeriesName(showMatcher.group());
                                            }

                                            episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
                                            episodeNameMatcher = episodeNamePattern.matcher(jsonString);

                                            if (episodeNameMatcher.find()) {
                                                ExtendedControlActivity.setEpisodeName(episodeNameMatcher.group());
                                            }

                                            Pattern scorePattern = Pattern.compile("(?<=rating\":)\\d\\.\\d*?(?=,)");
                                            Matcher scoreMatcher = scorePattern.matcher(jsonString);

                                            if (scoreMatcher.find()) {
                                                ExtendedControlActivity.setScore((double) Math.round(Float.parseFloat(scoreMatcher.group()) * 100) /100);
                                            }


                                            if (seasonMatcher.find() && episodeMatcher.find()) {
                                                try {
                                                    ExtendedControlActivity.setVideoDetailsNums(parseInt(seasonMatcher.group()), parseInt(episodeMatcher.group()));
                                                } catch (NumberFormatException exception) {
                                                    exception.printStackTrace();
                                                }
                                            }
                                            if (plotMatcher.find()) {
                                                String plot = plotMatcher.group().replace("\\r", "\r");
                                                plot = plot.replace("\\n", "\n");
                                                plot = plot.replace("\\\"", "\"");
                                                if (ExtendedControlActivity.getPlot() == null || !(ExtendedControlActivity.getPlot().equals(plot))) {
                                                    ExtendedControlActivity.setPlot(plot);
                                                }
                                            }
                                        }
                                    }

                                        if (type != null) {
                                            jsonParam = new JSONObject();
                                            jsonParam.put("jsonrpc", "2.0");
                                            jsonParam.put("method", "Playlist.GetItems");
                                            jsonParam.put("id", 1);
                                            jsonParam2 = new JSONObject();
                                            jsonParam2.put("playlistid", type.ordinal());
                                            properties = "[title\", \"showtitle\", \"season\", \"episode\",\"album\",\"artist\",\"duration]";
                                            jsonParam2.put("properties", properties);
                                            jsonParam.put("params", jsonParam2);

                                            jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "[\""));
                                            jsonParamString = (jsonParamString.replaceAll("\\]\"", "\"]"));
                                            jsonParamString = (jsonParamString.replaceAll("\\\\", ""));
                                            query = URLEncoder.encode(jsonParamString, "UTF-8");

                                            conn = new URL(request + query).openConnection();
                                            conn.setReadTimeout(1500);
                                            inputStream = conn.getInputStream();

                                            br = new BufferedReader(new InputStreamReader(inputStream));
                                            jsonString = new StringBuffer();
                                            while ((line = br.readLine()) != null) {
                                                jsonString.append(line);
                                            }
                                            Pattern playlistPattern = Pattern.compile("\\[\\{\\\"album.*(?<=\\\"episode\\\"\\}\\])");
                                            Matcher playlistMatcher = playlistPattern.matcher(jsonString);
                                            ArrayList<JSONObject> playlistArray = new ArrayList<>();
                                            while (playlistMatcher.find()){
                                                JSONObject JSONElement = new JSONObject();
                                                Pattern typePattern = Pattern.compile("(?<=\"type\":\")\\w*");
                                                Matcher typeMatcher = typePattern.matcher(playlistMatcher.group());
                                                if (typeMatcher.find()){
                                                    JSONElement.put("type", typeMatcher.group());
                                                }
                                                Pattern showPattern = Pattern.compile("(?<=\"title\":\").*?(?=\",\"type)");
                                                Matcher showMatcher = showPattern.matcher(playlistMatcher.group());
                                                if (showMatcher.find()){
                                                    JSONElement.put("showtitle", showMatcher.group());
                                                }
                                                Pattern seasonPattern = Pattern.compile("(?<=season\":)\\d*");
                                                Matcher seasonMatcher = seasonPattern.matcher(playlistMatcher.group());
                                                if (seasonMatcher.find()){
                                                    JSONElement.put("season", seasonMatcher.group());
                                                }
                                                Pattern episodePattern = Pattern.compile("(?<=episode\":)\\d*");
                                                Matcher episodeMatcher = episodePattern.matcher(playlistMatcher.group());
                                                if (episodeMatcher.find()){
                                                    JSONElement.put("episode", episodeMatcher.group());
                                                }
                                                playlistArray.add(JSONElement);
                                            }
                                            PlaylistActivity.setPlaylist(playlistArray);
                                            br.close();
                                            }
                                } catch (IOException | JSONException exception) {
                                    exception.printStackTrace();
                                }
                            } else {
                                Log.println(Log.DEBUG, "Response", "none");
                            }
                        }
                    }
                }
                Log.i("Info Checker", "Unlocking");
                lock.unlock();

            } catch (ConnectException connectionRefused) {
                Log.i("Info Checker", "Connection terminated");
                playerInfo = new ArrayList<>();
            } catch (SocketException exception) {
                Log.w("WIFI", "Wifi not connected", exception);
                cancel(true);
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            finally {
                try {
                    Thread.sleep(1500);
                    if (!Main2Activity.getWebSocketStatus()) {
                        doInBackground();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }


    static void stopAsynchTask() {
        infoChecker.cancel(true);
        if (!infoChecker.isCancelled()){
            Log.d("Info Checker", "Cancellation Failed");
        }
    }

        static void getInfo() {
            if (infoChecker.getStatus() != AsyncTask.Status.RUNNING && infoChecker.getStatus() != AsyncTask.Status.FINISHED) {
                try {
                    infoChecker.execute();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Log.d("Info Checker", infoChecker.getStatus().toString());
                }
            } else if (infoChecker.getStatus() == AsyncTask.Status.FINISHED) {
                infoChecker = new AsynchInfoChecker();
                infoChecker.execute();
            } else {
                Log.i("Info Checker", ("Info Checker didn't intiallize: " + infoChecker.getStatus()));
            }
        }

        static boolean isPaused() {
            return isPaused;
    }

    static boolean playerInfoGotten() {
        return (!(playerInfo.isEmpty()) && playerInfo.size() > 0);
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

    static void getSubs() {
        Thread thread = new Thread(new SubtitleMenu());
        thread.start();
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
                    if (!ExtendedControlActivity.getSubtitleEnabled()) {
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
            if (playerInfo.size() > 0) {
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
    }

    static void setIsShuffled() {
        Thread thread = new Thread(new ShuffleThread());
        thread.start();
    }

    private static class RepeatThread implements Runnable {

        @Override
        public void run() {
            if (playerInfo.size() > 0) {
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
    }

    static void setIsRepeat() {
        Thread thread = new Thread(new RepeatThread());
        thread.start();
    }

    private static class TextInput implements Runnable {

        String text;
        TextInput (String text) {
            this.text = text;
        }

        @Override
        public void run() {
            try {

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Input.SendText");
                    jsonParam.put("id", 1);


                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("text", text);
                    jsonParam2.put("done", false);
                    jsonParam.put("params", jsonParam2);
               new URL(request + jsonParam).openStream();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void setText (String text) {
        Thread thread = new Thread(new TextInput(text));
        thread.start();
    }

    private static class GetInfo implements Runnable {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Info");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void getInfoActino () {
        new Thread(new GetInfo()).start();
    }

    private static class ContextMenuAction implements Runnable {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.ContextMenu");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void pressContextMenu() {
        new Thread(new ContextMenuAction()).start();
    }

    private static class ToggleFullScreenAction implements Runnable {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "GUI.SetFullScreen");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void toggleFullScreen() {
        new Thread(new ToggleFullScreenAction()).start();
    }

    private static class ClearAudioLibraryAction implements Runnable {

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "AudioLibrary.Clean");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static class ClearVideoLibraryAction implements Runnable {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "VideoLibrary.Clean");
                jsonParam.put("id", 1);
                new URL(request + jsonParam).openStream();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void clearAudioLibrary() {
        new Thread(new ClearAudioLibraryAction()).start();
    }

    static void clearVideoLibrary() {
        new Thread(new ClearVideoLibraryAction()).start();
    }

    static boolean isIsRepeat(){
        return isRepeat;
    }

    static boolean isIsShuffled(){
        return isShuffled;
    }

}


