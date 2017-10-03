package pf.aismap;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    private String serverMessage;
    public static final String SERVERIP = "192.168.10.1";  //ip del router   10.1
    public static final int SERVERPORT = 8080;             //puerto del router
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    PrintWriter out;
    BufferedReader in;

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    //public void sendMessage(String message){
    //    if (out != null && !out.checkError()) {
    //        out.println(message);
    //        out.flush();
    //    }
    //}

    public void stopClient(){mRun = false;}

    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");
            Socket socket = new Socket(serverAddr, SERVERPORT);
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e("TCP Client", "C: Sent.");
                Log.e("TCP Client", "C: Done.");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMessage);           //el servermessage lo publica publisprogress del asyntask
                        Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                    }
                    serverMessage = null;
                }
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }
            finally {socket.close();}
        } catch (Exception e) {Log.e("TCP", "C: Error", e);}
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}






