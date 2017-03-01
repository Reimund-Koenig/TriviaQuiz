package de.reimund_koenig.arduinotrivia;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class GameActivity extends FragmentActivity {

    //*****************************************************************************************
    //private statics
    //*****************************************************************************************
    private static ViewPager quizViewPager;
    private static boolean b_showHighScore;
    private static int actual_position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Declare Variables
        QuizFragmentPagerAdapter quizFragmentPagerAdapter;
        b_showHighScore = false;
        setContentView(R.layout.activity_game);
        quizViewPager = (ViewPager) findViewById(R.id.quiz_view_pager);
        quizFragmentPagerAdapter = new QuizFragmentPagerAdapter(getSupportFragmentManager());
        quizViewPager.setAdapter(quizFragmentPagerAdapter);

        //Set actual_position and the Startscreen
        actual_position = 1;
        quizViewPager.setCurrentItem(actual_position);

        //Actions on user interaction
        quizViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                //Wenn von Chat (Screen 0) auf Quiz (Screen 1) gewechselt wird, wird die Soft-Tastatur geschlossen
                if(actual_position == 0 && position == 1) {
                    Log.d("Trivia Bluetooth", "Close Keyboard");
                    final InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(quizViewPager.getWindowToken(), 0);
                }else if(actual_position == 1 && position == 0)
                {
                    PlayFragment.showNewMSGIncoming(false);
                }
                //Wenn b_showHighScore true ist wird kein Fragmentwechsel mehr zugelassen
                if(b_showHighScore)
                {
                    quizViewPager.setCurrentItem(2);
                }
                actual_position = position;
            }
        });
    }


    @Override
    protected void onDestroy() {
        //Destroy the BT-Handlyer on leave
        Log.d("Trivia Bluetooth", "On Destroy");
        BluetoothHandler.DestroyBluetooth();
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        Log.d("Trivia Bluetooth", "On Pause");
        try {
            int score = PlayFragment.getScore();
            //End game onPause -> Make other Players join the game!
            if(BluetoothHandler.getBTConnected()) {
                if(score > 0) {
                    BluetoothHandler.sendScore(String.valueOf(score));
                    PlayFragment.setScore(0);
                }
                HighScoreFragment.showHighScoreAndFinish();
                Toast.makeText(getApplicationContext(), "Quiz wurde beendet",
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            Log.d("Trivia Bluetooth", "Fehler bei Beenden von GameActivity: " + e.getMessage());
        }
        super.onPause();
    }


    //*****************************************************************************************
    //The Fragment Adapter -> private
    //*****************************************************************************************
    private static class QuizFragmentPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments = new Fragment[] { ChatFragment.newInstance("Chat"), PlayFragment.newInstance("Quiz"), HighScoreFragment.newInstance("Highscore")};

        public QuizFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //Returns the fragment at index
        @Override
        public Fragment getItem(int index) {
            return fragments[index];
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Chat";
                case 1:
                    return "Game";
                case 2:
                    return "Highscore";
            }
            return "";
        }

        //returns the number of fragments
        @Override
        public int getCount() {
            return fragments.length;
        }

    }

    //*****************************************************************************************
    //public static functions
    //*****************************************************************************************
    public static int getActualIndex()
    {
        return actual_position;
    }

    public static void showHighScore()
    {
        quizViewPager.setCurrentItem(2);
        b_showHighScore = true;
    }
}

//*****************************************************************************************
//End of File
//*****************************************************************************************