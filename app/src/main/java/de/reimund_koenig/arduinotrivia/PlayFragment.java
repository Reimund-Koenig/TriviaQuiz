package de.reimund_koenig.arduinotrivia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class PlayFragment extends Fragment{

    //*****************************************************************************************
    // Statics
    //*****************************************************************************************
    private static View v;
    private static Random random = new Random();
    private static char actual_answer;
    private static char selected_answer;
    private static String score;


    //*****************************************************************************************
    // Private Vars
    //*****************************************************************************************
    private ObjectAnimator colorFadeInAnswerButtons;

    private ObjectAnimator colorFadeInTheRightAnswer;
    private ObjectAnimator colorFadeOutAnswerButtonRight;
    private ObjectAnimator colorFadeOutAnswerButtonFalse;

    private Button btn_answerA;
    private Button btn_answerB;
    private Button btn_answerC;
    private Button btn_answerD;

    private Button btn_startQuizGame;
    private TextView tv_score;

    //*****************************************************************************************
    // Public functions
    //*****************************************************************************************
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_play, container, false);
        //get number of questions and parse XML
        int number_of_questions = QuestionHandler.loadQuestions(getActivity());
        BluetoothHandler.sendNumberOfQuestions(number_of_questions);

        //Declare Buttons
        btn_answerA   = (Button) view.findViewById(R.id.btn_answerA);
        btn_answerB   = (Button) view.findViewById(R.id.btn_answerB);
        btn_answerC   = (Button) view.findViewById(R.id.btn_answerC);
        btn_answerD   = (Button) view.findViewById(R.id.btn_answerD);

        //Set Clickable false until the first question is loaded
        btn_answerA.setClickable(false);
        btn_answerB.setClickable(false);
        btn_answerC.setClickable(false);
        btn_answerD.setClickable(false);

        //Get Name and Sex and send it to Quizserver
        TextView tv_name = (TextView) view.findViewById(R.id.tv_name_text);
        tv_score  = (TextView) view.findViewById(R.id.tv_score_text);

        SharedPreferences settings = getActivity().getSharedPreferences(Constants.ARDUINO_TRIVIA_GAME_SHARED_PREFERENCES, 0);

        final String sex = settings.getBoolean(Constants.SEX, false)?"Female":"Male";
        final String nickname = settings.getString(Constants.USERNAME, "");

        //Send Name and Sex
        BluetoothHandler.sendNickname(nickname);
        BluetoothHandler.sendSex(sex);

        //Set Textbox above the question
        if(settings.getBoolean(Constants.SEX, false))
        {
            tv_name.setText(nickname + " ♀");
        }else{
            tv_name.setText(nickname + " ♂");
        }

        //Set Btn Start into view
        btn_startQuizGame   = (Button) view.findViewById(R.id.btn_startQuizGame);
        btn_startQuizGame.setVisibility(View.VISIBLE);

        //Set GAME invisible
        final LinearLayout ll_quizGame   = (LinearLayout) view.findViewById(R.id.ll_quizGame);
        ll_quizGame.setVisibility(View.INVISIBLE);

        //Set view for the fragment
        v = view;

        //Load Question and set BTN enable
        QuestionHandler.questionQueueGetNextQuestion();

        //Button "Start Game"
        btn_startQuizGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_startQuizGame.setVisibility(View.INVISIBLE);
                ll_quizGame.setVisibility(View.VISIBLE);
            }
        });

        //Button "Answer A"
        btn_answerA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selected_answer = 'A';
                fadeInAnswerButton();
            }
        });

        //Button "Answer B"
        btn_answerB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selected_answer = 'B';
                fadeInAnswerButton();
            }
        });

        //Button "Answer C"
        btn_answerC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selected_answer = 'C';
                fadeInAnswerButton();
            }
        });

        //Button "Answer D"
        btn_answerD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selected_answer = 'D';
                fadeInAnswerButton();
            }
        });

        return view;
    }

    //*****************************************************************************************
    // Public static functions
    //*****************************************************************************************
    //Create new instance
    public static PlayFragment newInstance(String title) {

        PlayFragment pageFragment = new PlayFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        pageFragment.setArguments(bundle);
        score = "0";
        return pageFragment;
    }

    //If game ends and there are free slots -> try to start new game
    public static void tryToStartNewGame()
    {
        try {
            Log.d("Trivia Bluetooth", "Start Dialog Builder");
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Log.d("Trivia Bluetooth", "Neues Spiel");

                            final Button btn_startQuizGame   = (Button) v.findViewById(R.id.btn_startQuizGame);
                            final LinearLayout ll_quizGame   = (LinearLayout) v.findViewById(R.id.ll_quizGame);
                            final TextView tv_score   = (TextView) v.findViewById(R.id.tv_score_text);


                            btn_startQuizGame.setVisibility(View.VISIBLE);
                            ll_quizGame.setVisibility(View.INVISIBLE);
                            tv_score.setText("0");

                            //Load Question
                            QuestionHandler.questionQueueGetNextQuestion();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            HighScoreFragment.showHighScoreAndFinish();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setMessage("Möchten Sie erneut Spiel?")
                    .setNegativeButton("Nein", dialogClickListener)
                    .setPositiveButton("Ja", dialogClickListener)
                    .show();

        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Fehler Dialog builder: " + e.getMessage());
            HighScoreFragment.showHighScoreAndFinish();
        }
    }

    //Show or Hide the Box "Neue Nachricht"
    public static void showNewMSGIncoming(boolean b){
        final TextView incomnig_MSG = (TextView) v.findViewById(R.id.tv_chat_msg_incoming);
        if(b)
        {
            //Show Incoming MSG TextView
            incomnig_MSG.setVisibility(View.VISIBLE);
        }else{
            //Hide Incoming MSG TextView
            incomnig_MSG.setVisibility(View.GONE);
        }
    }

    //Set the score (int to string)
    public static void setScore(int i)
    {
        final TextView tv_score_tmp  = (TextView) v.findViewById(R.id.tv_score_text);
        score = Integer.valueOf(i).toString();
        Log.d("Trivia Bluetooth", "Set Score: " + score);
        tv_score_tmp.setText(score);
    }

    //returns the score as integer
    public static int getScore()
    {
        int sc = Integer.parseInt(score);
        Log.d("Trivia Bluetooth", "Get Score: " + sc);
        return sc;
    }

    //Loads the Question and the answers into view and set answer-buttons to enable
    //Answers gets randomly loaded into answer-buttons
    public static void showQuestionOnScreen(Question input)
    {
        try {
            final TextView tv_question = (TextView) v.findViewById(R.id.tv_QuizQuestion);

            final Button btn_answerA = (Button) v.findViewById(R.id.btn_answerA);
            final Button btn_answerB = (Button) v.findViewById(R.id.btn_answerB);
            final Button btn_answerC = (Button) v.findViewById(R.id.btn_answerC);
            final Button btn_answerD = (Button) v.findViewById(R.id.btn_answerD);

            tv_question.setText(input.getQuestion());

            Log.d("Trivia Bluetooth", "Lade Frage (" + input.getAnswer() + ")");
            //Answers gets randomly loaded into answer-buttons
            switch (random.nextInt(4)) {
                //Set Right answer to A
                case 0: {
                    btn_answerA.setText(input.getAnswer());
                    btn_answerB.setText(input.getGap1());
                    btn_answerC.setText(input.getGap2());
                    btn_answerD.setText(input.getGap3());
                    actual_answer = 'A';
                    break;
                }

                //Set Right answer to B
                case 1: {
                    btn_answerA.setText(input.getGap1());
                    btn_answerB.setText(input.getAnswer());
                    btn_answerC.setText(input.getGap2());
                    btn_answerD.setText(input.getGap3());
                    actual_answer = 'B';
                    break;
                }

                //Set Right answer to C
                case 2: {
                    btn_answerA.setText(input.getGap1());
                    btn_answerB.setText(input.getGap2());
                    btn_answerC.setText(input.getAnswer());
                    btn_answerD.setText(input.getGap3());
                    actual_answer = 'C';
                    break;
                }

                //Set Right answer to D
                case 3: {
                    btn_answerA.setText(input.getGap1());
                    btn_answerB.setText(input.getGap2());
                    btn_answerC.setText(input.getGap3());
                    btn_answerD.setText(input.getAnswer());
                    actual_answer = 'D';
                    break;
                }
            }
            //Aktiviere die Buttons
            btn_answerA.setClickable(true);
            btn_answerB.setClickable(true);
            btn_answerC.setClickable(true);
            btn_answerD.setClickable(true);
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "showQuestionOnScreen: " + e.getMessage());
        }
    }

    //*****************************************************************************************
    // Public static functions
    //*****************************************************************************************
    //reset to the standard XML file for the Buttons << gets called by Animation Handler
    public void resetButtons(){
        try {
            btn_answerA.setBackgroundColor(v.getResources().getColor(R.color.transparent));
            btn_answerB.setBackgroundColor(v.getResources().getColor(R.color.transparent));
            btn_answerC.setBackgroundColor(v.getResources().getColor(R.color.transparent));
            btn_answerD.setBackgroundColor(v.getResources().getColor(R.color.transparent));

            btn_answerA.setBackground(getResources().getDrawable(R.drawable.btn_answers));
            btn_answerB.setBackground(getResources().getDrawable(R.drawable.btn_answers));
            btn_answerC.setBackground(getResources().getDrawable(R.drawable.btn_answers));
            btn_answerD.setBackground(getResources().getDrawable(R.drawable.btn_answers));
        }catch(Exception e){
            Log.d("Trivia Bluetooth", "Fehler resetButtons: " + e.getMessage());
        }

    }

    //Sets the color of a button << gets called by Animation Handler
    public void setButtonColor(char choice, int color )
    {

        try {
            switch (choice) {
                case 'A': {
                    btn_answerA.setBackgroundColor(color);
                    break;
                }
                case 'B': {
                    btn_answerB.setBackgroundColor(color);
                    break;
                }
                case 'C': {
                    btn_answerC.setBackgroundColor(color);
                    break;
                }
                case 'D': {
                    btn_answerD.setBackgroundColor(color);
                    break;
                }
                default:
                    break;
            }
        }catch (Exception e)
        {

            Log.d("Trivia Bluetooth", "Fehler SC: " + e.getMessage());
        }
    }

    //*****************************************************************************************
    // Private functions
    //*****************************************************************************************
    //Fade in then one Answer where selected by the user
    private void fadeInAnswerButton()
    {
        try {
            if(colorFadeInAnswerButtons != null && colorFadeInAnswerButtons.isRunning())
            {
                colorFadeInAnswerButtons.cancel();
            }
            resetButtons();
            switch (selected_answer) {
                case 'A': {
                    colorFadeInAnswerButtons = ObjectAnimator.ofObject(btn_answerA, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_2), v.getResources().getColor(R.color.quiz_1));
                    break;
                }
                case 'B': {
                    colorFadeInAnswerButtons = ObjectAnimator.ofObject(btn_answerB, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_2), v.getResources().getColor(R.color.quiz_1));
                    break;
                }
                case 'C': {
                    colorFadeInAnswerButtons = ObjectAnimator.ofObject(btn_answerC, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_2), v.getResources().getColor(R.color.quiz_1));
                    break;
                }
                case 'D': {
                    colorFadeInAnswerButtons = ObjectAnimator.ofObject(btn_answerD, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_2), v.getResources().getColor(R.color.quiz_1));
                    break;
                }
                default:
                    break;
            }
            if(colorFadeInAnswerButtons.getDuration() != 2000)
            {
                colorFadeInAnswerButtons.addListener(new AnimatorListenerAdapter() {
                    private boolean mIsCanceled = false;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mIsCanceled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!mIsCanceled) {
                            btn_answerA.setClickable(false);
                            btn_answerB.setClickable(false);
                            btn_answerC.setClickable(false);
                            btn_answerD.setClickable(false);

                            if(selected_answer == actual_answer) {
                                setButtonColor(selected_answer, v.getResources().getColor(R.color.green));
                                fadeOutAnswerButtons();
                            }else{
                                setButtonColor(selected_answer, v.getResources().getColor(R.color.red));
                                fadeInTheRightAnswer();
                            }
                        }
                    }
                });
                colorFadeInAnswerButtons.setDuration(2000);
            }
            colorFadeInAnswerButtons.start();
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Fehler SC: " + e.getMessage());
        }
    }

    //Makes the right answer green
    private void fadeInTheRightAnswer()
    {
        try {
            switch (actual_answer) {
                case 'A': {
                    colorFadeInTheRightAnswer = ObjectAnimator.ofObject(btn_answerA, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_3), v.getResources().getColor(R.color.green));
                    break;
                }
                case 'B': {
                    colorFadeInTheRightAnswer = ObjectAnimator.ofObject(btn_answerB, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_3), v.getResources().getColor(R.color.green));
                    break;
                }
                case 'C': {
                    colorFadeInTheRightAnswer = ObjectAnimator.ofObject(btn_answerC, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_3), v.getResources().getColor(R.color.green));
                    break;
                }
                case 'D': {
                    colorFadeInTheRightAnswer = ObjectAnimator.ofObject(btn_answerD, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.quiz_3), v.getResources().getColor(R.color.green));
                    break;
                }
                default:
                    break;
            }
            if(colorFadeInTheRightAnswer.getDuration() != 1420)
            {
                colorFadeInTheRightAnswer.addListener(new AnimatorListenerAdapter() {
                    private boolean mIsCanceled = false;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mIsCanceled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!mIsCanceled) {
                            fadeOutAnswerButtons();
                        }
                    }
                });
                colorFadeInTheRightAnswer.setDuration(1420);
            }
            colorFadeInTheRightAnswer.start();
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Fehler SC: " + e.getMessage());
        }
    }

    //Fades out all answer buttons which where colored before
    private void fadeOutAnswerButtons()
    {
        try {
            resetButtons();
            switch (actual_answer) {
                case 'A': {
                    colorFadeOutAnswerButtonRight = ObjectAnimator.ofObject(btn_answerA, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.green), v.getResources().getColor(R.color.quiz_3));
                    break;
                }
                case 'B': {
                    colorFadeOutAnswerButtonRight = ObjectAnimator.ofObject(btn_answerB, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.green), v.getResources().getColor(R.color.quiz_3));
                    break;
                }
                case 'C': {
                    colorFadeOutAnswerButtonRight = ObjectAnimator.ofObject(btn_answerC, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.green), v.getResources().getColor(R.color.quiz_3));
                    break;
                }
                case 'D': {
                    colorFadeOutAnswerButtonRight = ObjectAnimator.ofObject(btn_answerD, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.green), v.getResources().getColor(R.color.quiz_3));
                    break;
                }
                default:
                    break;
            }
            //Bei falscher Antwort -> Beide ausblenden
            if(selected_answer != actual_answer) {
                switch (selected_answer) {
                    case 'A': {
                        colorFadeOutAnswerButtonFalse = ObjectAnimator.ofObject(btn_answerA, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.red), v.getResources().getColor(R.color.quiz_3));
                        break;
                    }
                    case 'B': {
                        colorFadeOutAnswerButtonFalse = ObjectAnimator.ofObject(btn_answerB, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.red), v.getResources().getColor(R.color.quiz_3));
                        break;
                    }
                    case 'C': {
                        colorFadeOutAnswerButtonFalse = ObjectAnimator.ofObject(btn_answerC, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.red), v.getResources().getColor(R.color.quiz_3));
                        break;
                    }
                    case 'D': {
                        colorFadeOutAnswerButtonFalse = ObjectAnimator.ofObject(btn_answerD, "backgroundColor", new ArgbEvaluator(), v.getResources().getColor(R.color.red), v.getResources().getColor(R.color.quiz_3));
                        break;
                    }
                    default:
                        break;
                }
                if (colorFadeOutAnswerButtonFalse.getDuration() != 1420) {
                    colorFadeOutAnswerButtonFalse.setDuration(1420);
                }
                colorFadeOutAnswerButtonFalse.start();
            }



            if(colorFadeOutAnswerButtonRight.getDuration() != 1600)
            {
                colorFadeOutAnswerButtonRight.addListener(new AnimatorListenerAdapter() {
                    private boolean mIsCanceled = false;

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mIsCanceled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!mIsCanceled) {
                            if(actual_answer == selected_answer)
                            {
                                Log.d("Trivia Bluetooth", "Next Question");
                                resetButtons();
                                QuestionHandler.questionQueueGetNextQuestion();
                                score = tv_score.getText().toString();
                                setScore(getScore() + 100);
                                Log.d("Trivia Bluetooth", "Weiter");
                            }else
                            {
                                //Verloren...
                                score = tv_score.getText().toString();
                                BluetoothHandler.sendScore(score);
                                setScore(0);
                                BluetoothHandler.sendGetCountConnected();
                            }
                        }
                    }
                });
                colorFadeOutAnswerButtonRight.setDuration(1600);
            }
            colorFadeOutAnswerButtonRight.start();
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Fehler SC: " + e.getMessage());
        }
    }
}
//*****************************************************************************************
//End of File
//*****************************************************************************************