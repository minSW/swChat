package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;


public class FileSendThread extends Thread {
    String filePath;
    String fileNm;
    Socket f_socket;
    DataOutputStream dos;
    DataInputStream dis;

    FileInputStream fis;
    BufferedInputStream bis;
    FileOutputStream fos;
    BufferedOutputStream bos;

    public static String result;


    // @Override
    public void run() {
        BufferedReader tmpbuf = new BufferedReader(new InputStreamReader(System.in));
        try {
            dos = new DataOutputStream(f_socket.getOutputStream());
            dis = new DataInputStream(f_socket.getInputStream());

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String select;
        try {
            select = tmpbuf.readLine();

            if (select.contains("SEND")){
                select=select.split(" ")[1];
                String[] tmp=select.split("/");
                fileNm = tmp[tmp.length-1];
                filePath = select.replace("/"+fileNm, "");

                try {
                    dos.writeUTF("file"+fileNm);
                    dos.flush();

                    result = fileRead(dos);
                    /*test*/System.out.println("UPLOAD : " + result);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else if (select.contains("DOWN")){
                filePath=select.split(" ")[1];
                try{
                    dos.writeUTF("receive");
                    dos.flush();

                    String result=fileWrite(dis);
                    System.out.println("DOWNLOAD: "+result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e) { e.printStackTrace(); }

    }

    private String fileRead(DataOutputStream dos){


        try {
            dos.writeUTF(fileNm);

            /*test*/System.out.println("[System] 파일 (" + fileNm + ")를 서버로 보내고 있습니다");

            File file = new File(filePath + "/" + fileNm);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            int len;
            int size = (int) file.length();
            byte[] data = new byte[size];
            while ((len = bis.read(data)) != -1) {
                dos.write(data, 0, len);
            }
            result = "SUCCESS";
        } catch (IOException e) {
            //  e.printStackTrace();
            result = "ERROR";
        }finally{
            try { fis.close(); } catch (IOException e) { e.printStackTrace(); }
            try { dos.close(); } catch (IOException e) { e.printStackTrace(); }
            try { bis.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        return result;
    }

    private String fileWrite(DataInputStream dis){

        //   String filePath = "C:/Users/MIN/Desktop/output/receive";
        try {
            String fileNm = dis.readUTF();
            System.out.println("[System] 파일 (" + fileNm + ")을 서버에서 받아오고 있습니다");
            dos.writeUTF(fileNm);
            dos.flush();
            File file = new File(filePath + "/" + fileNm);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int len;
            int size = 200 * 1024 * 1024;
            byte[] data = new byte[size];
            try {
                while ((len = dis.read(data)) != -1) {
                    bos.write(data, 0, len);
                }
            } catch (SocketException e) {
                // do nothing;
            }

            System.out.println("[System] "+filePath+"/"+fileNm+" 파일을 생성하였습니다");

            //bos.flush();
            result = "SUCCESS";

        } catch (IOException e) {
            e.printStackTrace();
            result = "ERROR";
        }finally{
            try { bos.close(); } catch (IOException e) { e.printStackTrace(); }
            try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
            try { dis.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        return result;
    }

    public void setSocket(Socket _socket) {
        f_socket = _socket;

    }
}
