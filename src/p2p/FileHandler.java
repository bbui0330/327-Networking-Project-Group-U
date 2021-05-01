package p2p;

import java.io.*;
import java.net.Socket;

public class FileHandler {
    Socket socket;
    InputStream inputStream;
    FileOutputStream fileOutputStream;
    BufferedOutputStream bufferedOutputStream;
    int bufferSize;


    FileHandler(Socket peer) {
        socket = peer;
        inputStream = null;
        fileOutputStream = null;
        bufferedOutputStream = null;
        bufferSize = 0;

    }

    void receiveFile(String fileName) {
        try {
            inputStream = socket.getInputStream();
            bufferSize = socket.getReceiveBufferSize();
            System.out.println("Buffer size: " + bufferSize);
            fileOutputStream = new FileOutputStream(fileName);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            byte[] bytes = new byte[bufferSize];
            int count;
            while ((count = inputStream.read(bytes)) >= 0) {
                bufferedOutputStream.write(bytes, 0, count);
            }
            bufferedOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendFile(File file) {

        FileInputStream fis;
        BufferedInputStream bis;
        BufferedOutputStream out;
        byte[] buffer = new byte[8192];
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            out = new BufferedOutputStream(socket.getOutputStream());
            int count;
            while ((count = bis.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            fis.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}