package com.levyinc.android.kodimote;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


class PlaylistArrayAdapter extends BaseAdapter {

    private ArrayList<PlaylistArrayObject> objects;
    private TypedArray sColor;
    private int defaultColor;
    private Context context;
    private int layout;

    private class ViewHolder {
        TextView firstLetter;
        TextView episodeName;
        TextView episodeStats;
    }

    PlaylistArrayAdapter(Context context, ArrayList<PlaylistArrayObject> objects, int layout) {
        this.context = context;
        this.objects = objects;
        this.sColor = context.getResources().obtainTypedArray(R.array.letter_tile_colors);
        this.defaultColor = ContextCompat.getColor(context, R.color.default_tile_color);
        this.layout = layout;
    }

    public int getCount() {
        return objects.size();
    }

    public PlaylistArrayObject getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;
        if(convertView == null) {
            linearLayout = new LinearLayout(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(layout, linearLayout, true);
        } else {
            linearLayout = (LinearLayout) convertView;
        }
        TextView firstLetter = (TextView) linearLayout.findViewById(R.id.first_letter_playlist);
        TextView episodeName = (TextView) linearLayout.findViewById(R.id.episode_name_playlist);
        TextView episodeStats = (TextView) linearLayout.findViewById(R.id.element_stats_playlist);

        String firstLetterChar = String.valueOf(objects.get(position).getEpisodeName().charAt(0));
        firstLetter.setText(firstLetterChar);
        firstLetter.setBackgroundColor(sColor.getColor(Math.abs(objects.get(position).getEpisodeName().hashCode() % sColor.length()), defaultColor));
        episodeName.setText(objects.get(position).getEpisodeName());
        episodeStats.setText("Season: " + objects.get(position).getNumbers()[0] + " Episode: " + objects.get(position).getNumbers()[1]);
        return linearLayout;
    }
}
