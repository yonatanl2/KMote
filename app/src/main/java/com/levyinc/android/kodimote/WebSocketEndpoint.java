package com.levyinc.android.kodimote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.NotYetConnectedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;


class WebSocketEndpoint {

    private WebSocketClient webSocketClient;
    private Handler msgHandler = new Handler();
    private String responseMessage;
    private int speed = 1;
    private String exceptionMessage;
    private String closeMessage;
    private String onOpenMessage;
    private boolean isPaused;
    private boolean isShuffled;
    private boolean isRepeat;
    private ArrayList<String> playerInfo = new ArrayList<>();
    private Lock lock = new ReentrantLock();



    WebSocketEndpoint (String ip, String port) {
        try {
            URI uri = new URI("ws://" + ip + ":" + port + "/jsonrpc");
            System.out.println(uri);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    onOpenMessage = handshakedata.toString();
                }

                @Override
                public void onMessage(String message) {

                    System.out.println(message);
                    responseMessage = message;

                    Pattern typePattern = Pattern.compile("\\{\"id\":1,\"jsonrpc\":\"2\\.0\",\"result\":\\[\\{\"playerid\":\\d,\"type\":\"\\D*\"\\}\\]\\}");
                    Matcher typeMatcher = typePattern.matcher(message);
                    if (typeMatcher.find()) {
                        Pattern pattern = Pattern.compile("(?<=\"playerid\":)(\\d)");
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            playerInfo.add(0, matcher.group());
                        } else {
                            playerInfo = new ArrayList<>();
                        }


                        Pattern pattern2 = Pattern.compile("\"type\":\"(\\D*)\"");
                        Matcher matcher2 = pattern2.matcher((message));
                        if (matcher2.find()) {
                            playerInfo.add(1, matcher2.group());
                        } else {
                            playerInfo = new ArrayList<>();
                        }
                    }

                    Pattern speedPattern = Pattern.compile("(?<=speed\":)\\d");
                    Matcher speedMatcher = speedPattern.matcher(message);
                    if (speedMatcher.find()) {
                        isPaused = !(message.contains("\"speed\":1"));
                        speed = parseInt(speedMatcher.group());
                    }

                    if (message.contains("Player.OnStop")) {
                        playerInfo = new ArrayList<>();
                    }

                    Pattern totalTimePattern = Pattern.compile("(?<=totaltime\":\\{\").*(?=\\},\")");
                    Matcher totalTimeMatcher = totalTimePattern.matcher(message);

                    Pattern elaspedTimePattern = Pattern.compile("(?<=time\":\\{\").*(?=\\},\")");
                    Matcher elaspedTimeMatcher = elaspedTimePattern.matcher(message);

                    if (totalTimeMatcher.find()) {
                        Pattern timePattern = Pattern.compile("\\d+");
                        Matcher timeMatcher = timePattern.matcher(totalTimeMatcher.group());

                        ArrayList<Integer> timeArray = new ArrayList<>();
                        while (timeMatcher.find()) {
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
                            while (timeMatcher.find()) {
                                timeArray.add(parseInt(timeMatcher.group()));
                            }
                            if (timeArray.toArray().length > 0) {
                                ExtendedControlActivity.elaspedTime = TimeUnit.MILLISECONDS.convert(timeArray.get(0), TimeUnit.HOURS) +
                                        TimeUnit.MILLISECONDS.convert(timeArray.get(2), TimeUnit.MINUTES) +
                                        TimeUnit.MILLISECONDS.convert(timeArray.get(3), TimeUnit.SECONDS);
                            }
                        }
                    }

                    Pattern subtitlePattern = Pattern.compile("(?<=subtitles\":\\[\\{).*?(?=\"\\}\\])");
                    Matcher subtitleMatcher = subtitlePattern.matcher(message);

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
                    Matcher subtitleEnabledMatcher = subtitleEnabledPattern.matcher(message);

                    if (subtitleEnabledMatcher.find()) {
                        ExtendedControlActivity.setSubtitleEnabled((subtitleEnabledMatcher.group().equals("true")));
                    }

                    Pattern audioStreamPattern = Pattern.compile("(?<=audiostreams\":\\[\\{).*?(?=\"\\}\\])");
                    Matcher audioStreamMatcher = audioStreamPattern.matcher(message);


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

                    Pattern seasonPattern = Pattern.compile("(?<=season\":)\\d*");
                    Matcher seasonMatcher = seasonPattern.matcher(message);

                    Pattern episodePattern = Pattern.compile("(?<=episode\":)\\d*");
                    Matcher episodeMatcher = episodePattern.matcher(message);

                    Pattern showPattern = Pattern.compile("(?<=showtitle\":\").*(?=\",\"stream)");
                    Matcher showMatcher = showPattern.matcher(message);

                    Pattern episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
                    Matcher episodeNameMatcher = episodeNamePattern.matcher(message);

                    Pattern plotPattern = Pattern.compile("(?<=plot\":\").*(?=\",\"rating)");
                    Matcher plotMatcher = plotPattern.matcher(message);

                    Pattern seriesImagePattern = Pattern.compile("(?<=\"poster\":\"image://)http.*(?=/\",\"season)");
                    Matcher seriesMatch = seriesImagePattern.matcher(message);

                    Pattern imagePattern = Pattern.compile("(?<=thumbnail\":\"image://)http.*(?=/\")");
                    Matcher imageMatcher = imagePattern.matcher(message);

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
                                    Bitmap decodedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
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
                                    Bitmap decodedImage2 = BitmapFactory.decodeStream(url.openStream());
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
                            Matcher castMatcher = castPattern.matcher(message);
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
                    }

