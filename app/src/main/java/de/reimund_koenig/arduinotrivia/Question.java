package de.reimund_koenig.arduinotrivia;


/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class Question {

    //*****************************************************************************************
    //Private
    //*****************************************************************************************
    private String question;
    private String answer;
    private String gap1;
    private String gap2;
    private String gap3;

    //*****************************************************************************************
    //Constructor
    //*****************************************************************************************
    public Question(String q, String a, String g1, String g2, String g3)
    {
        question = q;
        answer   = a;
        gap1     = g1;
        gap2     = g2;
        gap3     = g3;
    }

    //*****************************************************************************************
    //Getter
    //*****************************************************************************************
    public String getQuestion()
    {
        return  question;
    }

    public String getAnswer()
    {
        return  answer;
    }

    public String getGap1()
    {
        return  gap1;
    }

    public String getGap2()
    {
        return  gap2;
    }

    public String getGap3()
    {
        return  gap3;
    }

}

//*****************************************************************************************
//End of File
//*****************************************************************************************