package de.reimund_koenig.arduinotrivia;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

final public class BluetoothHandler {

    //*****************************************************************************************
    //Private Statics
    //*****************************************************************************************
    //BT adapter and socket
    private static BluetoothAdapter triviaBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static BluetoothSocket socket;

    //Connect BT Thread and Handler with delay time (max waiting time befor stop searching BT)
    private static Thread connectBluetoothAndStartGame_Thread;
    private static Handler listenDataHandler = new Handler();
    private static int handlerPostTime = 15000;

    //Dialog for connecting
    private static ProgressDialog connectBluetoothDialog;

    //Msg queue to handle messages
    private static Queue<String> msg_queue = new LinkedList<>();

    //ACK to check if the msg reaches the server
    private static String quizACK = "";

    //Booleans to stop some threads or control them
    private static boolean bTConnected = false;
    private static boolean isPairing = false;
    private static boolean receiveThreadStop;
    private static boolean sendThreadStop;

    //Read buffer -> all incoming chars goes there
    private static byte[] readBuffer;

    //Actual readBufferPosition
    private static int readBufferPosition;

    //The streams
    private static OutputStream outputStream;
    private static InputStream inputStream;


    //*****************************************************************************************
    //Public static functions to SEND DATA
    //*****************************************************************************************
    //Sends a Msg to other users
    public static void sendMessage(String msg)
    {
        msg = replaceUmlaut(msg);
        Log.d("Trivia Bluetooth", "Send Message: " + msg);
        msg = "~M" + msg + "^";
        msg_queue.add(msg);
    }

    //Send the Nickname to Server
    public static void sendNickname(String msg)
    {
        msg = replaceUmlaut(msg);
        Log.d("Trivia Bluetooth", "Send Nickname: " + msg);
        msg = "~N" + msg + "^";
        msg_queue.add(msg);
    }

    //Send Sex to Server
    public static void sendSex(String msg)
    {
        msg = replaceUmlaut(msg);
        Log.d("Trivia Bluetooth", "Send sex: " + msg);
        msg = "~S" + msg + "^";
        msg_queue.add(msg);
    }

    //Send the Number of Questions from question.xml to Server
    public static void sendNumberOfQuestions(int number)
    {
        String msg = number + "";
        msg = replaceUmlaut(msg);
        Log.d("Trivia Bluetooth", "Send number of questions: " + msg);
        msg = "~Q" + msg + "^";
        msg_queue.add(msg);
    }

