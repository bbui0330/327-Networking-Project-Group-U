package p2p;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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
		boolean[] bools = new boolean[2];
		int newPort = port;
		switch (type) {
		case "Server": 
			// sets IP address to the IP address of this device
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();

//			while(true) {
				// creates a server socket, bound to the specified port
				ServerSocket serverSock = new ServerSocket(newPort);

				Socket server = serverSock.accept();
				server.setKeepAlive(true);
				System.out.println("Connected");

				NodeInfo serverNodeInfo = new NodeInfo(server);
				serverNodeInfo.addNode(this.ip);
				serverNodeInfo.receiveDHT();
				serverNodeInfo.sendDHT();

				printDht(serverNodeInfo);
				
				bools[0] = requestOrSend(serverNodeInfo);
				System.out.println(requestOrSend(serverNodeInfo));
				if(requestOrSend(serverNodeInfo)) {
					sendMissing(server, serverNodeInfo);
				}else {
					missingFiles(server, serverNodeInfo);
				}
				server.close();		// closes the socket
				break;

//				compareFiles(serverNodeInfo, server);
				
//				newPort += 1;
//				Thread.sleep(1000);
//
////				server.close();		// closes the socket
//			}
		case "Client":
			
//			while(true) {
				System.out.println("Waiting for connection ...");
				Socket peer = null;	// creates client socket
				// checks every IP in the network for the peer that is acting as a server
				for (String ip : networkIps) {
					try {
						/* creates a stream socket and connects it to the 
						 * specified port number at the specified IP address */
						peer = new Socket(ip, newPort);
						System.out.println("Connected");

						NodeInfo peerNodeInfo = new NodeInfo(peer);
						peerNodeInfo.addNode(InetAddress.getLocalHost().getHostAddress().toString());
						peerNodeInfo.sendDHT();
						Thread.sleep(1000);
						peerNodeInfo.receiveDHT();
						printDht(peerNodeInfo);
						bools[1] = requestOrSend(peerNodeInfo);
						System.out.println(requestOrSend(peerNodeInfo));
						if(requestOrSend(peerNodeInfo)) {
							sendMissing(peer, peerNodeInfo);
						}else {
							missingFiles(peer, peerNodeInfo);
						}
//						compareFiles(peerNodeInfo, peer);
						peer.close();		// closes the socket
				
					} catch (ConnectException e) {
						// do nothing
					}
				}
			}
//		}
	}
	
	/**
	 * Prints dht
	 * @param nodeInfo
	 */
	private void printDht(NodeInfo nodeInfo) {
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	// gets the dht table
		System.out.println("\nDHT:");
		Enumeration dhtNames = dht.keys();
		while(dhtNames.hasMoreElements()) {
			String key = (String) dhtNames.nextElement();
			System.out.print("Key: " +key+" & Value: [ "); 
			for(int i = 0; i < dht.get(key).length; i++) {
				if(i == dht.get(key).length-1) {
					System.out.print(dht.get(key)[i].getName());
					break;
				}
				System.out.print(dht.get(key)[i].getName() + ", ");
			}
			System.out.print("]\n\n");

		}
	}
	
	
	private void missingFiles(Socket socket, NodeInfo nodeInfo) throws ClassNotFoundException, IOException, InterruptedException {
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	// gets the dht table
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		List<String> fileNames = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			fileNames.add(files.get(i).getName());
		}
		for(int j = 0; j < keys.length; j++) {
			// compares my files to the other nodes/peers in the network
			if(!keys[j].equals(InetAddress.getLocalHost().getHostAddress().toString())) {
				List<String> missingFileNames = new ArrayList();
				// compares my files to the peer/node
				for(File peerFile : dht.get(keys[j])) {
					// add the missing file to missingFileNames
					if(!fileNames.contains(peerFile.getName())) {
						missingFileNames.add(peerFile.getName());
					}
				}
				
				if(!missingFileNames.isEmpty()) {
					// iterates through the file name that I am missing
					for(String s : missingFileNames) {
						System.out.println("Requesting file ...");
						DataOutputStream out =new DataOutputStream(socket.getOutputStream());
						// sends message to peer/node
						out.writeUTF(s); 
						DataInputStream in= new
								DataInputStream(socket.getInputStream()); 
						String receivedMessage = in.readUTF();
						if(receivedMessage.equalsIgnoreCase("Sending")) {
							// I will receive my peer my file
							System.out.println("Receiving file ...");
							fileHandler.receiveFile(socket);
						}
					}
				}
				
				List<String> updatedMissingFileNames = new ArrayList();
				// compares my files to the peer/node
				for(File peerFile : dht.get(keys[j])) {
					// add the missing file to missingFileNames
					if(!fileNames.contains(peerFile.getName())) {
						updatedMissingFileNames.add(peerFile.getName());
					}
				}
				if(updatedMissingFileNames.isEmpty()) {
					DataOutputStream out2 =new DataOutputStream(socket.getOutputStream());
					out2.writeUTF("Exit");
					out2.flush();
				}else {
					// sends the "Done" message to peer/node
					DataOutputStream out3 =new DataOutputStream(socket.getOutputStream());
					out3.writeUTF("Done");
					out3.flush();
					System.out.println("DONE");
					
					// updates node with current files
					nodeInfo.addNode(InetAddress.getLocalHost().getHostAddress().toString());
					// sends updated dht
					nodeInfo.sendDHT();
					// gets the updated dht table
					Hashtable<String, File[]> updatedDht = nodeInfo.getDHT();
					sendMissing(socket, nodeInfo);
				}
			}
		}
	}
	
	private boolean requestOrSend(NodeInfo nodeInfo) throws UnknownHostException {
		boolean request = false;
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	// gets the dht table
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		List<String> fileNames = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			fileNames.add(files.get(i).getName());
		}
		for(int j = 0; j < keys.length; j++) {
			// compares my files to the other nodes/peers in the network
			if(!keys[j].equals(InetAddress.getLocalHost().getHostAddress().toString())) {
				List<File> peerFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[j])));
				List<String> peerFileNames = new ArrayList<>();
				for (int i = 0; i < peerFiles.size(); i++) {
					peerFileNames.add(peerFiles.get(i).getName());
				}
				if(peerFiles.size() > files.size()) {
					request = true;
				}else if(peerFiles.size() == files.size()){
					System.out.println("Waiting ...");
					requestOrSend(nodeInfo);
				}else {
					request = false;
				}
			}
