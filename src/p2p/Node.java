package p2p;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	
	int port;
	String ip;
	
	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		switch(type) {
		case "Server":
			this.port = port;
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			ServerSocket serverSock = new ServerSocket(port);
			Socket Sock=serverSock.accept();
			System.out.println("Connected");
			/*
			 * DataOutputStream out =new DataOutputStream(Sock.getOutputStream());
			 * out.writeUTF("i am fine, thank you"); DataInputStream in= new
			 * DataInputStream(Sock.getInputStream()); System.out.println(in.readUTF());
			 */
			Sock.close();
			break;
		case "Client":
			this.port = port;
			System.out.println("Waiting for connection ...");
			Socket client;
			for(String ip : networkIps) {
				try {
					client = new Socket(ip, port);
					System.out.println("Connected");
					
					/*
					 * DataInputStream in= new DataInputStream(sock.getInputStream());
					 * System.out.println(in.readUTF()); DataOutputStream out =new
					 * DataOutputStream(sock.getOutputStream());
					 * out.writeUTF("waiting for connection");
					 */
					client.close();
				}catch(ConnectException e) {
					// do nothing
				}
			}
			
			break;
		}
		
	}
}
