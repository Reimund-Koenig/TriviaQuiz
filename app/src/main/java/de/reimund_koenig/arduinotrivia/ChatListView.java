package de.reimund_koenig.arduinotrivia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
public class ChatListView extends ArrayAdapter<String> {

    //*****************************************************************************************
    //final private
    //*****************************************************************************************
    private final Context context;
    private final ArrayList<String> content;
    private final ArrayList<String> nickname;
    private final ArrayList<String> sex;


    //*****************************************************************************************
    //constuctor
    //*****************************************************************************************
    public ChatListView(Context context, ArrayList<String> content, ArrayList<String> nickname, ArrayList<String> sex) {
        super(context, R.layout.row_chat_layout, content);

        this.context = context;
        this.content = content;
        this.sex = sex;
        this.nickname = nickname;
    }

    //*****************************************************************************************
    //Get View Function
    //*****************************************************************************************
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String tmp_content  = content.get(position);
        String tmp_nickname = nickname.get(position);
        String tmp_sex      = sex.get(position);
        View rowView;

        rowView = inflater.inflate(R.layout.row_chat_layout,null,false);

        //if sex isnt set--> it must be a Broadcast String
        if(tmp_sex.equals(""))
        {
            //Info Broadcast
            TextView textView = (TextView) rowView.findViewById(R.id.row_layout_info_text);

            LinearLayout lv_left = (LinearLayout)  rowView.findViewById(R.id.row_layout_left);
            LinearLayout lv_right = (LinearLayout)  rowView.findViewById(R.id.row_layout_right);
            LinearLayout lv_info = (LinearLayout)  rowView.findViewById(R.id.row_layout_info);
            lv_right.setVisibility(View.GONE);
            lv_left.setVisibility(View.GONE);
            lv_info.setVisibility(View.VISIBLE);

            textView.setText(tmp_content);

            //If nickname isnt set it is a message from the owner
        }else if(tmp_nickname.equals(""))
        {
            //Eigener Text (rechts)
            TextView textView = (TextView) rowView.findViewById(R.id.user_text_right);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.user_bubble_right);
            LinearLayout lv_left = (LinearLayout)  rowView.findViewById(R.id.row_layout_left);
            LinearLayout lv_right = (LinearLayout)  rowView.findViewById(R.id.row_layout_right);
            LinearLayout lv_info = (LinearLayout)  rowView.findViewById(R.id.row_layout_info);
            lv_info.setVisibility(View.GONE);
            lv_right.setVisibility(View.VISIBLE);
            lv_left.setVisibility(View.GONE);
            textView.setText(tmp_content);
            //set male/female picture
            if(tmp_sex.startsWith("F"))
            {
                imageView.setImageResource(R.drawable.bubble_right_f);
            }else{
                imageView.setImageResource(R.drawable.bubble_right_m);
            }
            //otherwise its a message from another player to the owner
        }else{
            //Nachricht von anderen (links)
            TextView textView = (TextView) rowView.findViewById(R.id.user_text_left);
            TextView textView2 = (TextView) rowView.findViewById(R.id.user_name_left);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.user_bubble_left);
            LinearLayout lv_left = (LinearLayout)  rowView.findViewById(R.id.row_layout_left);
            LinearLayout lv_right = (LinearLayout)  rowView.findViewById(R.id.row_layout_right);
            LinearLayout lv_info = (LinearLayout)  rowView.findViewById(R.id.row_layout_info);
            lv_right.setVisibility(View.GONE);
            lv_info.setVisibility(View.GONE);
            lv_left.setVisibility(View.VISIBLE);
            textView2.setText(tmp_nickname);
            textView.setText(tmp_content);
            //set male/female picture
            if(tmp_sex.startsWith("F"))
            {
                imageView.setImageResource(R.drawable.bubble_left_f);
            }else{
                imageView.setImageResource(R.drawable.bubble_left_m);
            }
        }

        return rowView;
    }
}
//*****************************************************************************************
//End of File
//*****************************************************************************************