//				for(File f: files) {	// checks my files
//					if(!peerFileNames.contains(f.getName())) {
//						// I need to send my peer my file
//						request = false;
//					}
//				}
//				for(File f: peerFiles) {	// checks files in peer files
//					if(!fileNames.contains(f.getName())) {
//						// I will receive the file from my peer
//						request = true;
//					}
//				}
				
//			}
		}
		return request;
	}
	
	private void sendMissing(Socket socket, NodeInfo nodeInfo) throws IOException, ClassNotFoundException, InterruptedException {
		while(true) {
			DataInputStream in= new
					DataInputStream(socket.getInputStream()); 
			String fileName = in.readUTF();
			System.out.println(fileName);
			if(fileName.equalsIgnoreCase("Done")) {
				System.out.println("DONE");
				nodeInfo.receiveDHT();
				// checks my missing files now
				missingFiles(socket, nodeInfo);
			}else if(fileName.equalsIgnoreCase("Exit")) {
				// closes my socket
				socket.close();
			}else {
				System.out.println("Sending file ...");
				// Sending message to peer/node
				DataOutputStream out =new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Sending");
				out.flush();
				// creating file and sending the file
				String path = fileHandler.getPath();
				File tempFile = new File(path + File.separator + fileName);
				List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
				File file = files.get(files.indexOf(tempFile));
				fileHandler.sendFile(socket, file);
			}
		}		
	}
	
	
	private void compareFiles(NodeInfo nodeInfo, Socket socket) throws IOException, InterruptedException, ClassNotFoundException {
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	// gets the dht table
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		// stores a list of files from my device
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		List<String> fileNames = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			fileNames.add(files.get(i).getName());
		}

		for(int j = 0; j < keys.length; j++) {
			// compares my files to the other nodes/peers in the network
			if(!keys[j].equals(this.ip)) {
				// stores a list of files from my device
				List<File> peerFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[j])));
				List<String> peerFileNames = new ArrayList<>();
				for (int i = 0; i < peerFiles.size(); i++) {
					peerFileNames.add(peerFiles.get(i).getName());
				}
				for(File f: files) {	// checks my files
					if(peerFileNames.contains(f.getName())) {
						// my peer has the same file name in their list of files

					}else {
						// I will send my peer my file
						fileHandler.sendFile(socket, f);
					}
				}
				
				for(File f: peerFiles) {	// checks files in peer files
					if(fileNames.contains(f.getName())) {
						// I have the same file name in my list of files

					}else {
						// I will receive the file from my peer
						fileHandler.receiveFile(socket);
					}
				}
			}
		}
	}
		
	@Override
	public void run() {
		while(true) {
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



}
