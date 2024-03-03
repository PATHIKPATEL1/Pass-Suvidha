package com.example.passsuvidha;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomShowPdfAdaptor extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] titles;
    private final String[] subtitles;

    public CustomShowPdfAdaptor(Activity context, String[] titles, String[] subtitles) {
        super(context, R.layout.custom_activity_show_pdf,titles);

        this.context=context;
        this.titles=titles;
        this.subtitles=subtitles;
    }


    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.custom_activity_show_pdf, null,true);
        TextView titleText = (TextView) rowView.findViewById(R.id.lbl_pass_pdf_name);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.lbl_pass_pdf_time);

        titleText.setText(titles[position]);
        subtitleText.setText(subtitles[position]);

        return rowView;

    };


}
