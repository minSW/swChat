package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SendThread extends Thread{
    public static String SignID;
    public static String SignPW;

    private Socket m_socket;

    @Override
    public void run() {
        super.run();
        try {
            BufferedReader tmpbuf = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter sendWriter = new PrintWriter(m_socket.getOutputStream());

            String sendString;

            System.out.println("\n :::: HELLO SW CHAT :::: \n	-GUIED-");
            System.out.println("FILE : 파일전송 / LOAD CHAT : 본 ID의 대화내용 읽기 \nWHISPER : 특정인에게 귓속말하기 / EXIT : 나가기");


            while(true){
                if (!ChatClient.login){
                    Thread.sleep(800);
                    if (ChatClient.login) break;

                    System.out.println("\n(회원가입: 1) (로그인: 2)");
                    int select=tmpbuf.read();
                    tmpbuf.readLine();

                    if (select=='1'){
                        System.out.println("사용할 ID를 입력해주십시오 : ");
                        SignID = tmpbuf.readLine();

                        System.out.println("사용할 비밀번호를 입력해주십시오: ");
                        SignPW = tmpbuf.readLine();

                        sendWriter.println("SignID접속시도" + SignID +","+SignPW);
                        sendWriter.flush();

                    } else if (select=='2'){
                        System.out.println("ID : ");
                        ChatClient.UserID = tmpbuf.readLine();
                        System.out.println("Password : ");
                        ChatClient.UserPW = tmpbuf.readLine();

                        sendWriter.println("UserID접속시도" + ChatClient.UserID+","+ChatClient.UserPW);
                        sendWriter.flush();

                    } else {
                        System.out.println("잘못된 값을 입력하셨습니다");
                    }
                } else break;
            }

            while(true){
                if (!ChatClient.Enter){
                    Thread.sleep(800);
                    if (ChatClient.Enter) break;
                    System.out.println("\n(M:N 채팅방 개설 : 0) (1:M 채팅방 개설 : 1) (채팅방 확인 : 2) (채팅방 접속 : 3)");
                    int select=tmpbuf.read();
                    tmpbuf.readLine();

                    if (select=='1' || select=='0'){
                        sendWriter.println("채팅방개설요청"+select);
                        sendWriter.flush();
                        break;

                    }else if (select=='2'){
                        sendWriter.println("채팅방조회요청");
                        sendWriter.flush();
                        Thread.sleep(1000);

                    } else if (select =='3'){
                        if (!ChatClient.Enter){
                            System.out.println("채팅방 번호를 입력하세요");
                            sendString=tmpbuf.readLine();
                            sendWriter.println("채팅방접속요청"+sendString);
                            sendWriter.flush();
                        }

                    }
                    else {
                        System.out.println("잘못된 값을 입력하셨습니다");
                    }
                } else break;
            }


            while(true){
                sendString=tmpbuf.readLine();

                if(sendString.equals("EXIT")) break;
                else if(sendString.equals("FILE")){ // 'FILE' command -> FILE SEND MODE
                    Socket f_socket = new Socket(ChatClient.ip, 8888); // filethread socket open
                    DataOutputStream tmp = new DataOutputStream(f_socket.getOutputStream());
                    tmp.writeUTF("FILE"); // "it's FILE" notice to server
                    FileSendThread fsend_thread = new FileSendThread();
                    fsend_thread.setSocket(f_socket); // socket connect into filethread
                    fsend_thread.start();

                    System.out.println("파일을 보내시려면 'SEND 파일경로'를 입력하세요 (ex. SEND C:/Users/MIN/Desktop/text.txt");
                    while(fsend_thread.isAlive()){
                        // wait for sending file
                    }
                    f_socket.close();

                    sendWriter.println(" [System] 파일을 업로드 했습니다. 받으시려면 'RECEIVE' 이라고 입력하세요 ");
                    sendWriter.flush();		// "COMPELETE" notice to server

                }
                else if(sendString.equals("RECEIVE")){ // 'RECEIVE' command -> FILE RECEIVE MODE
                    Socket f_socket = new Socket(ChatClient.ip, 8888);
                    DataOutputStream tmp = new DataOutputStream(f_socket.getOutputStream());
                    tmp.writeUTF("FILE");
                    FileSendThread fsend_thread = new FileSendThread();
                    fsend_thread.setSocket(f_socket);
                    fsend_thread.start();

                    System.out.println("파일을 받으시려면 'DOWN 파일경로'를 정확히 입력하세요 (ex. DOWN C:/Users/MIN/Desktop)");
                    while(fsend_thread.isAlive()){
                        // wait for receiving file
                    }
                    f_socket.close();

                    if(FileSendThread.result.contains("SUCCESS")) System.out.println("파일을 다운받았습니다");
                    else System.out.println(FileSendThread.result+"경로 혹은 입력에 오류가 있어 실패했습니다. 다시 받으시려면 RECEIVE를 입력하세요");
                } else {
                    sendWriter.println(sendString); // normal message
                    sendWriter.flush();
                }
            }

            sendWriter.close();
            tmpbuf.close();
            m_socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket(Socket _socket)
    {
        m_socket = _socket;
    }
}
