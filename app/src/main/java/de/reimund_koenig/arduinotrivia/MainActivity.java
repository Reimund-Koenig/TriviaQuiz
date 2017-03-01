package de.reimund_koenig.arduinotrivia;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class MainActivity extends Activity {

    //*****************************************************************************************
    //Statics
    //*****************************************************************************************
    private static int introduction_index = 0;

    //*****************************************************************************************
    //Protected
    //*****************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create Menu Buttons
        final ImageButton btn_start = (ImageButton) findViewById(R.id.btn_start);
        final ImageButton btn_settings = (ImageButton) findViewById(R.id.btn_settings);
        final ImageButton btn_introduction = (ImageButton) findViewById(R.id.btn_introduction);
        final ImageButton btn_close = (ImageButton) findViewById(R.id.btn_close);


        //*****************************************************************************************
        // If Device have no BT-Adapter
        if(!BluetoothHandler.bluetoothAvailable())
        {
            Toast.makeText(MainActivity.this, "Das Gerät unterstützt kein Bluetooth", Toast.LENGTH_LONG).show();
        }

        //****************************************************************************************************************
         //Start Button OnClick
        btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btn_start.setBackgroundColor(getResources().getColor(R.color.quiz_2));
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        //Start GameActivity
                        SharedPreferences settings = getSharedPreferences(Constants.ARDUINO_TRIVIA_GAME_SHARED_PREFERENCES, 0);
                        //If username isnt set -> first set username
                        if(settings.getString(Constants.USERNAME, "").equals(""))
                        {
                            Toast.makeText(MainActivity.this, "Bitte erst Nickname & Geschlecht eintragen!", Toast.LENGTH_LONG).show();
                            showSettings();
                        }else {

                            //if username is set and BT is off -> Start Bluetooth
                            if (!BluetoothHandler.getBluetoothAdapter().isEnabled()) {
                                //BT is disabled... start enable request
                                Intent startBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(startBT, 1);
                            } else {
                                //if username is set and BT is on -> Start Game
                                connectBluetoothAndStartGame();
                            }
                        }
                        btn_start.setBackgroundColor(getResources().getColor(R.color.transparent));
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });


        //****************************************************************************************************************
        //Setting Button OnClick
        btn_settings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btn_settings.setBackgroundColor(getResources().getColor(R.color.quiz_2));
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        btn_settings.setBackgroundColor(getResources().getColor(R.color.transparent));
                        showSettings();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        //****************************************************************************************************************
        //Introduction Button OnClick
        btn_introduction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btn_introduction.setBackgroundColor(getResources().getColor(R.color.quiz_2));
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        //Starts the introduction dialog
                        showIntroduction();
                        btn_introduction.setBackgroundColor(getResources().getColor(R.color.transparent));
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

        //****************************************************************************************************************
        //Close Button OnClick
        btn_close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        btn_close.setBackgroundColor(getResources().getColor(R.color.quiz_2));
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        btn_close.setBackgroundColor(getResources().getColor(R.color.transparent));
                        //Ends the application
                        finish();
                        System.exit(0);
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /********************************************************************************************************************************
     * Bluetooth Functions
     /*********************************************************************************************************************************/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            //Start BT
            connectBluetoothAndStartGame();
        } else {
            Log.d("Trivia Bluetooth", "Bitte erst BT aktivieren");
        }
    }

    private void connectBluetoothAndStartGame()
    {
        //Start BT handler
        BluetoothHandler.connectBluetoothAndStartGame(MainActivity.this);
    }

    /***
     * END OF Bluetooth Functions
     *********************************************************************************************************************************/

    /*********************************************************************************************************************************
     * SETTINGS
     /*********************************************************************************************************************************/
    protected void showSettings()
    {
        try {
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_settings_layout);

            // set the custom dialog components - text, image and button
            final EditText et_username = (EditText) dialog.findViewById(R.id.dialog_et_nickname);
            final Switch tb_sex = (Switch) dialog.findViewById(R.id.dialog_tb_sex);
            final Button dialogButtonSave = (Button) dialog.findViewById(R.id.btn_dialogSave);
            final Button dialogButtonAbort = (Button) dialog.findViewById(R.id.btn_dialogAbort);

            SharedPreferences settings = getSharedPreferences(Constants.ARDUINO_TRIVIA_GAME_SHARED_PREFERENCES, 0);

            et_username.setText(settings.getString(Constants.USERNAME, ""));
            et_username.setMaxEms(12);
            tb_sex.setChecked(settings.getBoolean(Constants.SEX,false));
            et_username.setSelection(et_username.getText().length());

            //Only allow Letters or Digits for Nickname
            InputFilter filter1 = new InputFilter() {
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetterOrDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
            };
            //Max 12 characters for nickname
            InputFilter filters2 = new InputFilter.LengthFilter(12);

            //use the filters
            et_username.setFilters(new InputFilter[] { filter1 , filters2 });

            //Protection against Handle Error! IMPORTANT!
            tb_sex.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Protection against Handle Error
                }
            });

            //OnTouch Save
            dialogButtonSave.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            dialogButtonSave.setBackgroundColor(getResources().getColor(R.color.darkgray));
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            dialogButtonSave.setBackgroundColor(getResources().getColor(R.color.gray));

                            String tmp_username = et_username.getText().toString();
                            if(tmp_username.equals("")) {
                                Toast.makeText(MainActivity.this, "Bitte einen Namen eintragen!", Toast.LENGTH_SHORT).show();
                            }else {
                                //Save sex and username
                                SharedPreferences settings = getSharedPreferences(Constants.ARDUINO_TRIVIA_GAME_SHARED_PREFERENCES, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                tmp_username = BluetoothHandler.replaceUmlaut(tmp_username);
                                editor.putString(Constants.USERNAME, tmp_username);
                                editor.putBoolean(Constants.SEX, tb_sex.isChecked());

                                // Commit the edits!
                                editor.apply();
                                dialog.dismiss();
                            }
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });

            //OnTouch Abort
            dialogButtonAbort.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            dialogButtonAbort.setBackgroundColor(getResources().getColor(R.color.darkgray));
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            //abort
                            dialogButtonAbort.setBackgroundColor(getResources().getColor(R.color.gray));
                            dialog.dismiss();
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });
            dialog.show();

        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Error: " + e.getMessage()) ;
        }
    }
    /***
     * END OF SETTINGS
     *********************************************************************************************************************************/

    /*********************************************************************************************************************************
     * INTRODUCTION
    /*********************************************************************************************************************************/
    protected void showIntroduction()
    {
        try {
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_introduction_layout);

            // set the custom dialog components - text, image and button
            final Button btn_dialogBack = (Button) dialog.findViewById(R.id.btn_dialogBack);
            final Button btn_dialogForward = (Button) dialog.findViewById(R.id.btn_dialogForward);
            final Button btn_dialogClose = (Button) dialog.findViewById(R.id.btn_dialogClose);
            final TextView tv_content = (TextView) dialog.findViewById(R.id.dialog_text);
            final TextView dialog_headline = (TextView) dialog.findViewById(R.id.dialog_headline);
            final TextView dialog_site = (TextView) dialog.findViewById(R.id.dialog_site);


            final ArrayList<String> headlines = new ArrayList<>();
            final ArrayList<String> textes = new ArrayList<>();

            //Add textes and headlines from string.xml
            headlines.add(getString(R.string.introduction_headline_abstract));
            textes.add(getString(R.string.introduction_text_abstract));

            headlines.add(getString(R.string.introduction_headline_start));
            textes.add(getString(R.string.introduction_text_start));

            headlines.add(getString(R.string.introduction_headline_chat));
            textes.add(getString(R.string.introduction_text_chat));

            headlines.add(getString(R.string.introduction_headline_game));
            textes.add(getString(R.string.introduction_text_game));

            headlines.add(getString(R.string.introduction_headline_highscore));
            textes.add(getString(R.string.introduction_text_highscore));

            dialog_site.setText("(" + (introduction_index+1) + "\\" + headlines.size() + ")");

            //OnClick "Weiter"
            btn_dialogForward.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            btn_dialogForward.setBackgroundColor(getResources().getColor(R.color.darkgray));
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            btn_dialogForward.setBackgroundColor(getResources().getColor(R.color.gray));

                            introduction_index++;
                            if(introduction_index >= headlines.size())
                                introduction_index = 0;
                            tv_content.setText(textes.get(introduction_index));
                            dialog_headline.setText(headlines.get(introduction_index));
                            dialog_site.setText("(" + (introduction_index+1) + "\\" + headlines.size() + ")");
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });

            //OnClick "Back"
            btn_dialogBack.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            btn_dialogBack.setBackgroundColor(getResources().getColor(R.color.darkgray));
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            btn_dialogBack.setBackgroundColor(getResources().getColor(R.color.gray));
                            introduction_index--;
                            if(introduction_index <0)
                                introduction_index = headlines.size()-1;
                            tv_content.setText(textes.get(introduction_index));
                            dialog_headline.setText(headlines.get(introduction_index));
                                    dialog_site.setText("(" + (introduction_index+1) + "\\" + headlines.size() + ")");
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });

            //OnClick "Schließen"
            btn_dialogClose.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            btn_dialogClose.setBackgroundColor(getResources().getColor(R.color.darkgray));
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            btn_dialogClose.setBackgroundColor(getResources().getColor(R.color.gray));
                            introduction_index = 0;
                            dialog.dismiss();
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });
            dialog.show();

        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Error: " + e.getMessage()) ;
        }
    }
    /***
     * END OF INTRODUCTION
     *********************************************************************************************************************************/
}

//*****************************************************************************************
//End of File
//*****************************************************************************************