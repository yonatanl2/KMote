package com.levyinc.android.kodimote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


class ButtonActions {

    private static boolean status = false;
    private static String request;
    private static ArrayList<String> playerInfo = new ArrayList<>();
    private static ArrayList<String> videoDetailArray = new ArrayList<>();
    private static ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private static int speed = 1;
    private static boolean isPaused = false;


    static boolean getStatus() {
        return status;
    }


    private static class AsynchConnect extends AsyncTask <Void, Void, Boolean> {

        private Integer respCode;


        @Override
        protected Boolean doInBackground(Void... params) {
                    try {
                        URL url = new URL(request);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        respCode = conn.getResponseCode();
                        status = (respCode == 200);
                    } catch (IOException exception) {
                        String stringException = exception.toString();
                        System.out.println(stringException);
                        if (stringException.contains("java.net.UnknownHostException")) {
                            status = false;
                        }
                    }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
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
            playerInfo = new ArrayList<>();
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
                    playerInfo.add(matcher.group());
                }
                Pattern pattern2 = Pattern.compile("\"type\":\"(\\D*)\"");
                Matcher matcher2 = pattern2.matcher((jsonString));
                if (matcher2.find()) {
                    playerInfo.add(matcher2.group());
                }
                br.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (playerInfoGotten()){
                new AsynchPropertiesGetter().execute();
            }
            super.onPostExecute(aVoid);
        }
    }

        static void getInfo() {
            new AsynchInfoChecker().execute();
        }

    private static class AsynchPropertiesGetter extends AsyncTask<Void, Void, Void> {


        StringBuffer jsonString;
        JSONObject jsonParam;
        String jsonParamString;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.getProperties");
                jsonParam.put("id", 1);
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                if (matcher2.find()) {
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    String properties = "[\"speed\", \"time\", \"percentage\", \"subtitles\", \"totaltime\", \"type\"]";
                    jsonParam2.put("properties", properties);
                    jsonParam.put("params", jsonParam2);
                }
                jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "["));
                jsonParamString = jsonParamString.replaceAll("]\"", "]");
                jsonParamString = jsonParamString.replaceAll("\\\\", "");

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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                conn.disconnect();
                isPaused = !(jsonString.toString().contains("\"speed\":1"));
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println(jsonString);
            new AsynchLibraryQuerier().execute();
            super.onPostExecute(aVoid);
        }
    }

        static boolean isPaused() {
            return isPaused;
    }

    static boolean playerInfoGotten() {
        return !(playerInfo.isEmpty());
    }


    private static class AsynchLibraryQuerier extends AsyncTask<Void, Void, Void>{

        StringBuffer jsonString;
        String jsonParamString;

        @Override
        protected Void doInBackground(Void... params) {

            videoDetailArray = new ArrayList<>();
            bitmapArrayList = new ArrayList<>();

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

                jsonParamString = (jsonParam.toString().replaceAll("\"\\[" , "[\""));
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
                jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }

                br.close();
                conn.disconnect();

            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }

            Pattern seriesImagePattern = Pattern.compile("(?<=\"poster\":\"image://)http.*(?=/\",\"season)");
            Matcher seriesMatch = seriesImagePattern.matcher(jsonString);
            if (seriesMatch.find()){
                try {
                    URL url = new URL(URLDecoder.decode(seriesMatch.group(), "UTF-8"));
                    Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    bitmapArrayList.add(decodedImage);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }


            } else {
                bitmapArrayList.add(null);
            }

            Pattern imagePattern = Pattern.compile("(?<=thumbnail\":\"image://)http.*(?=/\")");
            Matcher imageMatcher = imagePattern.matcher(jsonString);


            if (imageMatcher.find()){
                try {
                    URL url = new URL(URLDecoder.decode(imageMatcher.group(),"UTF-8"));
                    Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
                    bitmapArrayList.add(decodedImage2);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } else {
                bitmapArrayList.add(null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println(jsonParamString);
            System.out.println(jsonString);
            System.out.println(videoDetailArray);
            System.out.println(bitmapArrayList);


            Pattern seriesImagePattern = Pattern.compile("(?<=\"poster\":\"image://)http.*(?=/\",\"season)");
            Matcher seriesMatch = seriesImagePattern.matcher(jsonString);
            if (seriesMatch.find()) {
                try {
                    String temp = URLDecoder.decode(seriesMatch.group(), "UTF-8");
                    System.out.println(temp);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            Pattern seasonPattern = Pattern.compile("(?<=season\":)\\d*");
            Matcher seasonMatcher = seasonPattern.matcher(jsonString);
            if (seasonMatcher.find()){
              //  System.out.println(seasonMatcher.group());
            }

            Pattern episodePattern = Pattern.compile("(?<=episode\":)\\d*");
            Matcher episodeMatcher = episodePattern.matcher(jsonString);
            if (episodeMatcher.find()) {
             //   System.out.println(episodeMatcher.group());
            }

            Pattern showPattern = Pattern.compile("(?<=showtitle\":\").*(?=\",\"stream)");
            Matcher showMatcher = showPattern.matcher(jsonString);

            if (showMatcher.find()) {
              //  System.out.println(showMatcher.group());
            }

            Pattern episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
            Matcher episodeNameMatcher = episodeNamePattern.matcher(jsonString);
            if (episodeNameMatcher.find()){
             //   System.out.println(episodeNameMatcher.group());
            }

            Pattern plotPattern = Pattern.compile("(?<=plot\":\").*(?=\",\"rating)");
            Matcher plotMatcher = plotPattern.matcher(jsonString);
            if (plotMatcher.find()) {
              //  System.out.println(plotMatcher.group());
            }

            if (showMatcher.find()){
                if (episodeMatcher.find()) {
                    videoDetailArray.add(showMatcher.group() + ": " + episodeNameMatcher.group());
                } else {
                    videoDetailArray.add(showMatcher.group());
                }
            } else if (episodeMatcher.find()) {
                videoDetailArray.add(episodeNameMatcher.group());
            }

            if (seasonMatcher.find() && episodeMatcher.find()) {
                videoDetailArray.add("Season: " + seasonMatcher.group() + "Episode: " + episodeMatcher.group());
            }

            super.onPostExecute(aVoid);
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
        return !(bitmapArrayList.isEmpty());
    }


    static ArrayList<String> getExtendedInfoStrings(){
        return videoDetailArray;

    }

    static ArrayList<Bitmap> getExtendedInfoBitmaps(){
        return bitmapArrayList;

    }

    static void getSubs() {
        new AsyncSubtitleMenu().execute();
    }

}


