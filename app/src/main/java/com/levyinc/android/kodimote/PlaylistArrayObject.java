package com.levyinc.android.kodimote;


public class PlaylistArrayObject {

    private String episodeName;
    private int[] numbers;

    public PlaylistArrayObject(String episodeName, int[] numbers) {
        this.episodeName = episodeName;
        this.numbers = numbers;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public int[] getNumbers() {
        return numbers;
    }


}
