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

			while(true) {
				// creates a server socket, bound to the specified port
				ServerSocket serverSock = new ServerSocket(port);
				
				Socket server = serverSock.accept();
				System.out.println("Connected");

				NodeInfo serverNodeInfo = new NodeInfo(server);
				serverNodeInfo.addNode(this.ip);
				serverNodeInfo.receiveDHT();
				serverNodeInfo.sendDHT();
				server.close();		// closes the socket
			
				fileComparison(serverNodeInfo, server);
//				server.close();		// closes the socket
			}
		case "Client":
			System.out.println("Waiting for connection ...");
			Socket peer = null;	// creates client socket
			// checks every IP in the network for the peer that is acting as a server
			for (String ip : networkIps) {
				try {
					while(true) {
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
						fileComparison(peerNodeInfo, peer);
					}
//					Hashtable<String, File[]> dht = peerNodeInfo.getDHT();
//					// stores the list of keys (IP addresses)
//					String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
//					for(int j = 0; j < keys.length; j++) {
//						while(true) {
//							fileComparison(peerNodeInfo, peer);
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
	 * @throws ClassNotFoundException 
	 */
	private void fileComparison(NodeInfo nodeInfo, Socket socket) throws IOException, InterruptedException, ClassNotFoundException {
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
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		// stores the absolute path of the folder
		String path = fileHandler.getPath();
		for(int j = 0; j < keys.length; j++) {
			// compares my files to the other nodes/peers in the network
			if(!keys[j].equals(this.ip)) {
				// does comparison while the list of files are not equal
				while(!dht.get(keys[j]).equals(files)) {
					// my peer has more files than I do
					if(dht.get(keys[j]).length >= files.size()) {
						for(File f: dht.get(keys[j])) {
							// changes absolute path to my absolute path to check if I have the file
							File temp = new File(path + File.separator + f.getName());
							// my files list has the same file as my peer
							if(files.contains(temp)) {
								//Getting the last modified time
								long fileLastModified = f.lastModified();
								long myFileLastModified = files.get(files.indexOf(temp)).lastModified();
								// if peer/node has a more recently modified version
								if(fileLastModified > myFileLastModified) {
//							    	fileHandler.requestFile(socket, f.getName());
////							    	sleep(1000);
									fileHandler.receiveFile(socket, f);
								}else {
									fileHandler.sendFile(socket, files.get(files.indexOf(temp)));
									}
//								if(!fileHandler.compareFiles(files.get(files.indexOf(temp)), f)) {
//									//Getting the last modified time
//									long fileLastModified = f.lastModified();
//									long myFileLastModified = files.get(files.indexOf(temp)).lastModified();
//									// if peer/node has a more recently modified version
//									if(fileLastModified > myFileLastModified) {
////							    	fileHandler.requestFile(socket, f.getName());
//////							    	sleep(1000);
//										fileHandler.receiveFile(socket, f);
//									}else {
//										fileHandler.sendFile(socket, files.get(files.indexOf(temp)));
//									}
//								}
							}else {	// I do not have the file
//							fileHandler.requestFile(socket, f.getName());
////					    	sleep(1000);
								fileHandler.receiveFile(socket, f);
								System.out.println("I copied your file");
							}
							nodeInfo.updateNode(InetAddress.getLocalHost().getHostAddress().toString());
							System.out.println("Node has been updated");
							nodeInfo.sendDHT();
							Thread.sleep(1000);
							nodeInfo.receiveDHT();
							
						}

					}else {
						// gets the first file within the peers File list
						File tempFileForDhtPath = dht.get(keys[j])[0];
						// gets the path of the first file
						Path pathForDht = Paths.get(tempFileForDhtPath.toURI());
						// gets the parent path (or folder absolute path) of the file 
						// and converts to a string
						String pathForDhtString = pathForDht.getParent().toString();
						// stores a list of files from my device
						List<File> peerFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[j])));
						for(int n = 0; n < files.size(); n++) {
							System.out.println("Checking my files" + files.size());
							File temp = new File(pathForDhtString + File.separator + files.get(n).getName());
							if(peerFiles.contains(temp)) {
								System.out.println("You have my file");
								//Getting the last modified time
								long myFileLastModified = files.get(n).lastModified();
								long fileLastModified = dht.get(keys[j])[n].lastModified();
								// if peer/node has a more recently modified version
								if(fileLastModified > myFileLastModified) {
//							    	fileHandler.requestFile(socket, f.getName());
////							    	sleep(1000);
									fileHandler.receiveFile(socket, temp);
									System.out.println("Updated to my file");
								}else{
									fileHandler.sendFile(socket, dht.get(keys[j])[n]);
									System.out.println("Updated to your file");
								}
//								if(!fileHandler.compareFiles(dht.get(keys[j])[n], files.get(n))) {
//									//Getting the last modified time
//									long myFileLastModified = files.get(n).lastModified();
//									long fileLastModified = dht.get(keys[j])[n].lastModified();
//									// if peer/node has a more recently modified version
//									if(fileLastModified > myFileLastModified) {
////							    	fileHandler.requestFile(socket, f.getName());
//////							    	sleep(1000);
//										fileHandler.receiveFile(socket, temp);
//										System.out.println("Updated to my file");
//									}else {
//										fileHandler.sendFile(socket, dht.get(keys[j])[n]);
//										System.out.println("Updated to your file");
//									}
//								}
							}else {	// You do not have the file
//							fileHandler.requestFile(socket, f.getName());
////					    	sleep(1000);
								System.out.println("I am sending you my file");
								fileHandler.sendFile(socket, dht.get(keys[j])[n]);
								System.out.println("I sent you my file");
							}
							nodeInfo.updateNode(InetAddress.getLocalHost().getHostAddress().toString());
							System.out.println("Node has been updated");
							nodeInfo.sendDHT();
							Thread.sleep(1000);
							nodeInfo.receiveDHT();
						}
//						for(int m = 0; m < keys.length; m++) {
//							// compares my files to the other nodes/peers in the network
//							List<File> dhtFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[m])));
//							Path peerPath = Paths.get(dhtFiles.get(0).toURI()); 
//							for(int k = 0; k < files.size(); k++) {
//								System.out.println("Checking all your files");
//								File tempFile = new File(peerPath + File.separator + files.get(k).getName());
//								if(dhtFiles.contains(tempFile)) {
//									System.out.println("This file is in here");
//								}
//								else {
//									System.out.println("This file is NOT in here");
//									fileHandler.sendFile(socket, files.get(k));
//								}
//							}
//							nodeInfo.updateNode(InetAddress.getLocalHost().getHostAddress().toString());
//							System.out.println("Node has been updated");
//						}
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
