package com.levyinc.android.kodimote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


class ButtonActions {

    private static boolean status = false;
    private static String request;
    private static ArrayList<String> playerInfo = new ArrayList<>();
    private static ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private static ArrayList<Integer> videoDetailsNums = new ArrayList<>();
    private static String videoDetails;
    private static Long contentTime;
    private static Long elaspedTime;
    private static String episodeName;
    private static String seriesName;
    private static int speed = 1;
    private static boolean isPaused = false;
    private static String plot = null;
    public static Handler buttonActionsHandler = new Handler();


    static boolean getStatus() {
        return status;
    }


    private static class AsynchConnect extends AsyncTask <Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
                    try {
                        URL url = new URL(request);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
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
        request = "http://" + piIp + ":" + piPort + "/jsonrpc";
        new AsynchConnect().execute();
    }

    static void disconnect() {
        try {
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.disconnect();
        } catch (IOException exception) {
            System.out.println(exception.toString());
        }
    }


    private static class AsynchFastForward extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();


                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                System.out.println(jsonParam.toString());
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println(speed);
            super.onPostExecute(aVoid);
        }
    }

        static void fastForward() {
     new AsynchFastForward().execute();
    }

    private static class AsynchRollBack extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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

                byte[] bytes = jsonParam.toString().getBytes("UTF-8");


                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void rollBack() {
       new AsynchRollBack().execute();
    }

    private static class AsynchPlayPause extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            speed = 1;
            if (!playerInfo.isEmpty()) {
                try {
                    URL url = new URL(request);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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
                    byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.connect();


                    OutputStream printout = conn.getOutputStream();
                    printout.write(bytes);
                    printout.flush();
                    printout.close();
                    conn.disconnect();
                } catch (IOException | JSONException exception) {
                    exception.printStackTrace();
                }
            }                 return null;

        }
    }

        static void playPause() {
       new AsynchPlayPause().execute();
    }


    private static class AsynchStop extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            speed = 1;
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

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
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");


                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();


                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void stop() {
     new AsynchStop().execute();
    }

    private static class AsynchUp extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Up");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }

            return null;
        }
    }

        static void up() {
       new AsynchUp().execute();
    }

    private static class AsynchDown extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Down");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void down() {
            new AsynchDown().execute();
    }

    private static class AsynchSelect extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Select");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");


                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void select() {
        new AsynchSelect().execute();
    }

    private static class AsynchLeft extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Left");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }
        static void left() {
        new AsynchLeft().execute();
    }

    private static class AsynchRight extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Right");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void right() {
        new AsynchRight().execute();
    }

    private static class AsynchBack extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Back");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setFixedLengthStreamingMode(bytes.length);

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

    static void back () {
        new AsynchBack().execute();
    }

    private static class AsynchInfoChecker extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.GetActivePlayers");
                jsonParam.put("id", 1);
                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
                conn.disconnect();


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

                        String properties = "[\"speed\", \"time\", \"percentage\", \"subtitles\", \"totaltime\", \"type\"]";
                        jsonParam2.put("properties", properties);
                        jsonParam.put("params", jsonParam2);
                        String jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "["));
                        jsonParamString = jsonParamString.replaceAll("]\"", "]");
                        jsonParamString = jsonParamString.replaceAll("\\\\", "");

                        bytes = jsonParamString.getBytes("UTF-8");

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
                        conn.setFixedLengthStreamingMode(bytes.length);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.connect();
                        printout = conn.getOutputStream();
                        printout.write(bytes);
                        printout.flush();
                        printout.close();
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
            if (playerInfoGotten()){
                new AsynchLibraryQuerier().execute();

            }
            buttonActionsHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new AsynchInfoChecker().execute();
                }
            }, 1500);
            super.onPostExecute(aVoid);
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


    private static class AsynchLibraryQuerier extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            try {

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.getItem");
                jsonParam.put("id", "VideoGetItem");
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                if (matcher2.find()) {
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    String properties = "[plot\", \"rating\", \"art\", \"title\" ,\"album\", \"artist\", \"season\", \"episode\", \"duration\", \"showtitle\", \"tvshowid\", \"thumbnail\", \"file\", \"fanart\", \"streamdetails]";
                    jsonParam2.put("properties", properties);
                    jsonParam.put("params", jsonParam2);
                }

                String jsonParamString = (jsonParam.toString().replaceAll("\"\\[" , "[\""));
                jsonParamString = (jsonParamString.replaceAll("\\]\"" , "\"]"));
                jsonParamString = (jsonParamString.replaceAll("\\\\",""));

                byte[] bytes = jsonParamString.getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();
                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF8")));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }

                br.close();
                conn.disconnect();


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
                    if (episodeNameMatcher.group() == episodeName || episodeName == null) {
                        if (seriesMatch.find() && imageMatcher.find()) {
                            try {
                                bitmapArrayList = new ArrayList<>();
                                url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                                Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                bitmapArrayList.add(decodedImage);

                                url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));
                                Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                                bitmapArrayList.add(decodedImage2);

                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        } else if (seriesMatch.find() && imageMatcher.find()) {
                            try {
                                bitmapArrayList = new ArrayList<>();
                                bitmapArrayList = new ArrayList<>();
                                url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                                Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                bitmapArrayList.add(decodedImage);
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                        } else if (imageMatcher.find()) {
                            try {
                                bitmapArrayList = new ArrayList<>();
                                url = new URL(URLDecoder.decode(imageMatcher.group(), "UTF-8"));
                                Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                                bitmapArrayList.add(decodedImage2);
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        } else {
                            bitmapArrayList = new ArrayList<>();
                            bitmapArrayList.add(0, null);
                            bitmapArrayList.add(1, null);
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


            return null;
        }

    }


    private static class AsyncSubtitleMenu extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "GUI.ActivateWindow");
                jsonParam.put("id", 1);

                JSONObject jsonParam2 = new JSONObject();
                jsonParam2.put("window", "subtitlesearch");

                jsonParam.put("params", jsonParam2);

                byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();
                OutputStream printout = conn.getOutputStream();
                printout.write(bytes);
                printout.flush();
                printout.close();
                conn.disconnect();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SlidingTabActivity.viewPager.setCurrentItem(1, true);
            super.onPostExecute(aVoid);

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
        new AsyncSubtitleMenu().execute();
    }

    static Long getContentTime() {
        return contentTime;
    }

    static  Long getElaspedTime() {
        return elaspedTime;
    }

    private static class AsyncSeek extends AsyncTask<Float, Void, Void> {
        @Override
        protected Void doInBackground(Float... params) {
            if (playerInfo.toArray().length > 0) {
                try {
                    URL url = new URL(request);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Pattern pattern3 = Pattern.compile("\\d$");
                    Matcher matcher3 = pattern3.matcher(playerInfo.get(0));

                    if (matcher3.find()) {
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("jsonrpc", "2.0");
                        jsonParam.put("method", "Player.Seek");
                        jsonParam.put("id", 1);

                        JSONObject jsonParam2 = new JSONObject();
                        jsonParam2.put("playerid", parseInt(matcher3.group()));
                        jsonParam2.put("value", params[0]);

                        jsonParam.put("params", jsonParam2);

                        byte[] bytes = jsonParam.toString().getBytes("UTF-8");

                        conn.setFixedLengthStreamingMode(bytes.length);

                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.connect();

                        OutputStream printout = conn.getOutputStream();
                        printout.write(bytes);
                        printout.flush();
                        printout.close();
                        conn.disconnect();
                    }
                } catch (IOException | JSONException exception) {
                    exception.printStackTrace();
                }
            }
            return null;
        }
    }


    static void playerSeek (float percentage) {
        new AsyncSeek().execute(percentage);
    }

}