                    if (showMatcher.find()) {
                        ExtendedControlActivity.setSeriesName(showMatcher.group());
                    }

                    episodeNamePattern = Pattern.compile("(?<=label\":\").*(?=\",\"plot)");
                    episodeNameMatcher = episodeNamePattern.matcher(message);

                    if (episodeNameMatcher.find()) {
                        ExtendedControlActivity.setEpisodeName(episodeNameMatcher.group());
                    }

                    Pattern scorePattern = Pattern.compile("(?<=rating\":)\\d\\.\\d*?(?=,)");
                    Matcher scoreMatcher = scorePattern.matcher(message);

                    if (scoreMatcher.find()) {
                        ExtendedControlActivity.setScore((double) Math.round(Float.parseFloat(scoreMatcher.group()) * 100) / 100);
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
                    if (responseMessage.contains("System.OnQuit")) {
                        webSocketClient.close();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println(reason);
                    closeMessage = reason;
                    onOpenMessage = null;
                    playerInfo = new ArrayList<>();
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                    exceptionMessage = ex.toString();

                }

                @Override
                public void send(String text) throws NotYetConnectedException {
                    super.send(text);
                }
            };
            webSocketClient.connect();
        } catch (Exception exception) {
            exception.printStackTrace();
            if (exception.toString().contains("java.net.UnknownHostException") || exception.toString().contains("java.net.ConnectException")) {
                Message msg = new Message();
                msg.obj = "Failed";
                msgHandler.sendMessage(msg);
                msgHandler.obtainMessage();

            }
        }
    }

    private Runnable downRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Down");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };

    void down() {
        msgHandler.post(downRun);
    }

    private Runnable upRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Up");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };

    void up() {
        msgHandler.post(upRun);
    }

    private Runnable rightRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Right");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };

    void right() {
        msgHandler.post(rightRun);
    }


    private Runnable leftRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Left");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };

    void left() {
        msgHandler.post(leftRun);
    }

    private Runnable selectRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Select");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };


    void select() {
        msgHandler.post(selectRun);
    }


    private Runnable backRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Back");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };

    void back() {
        msgHandler.post(backRun);
    }

    private Runnable homeRun = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Home");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    };


    void home(){
        msgHandler.post(homeRun);
    }

    String getCloseMessage() {
        return closeMessage;
    }

    String getOnOpenMessage() {
        return onOpenMessage;
    }

    void disconnect() {
        webSocketClient.close();
    }

    private class PowerAction implements Runnable {

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
                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException | WebsocketNotConnectedException exception) {
                exception.printStackTrace();
            }
        }
    }

    private PowerAction powerActionShutdown = new PowerAction("System.Shutdown");
    private PowerAction powerActionReboot = new PowerAction("System.Reboot");

    void powerButton(String method) {
        if (method.equals("shutdown")) {
            msgHandler.post(powerActionShutdown);
        } else if (method.equals("reboot")) {
            msgHandler.post(powerActionReboot);
        }
    }

    private Runnable infoChecker = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault());

            Log.i("Info Checker", format.format(cal.getTime()));
            Log.i("Info Checker", "Locking");
            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Player.GetActivePlayers");
                jsonParam.put("id", 1);

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

                if (playerInfo.size() > 0) {

                    jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.getProperties");
                    jsonParam.put("id", 1);
                    JSONObject jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(playerInfo.get(0)));

                    String properties = "[\"speed\", \"time\", \"percentage\", \"subtitles\", \"subtitleenabled\", \"audiostreams\", \"totaltime\", \"type\"]";
                    jsonParam2.put("properties", properties);
                    jsonParam.put("params", jsonParam2);

                    String jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "["));
                    jsonParamString = jsonParamString.replaceAll("]\"", "]");
                    jsonParamString = jsonParamString.replaceAll("\\\\", "");

                    bytes = jsonParamString.getBytes();
                    webSocketClient.send(bytes);

                    jsonParam = new JSONObject();
                    jsonParam.put("jsonrpc", "2.0");
                    jsonParam.put("method", "Player.getItem");
                    jsonParam.put("id", "VideoGetItem");
                    jsonParam2 = new JSONObject();
                    jsonParam2.put("playerid", parseInt(playerInfo.get(0)));
                    properties = "[plot\", \"rating\", \"art\", \"cast\", \"title\" ,\"album\", \"artist\", \"season\", \"episode\", \"duration\", \"showtitle\", \"tvshowid\", \"thumbnail\", \"file\", \"fanart\", \"streamdetails]";
                    jsonParam2.put("properties", properties);
                    jsonParam.put("params", jsonParam2);

                    jsonParamString = (jsonParam.toString().replaceAll("\"\\[", "[\""));
                    jsonParamString = (jsonParamString.replaceAll("\\]\"", "\"]"));
                    jsonParamString = (jsonParamString.replaceAll("\\\\", ""));

                    bytes = jsonParamString.getBytes();
                    webSocketClient.send(bytes);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                exceptionMessage = exception.getLocalizedMessage();
            } finally {
                if (exceptionMessage != null) {
                    System.out.println("Exception: " + exceptionMessage);
                }
                Log.i("Info Checker", "Unlocking");
                lock.unlock();
            }
        }
    };

    void getInfo() {
        msgHandler.post(infoChecker);
    }

    boolean playerInfoGotten() {
        return (!(playerInfo.isEmpty()) && playerInfo.toArray().length > 0);
    }

    boolean pauseStatus() {
        return isPaused;
    }

    private Runnable fastForward = new Runnable() {
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
                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void startFastForward() {
        msgHandler.post(fastForward);
    }

    private Runnable rollBack = new Runnable() {
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

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void startRollBack() {
        msgHandler.post(rollBack);
    }

    private Runnable playPause = new Runnable() {
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

                    byte[] bytes = jsonParam.toString().getBytes();
                    webSocketClient.send(bytes);

                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }
    };

    void startPlayPause() {
        msgHandler.post(playPause);
    }

    private Runnable stop = new Runnable() {
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

                byte[] bytes = jsonParam.toString().getBytes();
                webSocketClient.send(bytes);

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void startStop() {
        msgHandler.post(stop);
    }



   private class SubtitleAction implements Runnable {

        int index;

       SubtitleAction (int index) {
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

                        webSocketClient.send(jsonParam.toString().getBytes());
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
                    webSocketClient.send(jsonParam.toString().getBytes());

                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    void startSubtitleAction(int index) {
        msgHandler.post(new SubtitleAction(index));
    }

    private class ExecuteAction implements Runnable {

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
                webSocketClient.send(jsonParam.toString().getBytes());


            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    void sync(String action) {
        msgHandler.post(new ExecuteAction(action));
    }


    private class AsynchAudioStreamAction implements Runnable {

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

                    webSocketClient.send(jsonParam.toString().getBytes());
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    void audiStreamAction(int index) {
        msgHandler.post(new AsynchAudioStreamAction(index));
    }

    void volumeAction(String action){
        msgHandler.post(new ExecuteAction(action));
    }


    private class Seek implements Runnable {

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

                    webSocketClient.send(jsonParam.toString().getBytes());
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    void playerSeek (float percentage) {
        msgHandler.post(new Seek(percentage));
    }

    private Runnable subtitleMenu = new Runnable() {

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
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void getSubs() {
       msgHandler.post(subtitleMenu);
    }

    private class SetVolumePercentage implements Runnable {

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

                webSocketClient.send(jsonParam.toString().getBytes());

            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    void volumePercentageSetter(int percentage){
        msgHandler.post(new SetVolumePercentage(percentage));
    }

    private Runnable muteAction = new Runnable() {

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
                webSocketClient.send(jsonParam.toString().getBytes());


            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void muteButton() {
        msgHandler.post(muteAction);
    }

    private Runnable shuffle = new Runnable() {

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
                    webSocketClient.send(jsonParam.toString().getBytes());
                    isShuffled = !isShuffled;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void setIsShuffled() {
        msgHandler.post(shuffle);
    }

    private Runnable repeat = new Runnable() {

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
                    webSocketClient.send(jsonParam.toString().getBytes());

                    isRepeat = !isRepeat;
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void setIsRepeat() {
        msgHandler.post(repeat);
    }

    private Runnable getInfo = new Runnable() {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.Info");
                jsonParam.put("id", 1);
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void getInfoActino () {
        msgHandler.post(getInfo);
    }

    private Runnable contextMenuAction = new Runnable() {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "Input.ContextMenu");
                jsonParam.put("id", 1);
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void pressContextMenu() {
        msgHandler.post(contextMenuAction);
    }

    private Runnable toggleFullScreenAction = new Runnable() {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "GUI.SetFullScreen");
                jsonParam.put("id", 1);
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void toggleFullScreen() {
        msgHandler.post(toggleFullScreenAction);
    }

    private Runnable clearAudioLibraryAction = new Runnable() {

        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "AudioLibrary.Clean");
                jsonParam.put("id", 1);
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    private Runnable clearVideoLibraryAction = new Runnable() {


        @Override
        public void run() {
            try {

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("jsonrpc", "2.0");
                jsonParam.put("method", "VideoLibrary.Clean");
                jsonParam.put("id", 1);
                webSocketClient.send(jsonParam.toString().getBytes());
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    };

    void clearAudioLibrary() {
        msgHandler.post(clearAudioLibraryAction);
    }

    void clearVideoLibrary() {
        msgHandler.post(clearVideoLibraryAction);
    }

}

