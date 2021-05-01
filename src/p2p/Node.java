package p2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	FileHandler fileHandler;
	int port;
	String ip;
	String path = System.getProperty("user.dir");
	String folderPath = path + File.separator + "files";
	File directoryPath = new File(folderPath);

	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
	// should bigger than the file to be downloaded
	String FILE =  folderPath + File.separator + "temp1.txt";

	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		switch (type) {
		case "Server":
			this.port = port;
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			ServerSocket serverSock = new ServerSocket(port);
			Socket server = serverSock.accept();
			System.out.println("Connected");

			while(server.isConnected()) {
				// receive file
				receiveFile(server, FILE_SIZE, FILE);
			}

			server.close();
			break;
		case "Client":
			this.port = port;
			System.out.println("Waiting for connection ...");
			Socket client = null;
			for (String ip : networkIps) {
				try {
					client = new Socket(ip, port);
					System.out.println("Connected");

					// send file
					sendfile(client);
					
					client.close();
				} catch (ConnectException e) {
					// do nothing
				}
			}
			break;
		}

	}


	private void setDirectory() {
		if(!directoryPath.exists()) {
			directoryPath.mkdir();
		}
	}

	private String getPath() {
		return folderPath;
	}

	private File[] getListofFiles() {
		setDirectory();
		return directoryPath.listFiles();
	}
	
	private void sendfile(Socket socket) throws IOException {
		// send file
		for(File myFile: getListofFiles()) {
			byte [] mybytearrayclient  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearrayclient,0,mybytearrayclient.length);
			OutputStream os = socket.getOutputStream();
			System.out.println("Sending " + myFile.getName() + "(" + mybytearrayclient.length + " bytes)");
			os.write(mybytearrayclient,0,mybytearrayclient.length);
			os.flush();
			System.out.println("Done.");
		}
	}
	
	private void receiveFile(Socket socket, int fileSize, String fileName) throws IOException {
		// receive file
		byte [] mybytearray  = new byte [fileSize];
		InputStream is = socket.getInputStream();
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int bytesRead = is.read(mybytearray,0,mybytearray.length);
		int current = bytesRead;

		do {
			bytesRead =
					is.read(mybytearray, current, (mybytearray.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);

		bos.write(mybytearray, 0 , current);
		bos.flush();
		System.out.println("File " + fileName
				+ " downloaded (" + current + " bytes read)");
	}
}
