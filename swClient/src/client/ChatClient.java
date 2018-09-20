package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    public static String UserID;
    public static String UserPW;
    public static boolean login;
    public static boolean Enter;
    public static String ip="127.0.0.1"; //change to ChatServer's ip


    public static void main(String[] args) {

        try {
            Socket c_socket = new Socket(ip, 8888);
            DataOutputStream tmp = new DataOutputStream(c_socket.getOutputStream());
            tmp.writeUTF("CHAT");

            ReceiveThread rec_thread = new ReceiveThread();
            rec_thread.setSocket(c_socket);

            SendThread send_thread = new SendThread();
            send_thread.setSocket(c_socket);

            login=false;
            Enter=false;

            send_thread.start();
            rec_thread.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
