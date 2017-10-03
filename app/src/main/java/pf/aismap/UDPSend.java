package pf.aismap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


class UdpSend extends Thread  {



    @Override
    public void run()
    {
        UDPSend();
    }

    public void UDPSend()
    {
       // String mensaje1 = MainActivity.msg1;

        //String messageStr = mensaje1;
        String messageStr="nova";

        int server_port = 62150;
        try
        {
            DatagramSocket s = new DatagramSocket();
            InetAddress local = null;
            local = InetAddress.getByName("13.59.89.12");
            int msg_length = messageStr.length();
            byte[] message = messageStr.getBytes();
            DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
            s.send(p);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            android.util.Log.w("UDP", "Catched here.");
            e.printStackTrace();
        }
    }

}