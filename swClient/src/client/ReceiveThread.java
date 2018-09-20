package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class ReceiveThread extends Thread{

    private Socket m_socket;

    @Override
    public void run() {
        super.run();

        try {
            BufferedReader tmpbuf = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));

            String receiveString;
            String[] split;
            while(true)
            {
                try{
                    receiveString = tmpbuf.readLine();
                } catch (SocketException e){
                    break;
                }
                split=receiveString.split(">");

                if(split.length>=2&&split[0].equals(ChatClient.UserID)){
                    continue;
                }
                if(receiveString.contains("이(가) 로그인하였습니다")){
                    ChatClient.login=true;
                }
                if(receiveString.contains("채팅방에 입장하였습니다")) {
                    ChatClient.Enter=true;
                    if(receiveString.contains(ChatClient.UserID)) continue;
                }
                if (ChatClient.login==true) {
                    if (ChatClient.Enter==false && split.length>=2) continue;
                    System.out.println(receiveString);
                }
                else if(receiveString.contains("[System]")) System.out.println(receiveString);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSocket(Socket _socket)
    {
        this.m_socket = _socket;
    }

}
