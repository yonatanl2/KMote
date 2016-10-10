package com.levyinc.android.kodimote;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


class ButtonActions {

    public static boolean status = false;
    private static String request;
    public static ArrayList<String> playerInfo = new ArrayList<>();
    private static int speed = 1;
    private static boolean isPaused = false;


    private static class AsynchConnect extends AsyncTask <Void, Void, Integer> {

        private Integer respCode;


        @Override
        protected Integer doInBackground(Void... params) {
                    try {
                        URL url = new URL(request);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        respCode = conn.getResponseCode();
                        if (respCode == 200) {
                            status = true;
                        } else {
                            status = false;
                        }
                    } catch (IOException exception) {
                        String stringException = exception.toString();
                        System.out.println(stringException);
                        if (stringException.contains("java.net.UnknownHostException")) {
                            status = false;
                        }
                    }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Main2Activity.statusUpdater(status);

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
            speed = speed * 2;
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }
    }

        static void fastForward() {
     new AsynchFastForward().execute();
    }

    private static class AsynchRollBack extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (speed > 1) {
                speed = speed / 2;
            } else if (speed <= 1) {
                speed = speed * (-2);
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                conn.disconnect();
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                conn.disconnect();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
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
            super.onPostExecute(aVoid);
        }
    }

        static void getInfo() {
            new AsynchInfoChecker().execute();
        }

    private static class AsynchPauseChecker extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.getProperties");
                jsonParam.put("id", 1);
                Pattern pattern2 = Pattern.compile("\\d$");
                Matcher matcher2 = pattern2.matcher(playerInfo.get(0));

                if (matcher2.find()) {
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(matcher2.group()));
                    jsonParam2.put("properties", "speed");
                    jsonParam.put("params", jsonParam2);
                }
                String jsonParamString = (jsonParam.toString().replaceAll("\"speed\"", "[\"speed\"]"));

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
                StringBuffer jsonString = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
                br.close();
                conn.disconnect();
                if (jsonString.toString().contains("{\"speed\":1}")) {
                    isPaused = false;
                } else {
                    isPaused = true;
                }
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

        static boolean isPaused() {
            new AsynchPauseChecker().execute();
            return isPaused;
    }

    static boolean playerInfoGotten() {
        return !(playerInfo.isEmpty());
    }
}
