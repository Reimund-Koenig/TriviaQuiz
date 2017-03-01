package de.reimund_koenig.arduinotrivia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */
public class HighScoreListView extends ArrayAdapter<String> {


    //*****************************************************************************************
    //final private
    //*****************************************************************************************
    private final Context context;
    private final ArrayList<String> arrayList_score;
    private final ArrayList<String> arrayList_nickname;
    private final ArrayList<String> arrayList_sex;
    private final ArrayList<String> arrayList_hscPalce;

    //*****************************************************************************************
    //constructor of listview
    //*****************************************************************************************
    public HighScoreListView(Context context, ArrayList<String> arrayList_hscPlc, ArrayList<String> arrayList_nickname, ArrayList<String> arrayList_score, ArrayList<String> arrayList_sex) {
        super(context, R.layout.row_highscore_layout, arrayList_nickname);

        this.context = context;
        this.arrayList_hscPalce        = arrayList_hscPlc;
        this.arrayList_nickname        = arrayList_nickname;
        this.arrayList_sex             = arrayList_sex;
        this.arrayList_score           = arrayList_score;
    }

    //*****************************************************************************************
    //get view
    //*****************************************************************************************
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        rowView = inflater.inflate(R.layout.row_highscore_layout, parent, false);

        //declare Views
        TextView tv_nickname = (TextView) rowView.findViewById(R.id.hsc_nickname);
        TextView tv_score = (TextView) rowView.findViewById(R.id.hsc_score);
        TextView tv_place = (TextView) rowView.findViewById(R.id.hsc_place);

        ImageView male   = (ImageView) rowView.findViewById(R.id.hsc_male);
        ImageView female = (ImageView) rowView.findViewById(R.id.hsc_female);

        //Set text into view
        tv_place.setText(arrayList_hscPalce.get(position));
        tv_nickname.setText(arrayList_nickname.get(position));
        tv_score.setText(arrayList_score.get(position));

        //if female?
        if(arrayList_sex.get(position).toUpperCase().equals("FEMALE")) {
            male.setVisibility(View.GONE);
            female.setVisibility(View.VISIBLE);
        }else {
            male.setVisibility(View.VISIBLE);
            female.setVisibility(View.GONE);
        }

        return rowView;
    }
}

//*****************************************************************************************
//End of File
//*****************************************************************************************