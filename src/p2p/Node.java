package p2p;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node extends Thread {
	int port;
	String ip;
	FileHandler fileHandler; 
	String type;
	ConcurrentSkipListSet<String> networkIps;
	
	/**
	 * Constructor
	 * @param type: determine whether the peer is acting as client or server
	 * @param port: port number
	 * @param networkIps: list of IP addresses in the network
	 */
	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) {
		fileHandler = new FileHandler();
		this.port = port;	// sets the port
		this.type = type;
		this.networkIps = networkIps;
	}

	/**
	 * establishes connection to the network
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	private void link() throws IOException, ClassNotFoundException, InterruptedException {
		switch (type) {
		case "Server": 
			// sets IP address to the IP address of this device
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			// creates a server socket, bound to the specified port
			ServerSocket serverSock = new ServerSocket(port);
			
			Socket server = serverSock.accept();
			System.out.println("Connected");
			
			NodeInfo serverNodeInfo = new NodeInfo(server);
			serverNodeInfo.addNode(this.ip);
			serverNodeInfo.receiveDHT();
			serverNodeInfo.sendDHT();
			Hashtable<String, File[]> dhtServer = serverNodeInfo.getDHT();
			System.out.println("The set is: " + dhtServer.toString());
			
			// receive file
			fileHandler.receiveFile(server);

			server.close();		// closes the socket
			break;
		case "Client":
			System.out.println("Waiting for connection ...");
			Socket peer;	// creates client socket
			// checks every IP in the network for the peer that is acting as a server
			for (String ip : networkIps) {
				try {
					/* creates a stream socket and connects it to the 
					 * specified port number at the specified IP address */
					peer = new Socket(ip, port);
					System.out.println("Connected");
					
					//
					NodeInfo peerNodeInfo = new NodeInfo(peer);
					peerNodeInfo.addNode(InetAddress.getLocalHost().getHostAddress().toString());
					peerNodeInfo.sendDHT();
					Thread.sleep(1000);
					peerNodeInfo.receiveDHT();
					
					Hashtable<String, File[]> dhtPeer = peerNodeInfo.getDHT();
					System.out.println("The set is: " + dhtPeer.toString());
					
					// send file
					fileHandler.sendFile(peer);
					
					peer.close();		// closes the socket
				} catch (ConnectException e) {
					// do nothing
				}
			}
			break;
		}
	}
	
	@Override
    public void run() {
        super.run();
        try{
        	link();	// starts the link() function
        }
        catch(IOException e) {} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	
	
}
