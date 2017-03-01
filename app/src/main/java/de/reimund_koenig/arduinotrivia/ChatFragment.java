package de.reimund_koenig.arduinotrivia;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class ChatFragment extends Fragment {

    //*****************************************************************************************
    //private statics
    //*****************************************************************************************
    private static ArrayList<String> chatArrayContent;
    private static ArrayList<String> chatArrayNickname;
    private static ArrayList<String> chatArraySex;
    private static Context c;
    private static View v;
    private static ListView chatListView;


    //*****************************************************************************************
    //constructor
    //*****************************************************************************************
    public ChatFragment()
    {
        chatArrayContent = new ArrayList<>();
        chatArrayNickname = new ArrayList<>();
        chatArraySex = new ArrayList<>();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatArrayContent = new ArrayList<>();
        chatArrayNickname = new ArrayList<>();
        chatArraySex = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        final ImageButton btn_sendMSG   = (ImageButton) view.findViewById(R.id.btn_sendMSG);
        final EditText chatEdit         = (EditText)    view.findViewById(R.id.chatEdit);
        chatListView                    = (ListView)    view.findViewById(R.id.chatList);
        v = view;
        c = view.getContext();
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.ARDUINO_TRIVIA_GAME_SHARED_PREFERENCES, 0);

        final String sex = settings.getBoolean(Constants.SEX, false)?"Female":"Male";

        final ChatListView chatListViewAdapter = new ChatListView(getActivity(), chatArrayContent, chatArrayNickname, chatArraySex); //Empty String stands for own message

        //chatListView.setAdapter(chatAdapter);
        chatListView.setAdapter(chatListViewAdapter);

        //Send Message Button OnClick
        btn_sendMSG.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btn_sendMSG.setBackgroundColor(getResources().getColor(R.color.quiz_2));
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        //Send Message
                        String tmp = chatEdit.getText().toString();
                        if(tmp.length() > 0) {
                            BluetoothHandler.sendMessage(tmp);
                            //  MainActivity
                            //get Sex, Name and Content of Message
                            chatArrayContent.add(tmp);  //Message
                            chatArrayNickname.add(""); //No Nickname = own message
                            chatArraySex.add(sex);      //Own Sex

                            //Create temporary ListViewAdapter
                            //Empty String stands for own message
                            final ChatListView chatListViewAdapter = new ChatListView(getActivity(), chatArrayContent, chatArrayNickname, chatArraySex);
                            //Set LSA into view
                            chatListView.setAdapter(chatListViewAdapter);

                            //Scroll to bottom of listview
                            chatListView.post(new Runnable() {
                                @Override
                                public void run() {
                                    chatListView.setSelection(chatListViewAdapter.getCount() - 1);
                                }
                            });

                        }
                        chatEdit.setText("");

                        btn_sendMSG.setBackgroundColor(getResources().getColor(R.color.quiz_1));
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });


        return view;
    }

    //*****************************************************************************************
    //public static funtions
    //*****************************************************************************************
    //create new instance
    public static ChatFragment newInstance(String title) {

        ChatFragment pageFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        pageFragment.setArguments(bundle);
        return pageFragment;

    }
    //Print a Message to the Chat fragment (called by BT-Handler)
    public static void printMessage(String [] input)
    {
        try {
            //get Sex, Name and Content of Message
            chatArraySex.add(input[0].toUpperCase());
            chatArrayNickname.add(input[1]);
            chatArrayContent.add(input[2]);

            //Create temporary ListViewAdapter
            //String stands for NOT own message
            final ChatListView chatListViewAdapter = new ChatListView(c, chatArrayContent, chatArrayNickname, chatArraySex);
            //Set LSA into view
            chatListView.setAdapter(chatListViewAdapter);

            //Scroll to bottom of listview
            chatListView.post(new Runnable() {
                @Override
                public void run() {
                    chatListView.setSelection(chatListViewAdapter.getCount() - 1);
                }
            });
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Error: " + e.getMessage());
        }
    }
}

//*****************************************************************************************
//End of File
//*****************************************************************************************