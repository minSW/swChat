package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientManagerThread extends Thread{
    public static HashMap<String, String> Sign=new HashMap<String, String>();
    public static HashMap<String, String> Login=new HashMap<String, String>();;
    public static ArrayList<String> ChatRoom = new ArrayList<String>();
    public static HashMap<String, String> Chatting = new HashMap<String, String>();

    public static int index=0;
    String chat =" \n";

    private String IDs;
    private String PWs;

    private Socket m_socket;
    private String m_ID;
    private int m_room;

    private PrintWriter out;

    @Override
    public void run() {
        super.run();

        try {
            BufferedReader tmpbuffer = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            String text;
            PrintWriter Me_out=new PrintWriter( m_socket.getOutputStream());

            while(true)
            {
                text = tmpbuffer.readLine();

                if(text == null)
                {
                    Login.remove(m_ID);
                    System.out.println(m_ID + " 이(가) 나갔습니다");
                    sendToRoom(m_ID + " 이(가) 나갔습니다");
                    String tmp=Chatting.get(m_ID)+"\n ### (EXIT) ### \n";
                    Chatting.replace(m_ID, tmp);
                    break;
                }

                String[] split = text.split("접속시도");
                if(split.length == 2 && split[0].equals("SignID"))
                {
                    split=split[1].split(",");
                    IDs = split[0];
                    PWs = split[1];
                    if (!Sign.containsKey(IDs))	{
                        Sign.put(IDs, PWs);
                        System.out.println(IDs + " 이(가) 가입되었습니다");
                        Me_out.println("[System]"+IDs + " 이(가) 가입되었습니다");
                        Me_out.flush();
                    }
                    else{
                        System.out.println(IDs + " 은(는) 이미 가입한 ID입니다");
                        Me_out.println("[System]"+IDs +" 은(는) 이미 가입한 ID입니다");
                        Me_out.flush();
                    }

                    continue;
                }

                else if(split.length == 2 && split[0].equals("UserID"))
                {
                    split=split[1].split(",");
                    IDs = split[0];
                    PWs = split[1];
                    if (Sign.containsKey(IDs)&&PWs.equals(Sign.get(IDs))){
                        if (!Login.containsKey(IDs)){
                            m_ID=IDs;
                            Login.put(IDs,PWs);
                            ChatServer.ClientOutput.put(m_ID,Me_out);
                            System.out.println(m_ID + " 이(가) 로그인하였습니다");
                            Me_out.println("[System]"+m_ID + " 이(가) 로그인하였습니다");
                            Me_out.flush();
                            if(!Chatting.containsKey(m_ID))Chatting.put(m_ID, chat);  //CHAT SAVE Point

                        }else{
                            System.out.println(IDs + " 는 이미 로그인중입니다");
                            Me_out.println("[System]"+IDs +" 는 이미 로그인중입니다");
                            Me_out.flush();
                        }
                    }else{
                        System.out.println(IDs + " 로그인 실패");
                        Me_out.println("[System]"+IDs + " 로그인 실패");
                        Me_out.flush();
                    }
                    continue;
                }

                if(Login.containsKey(m_ID)) { //After login
                    if (text.contains("채팅방개설요청")){
                        split = text.split("채팅방개설요청");
                        int select = Integer.parseInt(split[1]);
                        ChatRoom.add(index, m_ID);
                        ArrayList<String> client = new ArrayList<>();
                        client.add(m_ID);

                        if(select == '0'){
                            ChatServer.ManyToManyClient.put(index, client);
                        } else {
                            ChatServer.OneToManyClient.put(index, client);
                        }

                        m_room = index;
                        index++;
                        Me_out.println( m_ID+" 의 채팅방을 생성하였습니다");
                        Me_out.flush();
                        continue;
                    }else if(text.equals("채팅방조회요청")){
                        if (ChatRoom.isEmpty()){
                            Me_out.println("존재하는 채팅방이 없습니다");
                            Me_out.flush();
                            continue;
                        }else{
                            Me_out.flush();
                            int empty=0;
                            for(int i =0 ; i<ChatRoom.size(); i++){
                                if(!ChatRoom.get(i).equals("EMPTY")){
                                    Me_out.println("   "+i+" . "+ChatRoom.get(i)+" 의 채팅방");
                                    Me_out.flush();
                                }else empty++;
                            }
                            if((ChatRoom.size()-empty)==0){
                                Me_out.println("존재하는 채팅방이 없습니다");
                                Me_out.flush();
                                continue;
                            }
                            Me_out.println("[개설되어 있는 채팅방 목록]");
                            Me_out.flush();
                            Me_out.println("-총 "+(ChatRoom.size()-empty)+" 개의 채팅방-");
                            Me_out.flush();
                            continue;
                        }
                    }else if(text.contains("접속요청")){
                        split = text.split("접속요청");
                        int select = Integer.parseInt(split[1]);

                        if (select>=ChatRoom.size()||ChatRoom.get(select).equals("EMPTY")){
                            Me_out.println("존재하지 않는 채팅방입니다");
                            Me_out.flush();

                        }else{
                            m_room = select;
                            if(ChatServer.ManyToManyClient.containsKey(select)){
                                ChatServer.ManyToManyClient.get(select).add(m_ID);
                                System.out.println(m_room+"번 방에 현재 접속 중 : "+ ChatServer.ManyToManyClient.get(m_room));

                                for(String sendTo : ChatServer.ManyToManyClient.get(m_room)){
                                    sendToClient(sendTo, m_ID+" 이(가) 채팅방에 입장하였습니다");
                                }
                                Me_out.println(ChatRoom.get(select)+" 의 채팅방에 입장하였습니다");
                            } else {
                                ChatServer.OneToManyClient.get(select).add(m_ID);
                                System.out.println(m_room+"번 방에 현재 접속 중 :"+ ChatServer.OneToManyClient.get(m_room));

                                sendToClient(ChatRoom.get(m_room), m_ID+" 이(가) 채팅방에 입장하였습니다");
                                Me_out.println(ChatRoom.get(select)+" 의 1:M 채팅방에 입장하였습니다");
                            }

                            m_room = select;
                            Me_out.flush();
                            continue;
                        }
                        continue;
                    }
                }

                if(text.equals("WHISPER"))
                {
                    String whisper_ID;
                    String whisper_text;
                    Me_out.println("Enter the ID who you want to send a message : ");
                    Me_out.flush();
                    whisper_ID=tmpbuffer.readLine();
                    if(!Sign.containsKey(whisper_ID))
                    {
                        Me_out.println("This person didn't join. WRONG ID!!");
                        Me_out.flush();
                        continue;
                    }
                    Me_out.println("Enter the message : ");
                    Me_out.flush();
                    whisper_text=tmpbuffer.readLine();
                    Whisper(whisper_ID, whisper_text);
                    continue;
                }

                if(text.equals("LOAD CHAT"))
                {
                    Me_out.println("===============LOAD CHATTING==============="+Chatting.get(m_ID)+"\n=============================================");
                    Me_out.flush();
                    System.out.println(Chatting.get(m_ID));
                }
                sendToRoom(m_ID + "> "+ text); //¸Þ¼¼Áö Àü¼Û
            }

            //EXIT
            if(ChatServer.ManyToManyClient.containsKey(m_room)) {
                ChatServer.ManyToManyClient.get(m_room).remove(m_ID);
                if(ChatServer.ManyToManyClient.get(m_room).size() == 0){
                    ChatServer.ManyToManyClient.remove(m_room);
                    ChatRoom.set(m_room, "EMPTY");
                }
            } else {
                ChatServer.OneToManyClient.get(m_room).remove(m_ID);
                if(ChatServer.OneToManyClient.get(m_room).size() == 0){
                    ChatServer.OneToManyClient.remove(m_room);
                    ChatRoom.set(m_room, "EMPTY");
                }
            }

            //System.out.println(ChatServer.ManyToManyClient.size() + "ManyClient"+m_room+"."+m_ID);
            ChatServer.ClientOutput.remove(m_ID);
            tmpbuffer.close();
            m_socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void Whisper(String ID, String msg)
    {
        sendToClient(ID, "(Whispering) "+m_ID+"> "+msg);
    }

    public void sendToRoom(String msg)
    {
        if(ChatServer.ManyToManyClient.containsKey(m_room))
        { // M:N
            for(String sendTo : ChatServer.ManyToManyClient.get(m_room))
            {
                sendToClient(sendTo, msg);
            }
        }
        else
        {
            if(ChatRoom.get(m_room) == m_ID)
            { // 1 in 1:M
                for(String sendTo : ChatServer.OneToManyClient.get(m_room))
                {
                    sendToClient(sendTo, msg);
                }

            }
            else
            { // M in 1:M
                sendToClient(ChatRoom.get(m_room),msg);
                sendToClient(m_ID,msg);

            }
        }
    }

    public void sendToClient(String ID, String msg){
        out=ChatServer.ClientOutput.get(ID);
        out.println(msg);
        out.flush();
        if (msg.split(">")[0].equals(ID)) msg=msg.split(">")[1];
        String tmp=Chatting.get(ID)+msg+"\n";
        Chatting.replace(ID, tmp);
    }

    public void setSocket(Socket _socket)
    {
        m_socket = _socket;
    }
}
