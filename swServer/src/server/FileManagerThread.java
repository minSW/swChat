package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileManagerThread extends Thread {
    Socket f_socket;
    DataInputStream dis = null;
    DataOutputStream dos =null;
    FileOutputStream fos;
    BufferedOutputStream bos;

    FileInputStream fis;
    BufferedInputStream bis;

    public static String fileNm;
    String filePath=ChatServer.filePath;

    //@Override
    public void run() {

        try {
            dis = new DataInputStream(f_socket.getInputStream());
            dos = new DataOutputStream(f_socket.getOutputStream());
            while(true){
                if (dis!=null){
                    String type = dis.readUTF();
                    if(type.contains("file")){
                        fileNm=type.split("file")[1];
                        fileWrite(dis);
                        break;
                    }else if(type.equals("receive")){
                        fileRead(dos);
                        break;
                    }
                }

            }
            // try { dos.close(); } catch (IOException e) { e.printStackTrace(); }
            // try { dis.close(); } catch (IOException e) { e.printStackTrace(); }
        }catch (IOException e) {
            System.out.println("ERROR");
        }
    }

    private String fileWrite(DataInputStream dis){
        String result;
        try {
            System.out.println("파일을 클라이언트에서 서버로 받아오고 있습니다");
            String fileNm = dis.readUTF();
            File file = new File(filePath + "/" + fileNm);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int len;
            int size = 200 * 1024 * 1024;
            byte[] data = new byte[size];
            while ((len = dis.read(data)) != -1) {
                bos.write(data, 0, len);
            }
            System.out.println("서버에 (" + fileNm +") 파일이 성공적으로 업로드 되었습니다");
            System.out.println("경로 :" +filePath +" 에 저장");
            result = "SUCCESS";

        } catch (IOException e) {
            //  e.printStackTrace();
            result = "ERROR";
        } finally{
            try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
            try { bos.close(); } catch (IOException e) { e.printStackTrace(); }
            try { dis.close(); } catch (IOException e) { e.printStackTrace(); }

        }

        return result;
    }

    private String fileRead(DataOutputStream dos){

        String result;

        try {
            dos.writeUTF(fileNm);
            dos.flush();
            System.out.println(fileNm);

            System.out.println("파일 (" + fileNm + ")를 클라이언트에게 전송하고 있습니다");

            File file = new File(filePath + "/" + fileNm);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            int len;
            int size = (int) file.length();
            byte[] data = new byte[size];
            while ((len = bis.read(data)) != -1) {
                dos.write(data, 0, len);
            }

            System.out.println("(" + file +") 파일이 성공적으로 다운로딩 되었습니다");
            result = "SUCCESS";
        } catch (IOException e) {
            //  e.printStackTrace();
            result = "ERROR";
        }
        finally{
            try { fis.close(); } catch (Exception e) { e.printStackTrace(); }
            try { bis.close(); } catch (IOException e) { e.printStackTrace(); }
            try { dos.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        return result;
    }

    public void setSocket(Socket _socket) {
        f_socket = _socket;
    }
}
