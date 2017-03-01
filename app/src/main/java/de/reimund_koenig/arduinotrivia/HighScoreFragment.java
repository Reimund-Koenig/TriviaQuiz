package de.reimund_koenig.arduinotrivia;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class HighScoreFragment extends Fragment {

    //*****************************************************************************************
    //private statics
    //*****************************************************************************************
    private static ArrayList<String> arrayList_score;
    private static ArrayList<String> arrayList_nickname;
    private static ArrayList<String> arrayList_place;
    private static ArrayList<String> arrayList_sex;
    private static Context c;
    private static View v;
    private static Activity activity;
    private static ListView highScoreList;

    //*****************************************************************************************
    //constructor
    //*****************************************************************************************
    public HighScoreFragment()
    {
        arrayList_score = new ArrayList<>();
        arrayList_nickname = new ArrayList<>();
        arrayList_sex = new ArrayList<>();
        arrayList_place = new ArrayList<>();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        BluetoothHandler.sendHighScoreRequest();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_highscore, container, false);
        c = view.getContext();
        v = view;

        highScoreList = (ListView)    view.findViewById(R.id.highScoreList);

       final Button btn_refresh = (Button) view.findViewById(R.id.btn_refreshHighScore);

        //Start refesh Highscore
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetList();
                BluetoothHandler.sendHighScoreRequest();
            }
        });

        //Shows the actual Highscore
        showActualHighScore();
        return view;
    }


    //*****************************************************************************************
    //Public static functions
    //*****************************************************************************************
    //Create new instance
    public static HighScoreFragment newInstance(String title) {

        HighScoreFragment pageFragment = new HighScoreFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }
    //Clear actual Highscore View to rewrite it
    public static void resetList(){
        arrayList_nickname.clear();
        arrayList_score.clear();
        arrayList_sex.clear();
        arrayList_place.clear();
    }

    //Adds a Score to the Highscorelist
    public static void setOneScore(int place, String s_nickname, String s_sex, String s_score)
    {
        arrayList_nickname.add(s_nickname);
        arrayList_score.add(s_score);
        arrayList_sex.add(s_sex);
        Integer p = place;
        arrayList_place.add(Integer.toString(p) + ".");
    }

    //Shows the actual Score
    public static void showActualHighScore()
    {
        final HighScoreListView highScoreListViewAdapter = new HighScoreListView(c, arrayList_place, arrayList_nickname, arrayList_score, arrayList_sex); //Empty String stands for own message
        highScoreList.setAdapter(highScoreListViewAdapter);
    }

    //Shows the Score, but changes the "refesh" Button into "close" Button -> Player can only exit from here
    //This is a protection, that other players can join the game!
    public static void showHighScoreAndFinish()
    {
        //First go to View!!!
        GameActivity.showHighScore();

        Log.d("Trivia Bluetooth", "showHighScoreAndFinish");
        //Reset the Highscore
        resetList();
        //Reset Question queue
        QuestionHandler.clearQuestionQueue();
        //Get actual highscore
        BluetoothHandler.sendHighScoreRequest();

        //Change the Button -> User can only exit (he will lose connection to server)
        final Button btn_refresh = (Button) v.findViewById(R.id.btn_refreshHighScore);
        btn_refresh.setText("Beenden");

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        });
        //leave the game Save
        BluetoothHandler.sendLeaveGame();

        GameActivity.showHighScore();
    }
}

//*****************************************************************************************
//End of File
//*****************************************************************************************