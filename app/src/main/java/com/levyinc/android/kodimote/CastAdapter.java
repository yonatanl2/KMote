package com.levyinc.android.kodimote;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

class CastAdapter extends BaseAdapter{

    private ArrayList<String> cast;
    private Context context;
    private int layout;
    private int boxheights;
    private TypedArray sColor;
    private int defaultColor;

    CastAdapter (Context context, ArrayList<String> cast, int layout, int layoutWidth) {
        this.cast = cast;
        this.context = context;
        this.layout = layout;
        this.boxheights = layoutWidth / 4;
        this.sColor = context.getResources().obtainTypedArray(R.array.letter_tile_colors);
        this.defaultColor = ContextCompat.getColor(context, R.color.default_tile_color);
    }

    @Override
    public int getCount() {
        return cast.size();
    }

    @Override
    public Object getItem(int position) {
        return cast.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            textView = (TextView) inflater.inflate(layout, parent, false);
        } else {
            textView = (TextView) convertView;
        }
        textView.setText(cast.get(position));
        textView.setHeight(boxheights);
        textView.setBackgroundColor(sColor.getColor(Math.abs(cast.get(position).hashCode() % sColor.length()), defaultColor));
        return textView;
    }

    ArrayList<String> getArrayList() {
        return cast;
    }
}
