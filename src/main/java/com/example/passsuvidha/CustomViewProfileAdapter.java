package com.example.passsuvidha;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomViewProfileAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] titles;
    private final String[] subtitles;

    public CustomViewProfileAdapter(Activity context, String[] titles, String[] subtitles) {
        super(context, R.layout.custom_view_profile_listview,titles);

        this.context=context;
        this.titles=titles;
        this.subtitles=subtitles;
    }


    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.custom_view_profile_listview, null,true);
        TextView titleText = (TextView) rowView.findViewById(R.id.lbl_title);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.lbl_subtitle);

        titleText.setText(titles[position]);
        subtitleText.setText(subtitles[position]);

        return rowView;

    };

}



