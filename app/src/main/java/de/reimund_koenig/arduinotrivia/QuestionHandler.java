package de.reimund_koenig.arduinotrivia;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */
final public class QuestionHandler {

    //*****************************************************************************************
    //Statics
    //*****************************************************************************************
    private static ArrayList<Question> questions;
    private static Queue<Integer> QuestionQueue = new LinkedList<>();
    private static boolean nextQuestion = false;

    //*****************************************************************************************
    //Public static functions
    //*****************************************************************************************
    public static void questionQueueAdd(int i)
    {
        if(nextQuestion)
        {
            nextQuestion = false;
            PlayFragment.showQuestionOnScreen(questions.get(i));
        }else {
            QuestionQueue.add(i);
        }
    }

    public static void clearQuestionQueue()
    {
        QuestionQueue.clear();
    }

    public static void questionQueueGetNextQuestion()
    {
        //Wenn alle Fragen in der Queue gestellt wurden, verlange neue Frage, ansonsten Queue abarbeiten
        if(QuestionQueue.size() == 0)
        {
            //Neue Frage (bekommt jeder verbundene Mitspieler an seine Queue angehängt)
            nextQuestion = true;
            BluetoothHandler.sendNewQuestionRequest();
        }else {
            PlayFragment.showQuestionOnScreen(questions.get(QuestionQueue.element()));
            QuestionQueue.remove();
        }
    }

    public static int getNumberOfQuestions()
    {
        return  QuestionQueue.size();
    }

    public static int loadQuestions(Context c)
    {
        XmlPullParserFactory xmlFactoryObject;
        XmlPullParser quiz_xml_parser;
        try{
            xmlFactoryObject = XmlPullParserFactory.newInstance();
            quiz_xml_parser = xmlFactoryObject.newPullParser();
            InputStream inputStream = c.getAssets().open("questions.xml");
            quiz_xml_parser.setInput(inputStream, null);
            questions = new ArrayList<>();
            String question = "";
            String answer   = "";
            String gap1     = "";
            String gap2     = "";
            String gap3     = "";
            String text     = "";

            int event = quiz_xml_parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name = quiz_xml_parser.getName();
                switch (event) {
                    case XmlPullParser.TEXT:
                        text = quiz_xml_parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (name){
                            case "question":{
                                question = text;
                                break;
                            }
                            case "answer": {
                                answer = text;
                                break;
                            }
                            case "gap1": {
                                gap1 = text;
                                break;
                            }
                            case "gap2": {
                                gap2 = text;
                                break;
                            }
                            case "gap3": {
                                gap3 = text;
                                break;
                            }
                            case "query": {
                                questions.add(new Question(question,answer,gap1,gap2,gap3));
                                break;
                            }
                        }
                        break;
                }
                event = quiz_xml_parser.next();
            }
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Error: " + e.getMessage()) ;
        }
        return questions.size();
    }

}

//*****************************************************************************************
//End of File
//*****************************************************************************************