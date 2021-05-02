package p2p;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
			
			while(true) {
				fileComparison(serverNodeInfo, server);
//				server.close();		// closes the socket
			}

//			server.close();		// closes the socket
//			break;
		case "Client":
			System.out.println("Waiting for connection ...");
			Socket peer = null;	// creates client socket
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
					System.out.println("almost there");
					peerNodeInfo.receiveDHT();
					System.out.println("maybe not");
//					Hashtable<String, File[]> dht = peerNodeInfo.getDHT();
//					// stores the list of keys (IP addresses)
//					String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
//					for(int j = 0; j < keys.length; j++) {
//						while(true) {
					fileComparison(peerNodeInfo, peer);
					System.out.println("I can make it");
					//							if(dht.get(InetAddress.getLocalHost().getHostAddress().toString()).equals(dht.get(keys[j]))) {
					//								break;
					//							}
					//						}
					//					}
				} catch (ConnectException e) {
					// do nothing
				}
			}
			peer.close();		// closes the socket
			break;
		}
	}
	
	/**
	 * Compares the files
	 * @param nodeInfo
	 * @param socket
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void fileComparison(NodeInfo nodeInfo, Socket socket) throws IOException, InterruptedException {
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
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		// stores a list of files from my device
		List<File> files = new ArrayList<File>(Arrays.asList(dht.get(InetAddress.getLocalHost().getHostAddress().toString())));
		// stores the absolute path of the folder
		String path = fileHandler.getPath();
		for(int j = 0; j < keys.length; j++) {
			// compares my files to the other nodes/peers in the network
			if(!keys[j].equals(this.ip)) {
				if(dht.get(keys[j]).length >= files.size()) {
					for(File f: dht.get(keys[j])) {
						// changes absolute path to my absolute path to check if I have the file
						File temp = new File(path + File.separator + f.getName());
						if(files.contains(temp)) {
							if(!fileHandler.compareFiles(files.get(files.indexOf(temp)), f)) {
								//Getting the last modified time
							    long fileLastModified = f.lastModified();
							    long myFileLastModified = files.get(files.indexOf(temp)).lastModified();
							    // if peer/node has a more recently modified version
							    if(fileLastModified > myFileLastModified) {
//							    	fileHandler.requestFile(socket, f.getName());
////							    	sleep(1000);
							    	fileHandler.receiveFiles(socket);
							    	System.out.println("Updated to your file");
							    }else {
							    	fileHandler.sendFiles(socket, files.get(files.indexOf(temp)));
							    	System.out.println("You have my file");
							    }
							}
						}else {	// I do not have the file
//							fileHandler.requestFile(socket, f.getName());
////					    	sleep(1000);
					    	fileHandler.receiveFiles(socket);
					    	System.out.println("I copied your file");
						}
						nodeInfo.updateNode(InetAddress.getLocalHost().getHostAddress().toString());
						System.out.println("Node has been updated");
					}
				}else {
					int l = -1;
					for(int k = 0; k < files.size(); k++) {
						try {
							List<File> dhtFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[k])));
							File tempFile = new File(fileHandler.getPath() + File.separator + dhtFiles.get(k).getName());
							if(files.contains(tempFile)) {
								System.out.println("This file is in here");
							}
							else {
								System.out.println("This file is NOT in here");
								fileHandler.sendFiles(socket, files.get(k));
							}
						}catch(ArrayIndexOutOfBoundsException e) {
							System.out.println("This file is NOT in here");
							System.out.println("I am here");
							fileHandler.sendFiles(socket, files.get(k));
							l = k+1;
							continue;
						}
						for(int n = l; n < files.size(); n++) {
							System.out.println("This file is NOT in here");
							System.out.println("I am here in the next for");
							fileHandler.sendFiles(socket, files.get(k));
						}
//						// changes absolute path to my absolute path to check if I have the file
//						File temp = new File(path + File.separator + files.get(k).getName());
//						for(File f: dht.get(keys[k])) {
//							if(files.contains(f)) {
//								if(!fileHandler.compareFiles(f, files.get(k))) {
//									//Getting the last modified time
//									long fileLastModified = f.lastModified();
//									long myFileLastModified = files.get(files.indexOf(temp)).lastModified();
//									// if peer/node has a more recently modified version
//									if(fileLastModified > myFileLastModified) {
//										fileHandler.requestFile(socket, f.getName());
//										//							    	sleep(1000);
//										fileHandler.receiveFile(socket, f);
//										System.out.println("Updated to your file");
//									}else {
//										fileHandler.sendFile(socket, files.get(files.indexOf(temp)));
//										System.out.println("You have my file");
//									}
//								}
//							}else {	// I do not have the file
//								fileHandler.requestFile(socket, files.get(k).getName());
//								//					    	sleep(1000);
//								fileHandler.receiveFile(socket, files.get(k));
//								System.out.println("I copied your file");
//							}
//						}
						nodeInfo.updateNode(InetAddress.getLocalHost().getHostAddress().toString());
						System.out.println("Node has been updated");
					}
				}
			}
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
