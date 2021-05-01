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
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	FileHandler fileHandler;
	int port;
	String ip;
	String path = System.getProperty("user.dir");
	String folderPath = path + File.separator + "files";
	File directoryPath = new File(folderPath);

	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		switch (type) {
		case "Server":
			this.port = port;
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			ServerSocket serverSock = new ServerSocket(port);
			Socket server = serverSock.accept();
			System.out.println("Connected");

			while(server.isConnected()) {
				fileHandler.receiveFile("test1.txt");
				
//				File [] serverFiles = getListofFiles();
//				//				System.out.println(java.util.Arrays.toString(getListofFiles()));
//				Scanner in = new Scanner(server.getInputStream());
//				InputStream is = server.getInputStream();
//				PrintWriter pr = new PrintWriter(server.getOutputStream(), true);
//				String FileName = in.nextLine();
//				int FileSize = in.nextInt();
//				FileOutputStream fos = new FileOutputStream(FileName);
//				BufferedOutputStream bos = new BufferedOutputStream(fos);
//				byte[] filebyte = new byte[FileSize];
//
//				int file = is.read(filebyte, 0, filebyte.length);
//				bos.write(filebyte, 0, file);
//
//				System.out.println("Incoming File: " + FileName);
//				System.out.println("Size: " + FileSize + "Byte");
//				if(FileSize == file)System.out.println("File is verified");
//				else System.out.println("File is corrupted. File Recieved " + file + " Byte");
//				pr.println("File Recieved Successfully.");
			}
			// DataOutputStream out =new DataOutputStream(server.getOutputStream());
			// out.writeUTF("i am fine, thank you"); DataInputStream in= new
			// DataInputStream(server.getInputStream()); System.out.println(in.readUTF());
			server.close();
			break;
		case "Client":
			this.port = port;
			System.out.println("Waiting for connection ...");
			Socket client;
			for (String ip : networkIps) {
				try {
					client = new Socket(ip, port);
					System.out.println("Connected");
					File [] fileList = getListofFiles();
					for(File f: fileList) {
						fileHandler.sendFile(f);
					}
//					//	while(client.isConnected()) {
//					File [] clientFiles = getListofFiles();
//					//System.out.println(java.util.Arrays.toString(getListofFiles()));
//					String path = folderPath;
//					for(File f: clientFiles) {
//						//String file = path + File.separator + s;
////						File MyFile = new File(file);
//						int FileSize = (int) f.length();
//						OutputStream os = client.getOutputStream();
//						PrintWriter pr = new PrintWriter(client.getOutputStream(), true);
//						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
//						Scanner in = new Scanner(client.getInputStream());
//
//						pr.println(f.getName());
//						pr.println(FileSize);
//						byte[] filebyte = new byte[FileSize];
//						bis.read(filebyte, 0, filebyte.length);
//						os.write(filebyte, 0, filebyte.length);
//						//				        System.out.println(in.nextLine());
//						os.flush();
//					}
					//	DataInputStream input = new DataInputStream(client.getInputStream());
					//	System.out.println(input.readUTF()); DataOutputStream output =new
					//	DataOutputStream(client.getOutputStream());
					//	output.writeUTF("Hello");
					//	}
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
}
