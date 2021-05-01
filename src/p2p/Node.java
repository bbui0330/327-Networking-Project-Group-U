package p2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	int port;
	String ip;
	FileHandler fileHandler; 
	
	/**
	 * Constructor
	 * @param type: determine whether the peer is acting as client or server
	 * @param port: port number
	 * @param networkIps: list of IP addresses in the network
	 * @throws IOException
	 */
	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		fileHandler = new FileHandler();
		this.port = port;	// sets the port
		switch (type) {
		case "Server": 
			
			// sets IP address to the IP address of this device
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			// creates a server socket, bound to the specified port
			ServerSocket serverSock = new ServerSocket(port);
			
			Socket server = serverSock.accept();
			System.out.println("Connected");
			
			// receive file
			fileHandler.receiveFile(server);

			server.close();		// closes the socket
			break;
		case "Client":
			
			System.out.println("Waiting for connection ...");
			Socket client;	// creates client socket
			// checks every IP in the network for the peer that is acting as a server
			for (String ip : networkIps) {
				try {
					/* creates a stream socket and connects it to the 
					 * specified port number at the specified IP address */
					client = new Socket(ip, port);
					System.out.println("Connected");
					
					
				
					// send file
					fileHandler.sendFile(client);
					
					client.close();		// closes the socket
				} catch (ConnectException e) {
					// do nothing
				}
			}
			break;
		}
	}

	
	
	

	
	
}