    //Send a new Question Request --> All players get the same new question to their question stack!
    public static void sendNewQuestionRequest()
    {
        Log.d("Trivia Bluetooth", "Send New Question Request" );
        msg_queue.add("~R^");
        //Wenn msg_queue nach 3 Sekunden immernoch 0-> etwas ist schief gegangen -> versuche es erneut!
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(QuestionHandler.getNumberOfQuestions() == 0 && bTConnected)
                {
                    sendNewQuestionRequest();
                }
            }
        }, 3000);
    }

    //Send the Score to Server to get the Score into Highscore
    public static void sendScore(String sc)
    {
        Log.d("Trivia Bluetooth", "Send Score" );
        msg_queue.add("~H" + sc + "^");
    }

    //Send Leave Request --> After ACK the games finishs
    public static void sendLeaveGame()
    {
        Log.d("Trivia Bluetooth", "Leave the Game" );
        msg_queue.add("~L^");
    }

    //Send a new Highscore Request to reseive the Highscore from the Server
    public static void sendHighScoreRequest()
    {
        Log.d("Trivia Bluetooth", "Send Highscore Request" );
        msg_queue.add("~G^");
    }

    //Send a request to get the number of connected players
    public static void sendGetCountConnected()
    {
        Log.d("Trivia Bluetooth", "Get Connected Players Request" );
        String msg = "~C^";
        msg_queue.add(msg);
    }

    // Method for Debug - Send MSG to chat-server console
    /*
    public static void sendConsoleString(String msg)
    {
        msg = replaceUmlaut(msg);
        Log.d("Trivia Bluetooth", "Send Console String: " + msg);
        msg = "~W" + msg + "^";
        msg_queue.add(msg);
    }
    */


    //*****************************************************************************************
    //Public static functions OTHERS
    //*****************************************************************************************

    //A public Method to replace all "Umlaute": ä = ae, ...
    public  static String replaceUmlaut(String input)
    {
        input = input.replace("ü","ue");
        input = input.replace("Ü","Ue");
        input = input.replace("ö","oe");
        input = input.replace("Ö","Oe");
        input = input.replace("ä","ae");
        input = input.replace("Ä","Ae");
        input = input.replace("/","+");
        input = input.replace("|","+");
        input = input.replace("~","+");
        input = input.replace("^","+");
        input = input.replace("&","+");
        input = input.replace("'","+");
        input = input.replace("\"","+");
        input = input.replace("\\","+");

        //Only allow ASCII Letters
        input = input.replaceAll("[^\\x00-\\x7F]", "");

        return input;
    }


    //*****************************************************************************************
    //Public static functions for Bluetooth Handle
    //*****************************************************************************************
    //Checks the BT availability
    public static boolean bluetoothAvailable() {
        return (triviaBluetoothAdapter != null);
    }

    //Is BT connected?
    public static boolean getBTConnected()
    {
        return bTConnected;
    }

    //Returns the adapter itself
    public static BluetoothAdapter getBluetoothAdapter()
    {
        return triviaBluetoothAdapter;
    }

    //kills the Streams and BT-Adapter safely
    public static void DestroyBluetooth() {
        try {
            receiveThreadStop = true;
            sendThreadStop = true;
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Log.d("Trivia Bluetooth", "Fehler beim Beenden des outputStream");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Log.d("Trivia Bluetooth", "Fehler beim Beenden des inputStream");
                }
            }
            if (socket != null && socket.isConnected()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    Log.d("Trivia Bluetooth", "Fehler beim Beenden");
                }
            }
            msg_queue.clear();

            socket  = null;
            bTConnected = false;
            Log.d("Trivia Bluetooth", "BT destroyed");
        } catch (Exception e) {
            Log.d("Trivia Bluetooth", "Fehler beim Beenden:" + e.getMessage());
        }
    }



    public static void connectBluetoothAndStartGame(final Context context) {
        connectBluetoothDialog = ProgressDialog.show(context, "Bluetooth", "Verbinde zu Server", true);
        bTConnected = false;
        isPairing = false;
        connectBluetoothAndStartGame_Thread = new Thread(new Runnable() {
            public void run () {
                    try {
                        if (!connectBluetoothAndStartGame_Thread.isInterrupted()) {
                            Log.d("Trivia Bluetooth", "Start BT Connect");
                            //BT is  enabled
                            List<BluetoothDevice> pairedQuizServerDev = getPairedQuizServerDev();

                            Log.d("Trivia Bluetooth", "Size of paired Quizservers: " + pairedQuizServerDev.size());

                            if (pairedQuizServerDev.size() > 0) {
                                for (BluetoothDevice device : pairedQuizServerDev) {

                                    Log.d("Trivia Bluetooth", "Try to connect to " + device.getName());
                                    bTConnected = connectDevice(device);
                                    Log.d("Trivia Bluetooth", "Result: " + bTConnected);
                                    if (bTConnected) {
                                        Log.d("Trivia Bluetooth", "Begin Listen");
                                        beginListenForData();
                                        Log.d("Trivia Bluetooth", "Begin Send");
                                        beginSendData();
                                        break;
                                    }
                                }
                            }
                            if (!bTConnected) {
                                if (pairedQuizServerDev.size() < 4) {

                                    //pair new server and try again!
                                    Log.d("Trivia Bluetooth", "No paired Device ... try to pair Quizserver...");
                                    if (triviaBluetoothAdapter.isDiscovering()) {
                                        triviaBluetoothAdapter.cancelDiscovery();
                                    }
                                    triviaBluetoothAdapter.startDiscovery();
                                    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                                    intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                                    intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                                    Log.d("Trivia Bluetooth", "Start Register...");
                                    context.registerReceiver(triviaBluetoothReceiver, intentFilter);
                                    Log.d("Trivia Bluetooth", "Started Register...");
                                }
                            }
                            if (bTConnected) {
                                Log.d("Trivia Bluetooth", "Start Activity!");
                                if (connectBluetoothDialog.isShowing()) {
                                    connectBluetoothDialog.dismiss();
                                    Log.d("Trivia Bluetooth", "dialog.dismiss();!");
                                }
                                if (triviaBluetoothAdapter.isDiscovering()) {
                                    triviaBluetoothAdapter.cancelDiscovery();
                                }
                                context.startActivity(new Intent(context, GameActivity.class));
                                connectBluetoothAndStartGame_Thread.interrupt();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("tag", "Error: " + e.getMessage());
                    }
                }
            });

        if(connectBluetoothAndStartGame_Thread.isInterrupted())
        {
            connectBluetoothAndStartGame_Thread.run();
        }else {
            connectBluetoothAndStartGame_Thread.start();
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d("Trivia Bluetooth", "Connect Handler");
                if(!bTConnected && !isPairing) {
                    connectBluetoothAndStartGame_Thread.interrupt();
                    Toast.makeText(context, "Timeout: Quiz offline oder alle vier Plätze belegt.",
                    Toast.LENGTH_LONG).show();

                    if (triviaBluetoothAdapter.isDiscovering()) {
                        triviaBluetoothAdapter.cancelDiscovery();
                    }
                }else if(isPairing)
                {
                    //Dont create Output until pairing is finished
                    Log.d("Trivia Bluetooth", "Pairing needs more time");
                    handlerPostTime+=2000;
                }

                if (connectBluetoothDialog.isShowing()) {
                    connectBluetoothDialog.dismiss();
                    Log.d("Trivia Bluetooth", "dialog.dismiss();!");
                }
            }
        }, handlerPostTime);
    }


    //Send Data from MSG-Queue
    private static void beginSendData() {
        Thread sendThread;
        sendThreadStop = false;
        try {
            sendThread = new Thread(new Runnable() {
                public void run() {
                    //CHECK
                    while (!Thread.currentThread().isInterrupted() && !sendThreadStop) {
                        try {
                            if (msg_queue.size() > 0)
                            {
                                //SEND HERE
                                String tmp = msg_queue.element();
                                synchronized (Thread.currentThread()) {
                                    try {
                                        //TRY TO SEND
                                        outputStream. write(tmp.getBytes("US-ASCII"));
                                        Thread.currentThread().wait(100);
                                    } catch (Exception e) {
                                        Log.d("Trivia Bluetooth", "Output unknown");
                                    }
                                    //WAIT for ACK
                                    if (quizACK.equals("^ACK" + tmp.substring(1, tmp.length()-1))) {
                                        quizACK = "";
                                        msg_queue.remove();
                                    }
                                    //Else the item dont get removed from MSG_QUEUE -> and try to send again
                                }

                            }
                        } catch (Exception ex) {
                            receiveThreadStop = true;
                        }
                    }
                }
            });
            sendThread.start();
        }catch (Exception e)
        {
            Log.d("Trivia Bluetooth", "Thread Error: " + e.getMessage());
        }


    }



    //*****************************************************************************************
    //Private static functions for Bluetooth Handle
    //*****************************************************************************************

    //Listen for Data and handle it
    private static void beginListenForData()
    {
        Thread receiveThread;
        receiveThreadStop = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        receiveThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !receiveThreadStop)
                {
                    try
                    {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
                            //Neuer InputStream
                            final byte[] packetBytes = new byte[bytesAvailable];
                            //Schreibe Stream in packetBytes
                            int in = inputStream.read(packetBytes);
                            if(in == -1)
                                Log.d("Trivia Bluetooth", "End Of InputStream");
                            //Lese den Stream Zeichen für Zeichen
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                //Solange nicht ENDZEICHEN 0x7E erreicht wurde... (   0x7E = ~  )
                                if (b == 0x7E) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    listenDataHandler.post(new Runnable() {
                                        public void run() {
                                            HandleInputString(data);
                                        }
                                    });
                                } else {
                                    //If its the END-CHAR -> Write byte into Buffer
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        receiveThreadStop = true;
                    }
                }
            }
        });
        //START the thread
        receiveThread.start();
    }

    //*****************************************************************************************
    //Work with the received INPUT String
    //*****************************************************************************************
    private static void HandleInputString(String data)
    {
        //Log.d("Trivia Bluetooth", "Text: " + data);
        String command = data.substring(0,4);
        switch (command){
            case "^ACK":
            {
                //Log.d("Trivia Bluetooth", "ACK: " + data);
                //SYNC ACK
                quizACK = data;
                break;
            }
            case "^MSG":
            {
                //MESSAGE FROM OTHER PLAYER
                if (GameActivity.getActualIndex() == 1) {
                    //GameActivity aktive
                    PlayFragment.showNewMSGIncoming(true);
                }
                String tmp_s = data.substring(4, data.length());
                String[] tmp_s_array = tmp_s.split("&");
                ChatFragment.printMessage(tmp_s_array);
                break;
            }
            case "^HSC":
            {
                //HIGHSCORE Receive
                String tmp_s = data.substring(4, data.length());
                Log.d("Trivia Bluetooth", "Highscore: " + tmp_s);
                String[] all = tmp_s.split("@");
                int place = 1;
                if (all.length > 1) {
                    HighScoreFragment.resetList();
                    for (int i = 1; i < all.length - 2; i = i + 3) {
                        HighScoreFragment.setOneScore(place, all[i], all[i + 1], all[i + 2]);
                        place++;
                    }
                    HighScoreFragment.showActualHighScore();
                }
                break;
            }
            case "^INF":
            {
                //INFORMATION
                String tmp_s = data.substring(4, data.length());
                Log.d("Trivia Bluetooth", "Info: " + tmp_s);
                String[] tmp_s_array = {"", "", tmp_s};
                ChatFragment.printMessage(tmp_s_array);
                break;
            }
            case "^QUE":
            {
                //New Question for the Question Queue
                String tmp_s = data.substring(4, data.length());
                int questionIndex = Integer.parseInt(tmp_s);
                QuestionHandler.questionQueueAdd(questionIndex);
                break;
            }
            case "^GOO":
            {
                //Log Out ACK -> End BT and Activity
                bTConnected = false;
                DestroyBluetooth();
                break;
            }
            case "^CON":
            {
                Log.d("Trivia Bluetooth", "Try Count Con");
                try {
                    //number of connected players
                    String tmp_s = data.substring(4, data.length());
                    int connected = Integer.parseInt(tmp_s);
                    Log.d("Trivia Bluetooth", "Verbundene Teilnehmer: " + connected);
                    if (connected <= 3) {
                        Log.d("Trivia Bluetooth", "Es ist noch ein Slot frei -> Spieler darf erneut spielen");
                        //Es ist noch ein Slot frei -> Spieler darf erneut spielen
                        PlayFragment.tryToStartNewGame();
                    } else {
                        Log.d("Trivia Bluetooth", "Kein freier Slot mehr -> Spieler muss anderen den Vorang lassen");
                        //Kein freier Slot mehr -> Spieler muss anderen den Vorang lassen
                        HighScoreFragment.showHighScoreAndFinish();
                    }
                }catch (Exception e)
                {
                    Log.d("Trivia Bluetooth", "Fehler Count: " + e.getMessage());
                    HighScoreFragment.showHighScoreAndFinish();
                }
                break;
            }
        }
    }

    //Create the Broadcast Receiver for BT
    private final static BroadcastReceiver triviaBluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            Log.d("Trivia Bluetooth", "Event");
            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {
                    Log.d("Trivia Bluetooth", "BluetoothDevice.ACTION_FOUND");
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Connect to Device if it is a Quizserver
                    try {
                        //Check Size (QuizserverX == 11)
                        if(device.getName().length() == 11) {
                            //Get Substring (Quizserver) or other 10 Character long string
                            String tmp = device.getName().substring(0, 10);
                            Log.d("Trivia Bluetooth", "Found: " + device.getName());
                            if (tmp.equals("Quizserver")) {
                                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                                    Log.d("Trivia Bluetooth", "Create Bond");
                                    device.createBond();
                                }else{
                                    Log.d("Trivia Bluetooth", "Device isnt a Quizserver");
                                }
                            }
                        }else{
                            Log.d("Trivia Bluetooth", "Device isnt a Quizserver");
                        }
                    }catch (Exception e)
                    {
                        Log.d("Trivia Bluetooth", "Can't Connect");
                    }
                    break;
                }
                //Bond state changed
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                    Log.d("Trivia Bluetooth", "BluetoothDevice.ACTION_BOND_STATE_CHANGED");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //Connect to new Quizserver
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d("Trivia Bluetooth", "Try to start connect again");

                        if (connectBluetoothDialog.isShowing()) {
                            connectBluetoothDialog.dismiss();
                            Log.d("Trivia Bluetooth", "dialog.dismiss();!");
                        }
                        if (triviaBluetoothAdapter.isDiscovering()) {
                            triviaBluetoothAdapter.cancelDiscovery();
                        }
                        connectBluetoothAndStartGame(context);
                        break;
                    }
                }
                //On Pairing request
                case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                    Log.d("Trivia Bluetooth", "BluetoothDevice.ACTION_PAIRING_REQUEST");
                    //if the device isnt actually connected and isnt pairing, start pairing
                    if(!isPairing && !bTConnected){
                        isPairing= true;
                        pairDevice(intent);
                    }
                    break;
                }
                default:
                {
                    Log.d("Trivia Bluetooth", "Other Action:" + action);
                    break;
                }
            }
        }
    };

    //Returns all paired Quizservers
    //Get all paired Devices from BT Adapter
    private static List<BluetoothDevice> getPairedQuizServerDev() {
        //Get all paired devices
        Set<BluetoothDevice> pairedDevices = triviaBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> pairedQuizServerDev = new ArrayList<>();

        // put quizservers to the List
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().length() > 10) {
                String tmp = device.getName().substring(0, 10);
                if (tmp.equals("Quizserver")) {
                    Log.d("Trivia Bluetooth", "Add Paired Device: " + device.getName());
                    pairedQuizServerDev.add(device);
                }
            }
        }
        //Returns all paired Quizservers
        return pairedQuizServerDev;
    }

    //Try to pair a given Device
    private static void pairDevice(Intent intent)
    {
        //Only start if connect thread is active and BT isnt connected
        if(!connectBluetoothAndStartGame_Thread.isInterrupted() && !bTConnected)
        {
            try {
                //Try to pair a given Device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Set Pin in Array
                byte[] pinBytes = new byte[]{0x30, 0x30, 0x30, 0x30};//("0000").getBytes();
                Log.d("Trivia Bluetooth", "Start Pair...");
                //Hack to call hidden Method
                //Seems like this warning is a Bug from Android/JDK -> Byte with uppercase B isn't accepted as PinByte
                device.getClass().getMethod("setPin", byte[].class).invoke(device, pinBytes);
                device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                //Try some times to set the pin -> android hack for to set pin automatic
                for(int i = 0; i < 10; i++)
                {
                    try{
                        Thread.sleep(420);
                        device.getClass().getMethod("setPin", byte[].class).invoke(device, pinBytes);
                    }catch (Exception e)
                    {
                        Log.d("Trivia Bluetooth", "Fehler PIN: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.d("Trivia Bluetooth", e.getMessage());
            }
        }
    }


    //Connects a given device with the standard UUID
    private static boolean connectDevice(BluetoothDevice device)
    {
        //Standard BT uuid
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Log.d("Trivia Bluetooth", "ConnectDevice Error1: " + e1.getMessage());
        }
        if(socket != null) {
            //Cancel BT Discorvery
            if (triviaBluetoothAdapter.isDiscovering()) {
                triviaBluetoothAdapter.cancelDiscovery();
            }
            try {
                //=============================================================================================================================
                //Connect the socket
                socket.connect();
                //Set in- and outputstream
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                return  true;
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.d("Trivia Bluetooth", "ConnectDevice Error2: " + e2.getMessage());
                }
            }
        }
        return  false;
    }
}
//*****************************************************************************************
//End of File
//*****************************************************************************************