package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {
    public static HashMap<Integer, ArrayList<String>> ManyToManyClient = new HashMap<>();
    public static HashMap<Integer, ArrayList<String>> OneToManyClient = new HashMap<>();

    public static HashMap<String, PrintWriter> ClientOutput = new HashMap<String,PrintWriter>();

    public static String filePath="/Users/user/Downloads"; // change to server storage path

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        try {
            ServerSocket s_socket = new ServerSocket(8888);
            System.out.println("~~~ START SERVER ~~~");
            while(true)
            {

                Socket c_socket = s_socket.accept();
                DataInputStream tmp = new DataInputStream(c_socket.getInputStream());
                String type=tmp.readUTF();

                System.out.println(type);
                if (type.equals("CHAT")){ // CHAT MODE
                    ClientManagerThread c_thread = new ClientManagerThread();
                    c_thread.setSocket(c_socket);
                    c_thread.start();
                } else { // FILE MODE
                    FileManagerThread fsend_thread = new FileManagerThread();
                    fsend_thread.setSocket(c_socket);
                    fsend_thread.start();
                }
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
