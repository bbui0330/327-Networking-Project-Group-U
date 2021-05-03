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
		switch (type) {
		case "Server": 
			// sets IP address to the IP address of this device
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();

			// creates a server socket, bound to the specified port
			ServerSocket serverSock = new ServerSocket(port);

			// Listens for a connection to be made to this socket and acceptsit
			Socket server = serverSock.accept();
			
			server.setKeepAlive(true);
			System.out.println("Connected");

			// create a NodeInfo object for the server
			NodeInfo serverNodeInfo = new NodeInfo(server);
			// adds my IP to the dht
			serverNodeInfo.addNode(this.ip);
			// receives dht from peer
			serverNodeInfo.receiveDHT();
			// sends updated dht to peer
			serverNodeInfo.sendDHT();
			// prints dht
			printDht(serverNodeInfo);

			// determines whether I should receive or send files
			if(!requestOrSend(serverNodeInfo)) {
				// send peer missing files
				sendMissing(server, serverNodeInfo);
			}else {
				// receive missing files
				missingFiles(server, serverNodeInfo);
			}
			server.close();		// closes the socket
			break;

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

					// create a NodeInfo object for the peer
					NodeInfo peerNodeInfo = new NodeInfo(peer);
					// adds my IP to the dht
					peerNodeInfo.addNode(InetAddress.getLocalHost().getHostAddress().toString());
					// sends dht to peer
					peerNodeInfo.sendDHT();
					Thread.sleep(1000);
					// receives dht from peer
					peerNodeInfo.receiveDHT();
					// prints dht
					printDht(peerNodeInfo);

					// determines whether I should receive or send files
					if(!requestOrSend(peerNodeInfo)) {
						// send peer missing files
						sendMissing(peer, peerNodeInfo);
					}else {
						// receive missing files
						missingFiles(peer, peerNodeInfo);
					}
					peer.close();		// closes the socket
				} catch (ConnectException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Prints dht
	 * @param nodeInfo
	 */
	private void printDht(NodeInfo nodeInfo) {
		// gets the dht table
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	
		System.out.println("\nDHT:");	
		// gets an enumeration of dht keys
		Enumeration dhtNames = dht.keys();	
		// iterates through the enumeration
		while(dhtNames.hasMoreElements()) {	
			// stores the key as a string value
			String key = (String) dhtNames.nextElement();
			// prints out the key and value
			System.out.print("Key: " +key+" & Value: [ "); 	
			// iterates through the File[] for the key
			for(int i = 0; i < dht.get(key).length; i++) {	
				// if the file is the last element in the File[]
				if(i == dht.get(key).length-1) {
					// prints file name without a comma at the end
					System.out.print(dht.get(key)[i].getName());	
					break;
				}
				// prints each file name
				System.out.print(dht.get(key)[i].getName() + ", ");	
			}
			// prints the ending bracket and adds new lines
			System.out.print("]\n\n");	
		}
	}

	/**
	 * Checks my files against my peer to see if I am missing any files
	 * @param socket: socket associated with the current node/peer
	 * @param nodeInfo: NodeInfo associated with the current node/peer
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void missingFiles(Socket socket, NodeInfo nodeInfo) throws ClassNotFoundException, IOException, InterruptedException {
		// gets the dht table
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		// stores File[] from my device as List<File>
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		// creates a list of file names
		List<String> fileNames = new ArrayList<>();
		// iterates through my list of files
		for (int i = 0; i < files.size(); i++) {
			// gets file name and adds to fileNames list
			fileNames.add(files.get(i).getName());
		}
		// iterates through the list of keys (IP addresses)
		for(int j = 0; j < keys.length; j++) {
			// checks to see if the key is equal to my IP address
			if(!keys[j].equals(InetAddress.getLocalHost().getHostAddress().toString())) {
				// creates a list of missing file names
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
						// creates a DataOutputStream to write data for the socket output stream
						DataOutputStream out =new DataOutputStream(socket.getOutputStream());
						// sends message to peer/node
						out.writeUTF(s); 
						// creates a DataInputStream for the socket input stream
						DataInputStream in= new
								DataInputStream(socket.getInputStream()); 
						// saves the read message
						String receivedMessage = in.readUTF();
						// checks against a string that will be 
						// received once peer sends a file
						if(receivedMessage.equalsIgnoreCase("Sending")) {
							System.out.println("Receiving file ...");
							// Receive my peer file
							fileHandler.receiveFile(socket);
						}
					}
				}

				// creates a list of updated missing file name list 
				List<String> updatedMissingFileNames = new ArrayList();
				// compares my files to the peer/node
				for(File peerFile : dht.get(keys[j])) {
					// add the missing file to missingFileNames
					if(!fileNames.contains(peerFile.getName())) {
						updatedMissingFileNames.add(peerFile.getName());
					}
				}
				// if the 
				if(updatedMissingFileNames.isEmpty()) {
					// creates a DataOutputStream to write data for the socket output stream
					DataOutputStream out2 =new DataOutputStream(socket.getOutputStream());
					out2.writeUTF("Exit");
					// Flushes DataOutputStream
					// Forces any buffered OutputBytes to be written to stream
					out2.flush();
				}else {
					// creates a DataOutputStream to write data for the socket output stream
					DataOutputStream out3 =new DataOutputStream(socket.getOutputStream());
					// sends the "Done" message to peer/node
					out3.writeUTF("Done");
					// Flushes DataOutputStream
					// Forces any buffered OutputBytes to be written to stream
					out3.flush();

					// updates node with current files
					nodeInfo.addNode(InetAddress.getLocalHost().getHostAddress().toString());
					// sends updated dht
					nodeInfo.sendDHT();
					// gets the updated dht table
					Hashtable<String, File[]> updatedDht = nodeInfo.getDHT();
					// goes into the sendMissing function
					sendMissing(socket, nodeInfo);
				}
			}
		}
	}

	/**
	 * Determines whether the peer should receive files or send files
	 * @param nodeInfo: NodeInfo associated with the current peer/node
	 * @return true if you need to receive files, false if you need to send files
	 * @throws UnknownHostException
	 */
	private boolean requestOrSend(NodeInfo nodeInfo) throws UnknownHostException {
		// initializes the boolean to false
		boolean request = false;
		// gets the dht table
		Hashtable<String, File[]> dht = nodeInfo.getDHT();	
		// stores the list of keys (IP addresses)
		String[] keys = dht.keySet().toArray(new String[dht.keySet().size()]);
		// stores File[] from my device as List<File>
		List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
		// creates a list of file names
		List<String> fileNames = new ArrayList<>();
		// iterates through my list of files
		for (int i = 0; i < files.size(); i++) {
			// gets file name and adds to fileNames list
			fileNames.add(files.get(i).getName());
		}
		// iterates through the list of keys (IP addresses)
		for(int j = 0; j < keys.length; j++) {
			// checks to see if the key is equal to my IP address
			if(!keys[j].equals(InetAddress.getLocalHost().getHostAddress().toString())) {
				// stores File[] for my peer as List<File>
				List<File> peerFiles = new ArrayList<File>(Arrays.asList(dht.get(keys[j])));
				// creates a list of file names for my peer
				List<String> peerFileNames = new ArrayList<>();
				// iterates through the list of files for my peer
				for (int i = 0; i < peerFiles.size(); i++) {
					// gets file name and adds to peerFileNames list
					peerFileNames.add(peerFiles.get(i).getName());
				}
				// checks if peer has more files than me
				if(peerFiles.size() > files.size()) {
					// sets boolean to true
					request = true;
				}else if(peerFiles.size() == files.size()){
					run();	// reruns the program
				}else {
					// sets boolean to false
					request = false;
				}
			}
		}
		return request;
	}

	/**
	 * Sends my peer the files that they are missing
	 * @param socket: socket associated with the current node/peer
	 * @param nodeInfo: NodeInfo associated with the current node/peer
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	private void sendMissing(Socket socket, NodeInfo nodeInfo) throws IOException, ClassNotFoundException, InterruptedException {
		while(true) {
			// creates a DataInputStream for the socket input stream
			DataInputStream in= new
					DataInputStream(socket.getInputStream()); 
			// saves the read message
			String fileName = in.readUTF();
			
			if(fileName.equalsIgnoreCase("Done")) {
				System.out.println("DONE");
				// receives dht from peer
				nodeInfo.receiveDHT();
				// checks my missing files now
				missingFiles(socket, nodeInfo);
			}else if(fileName.equalsIgnoreCase("Exit")) {
				// closes my socket
				socket.close();
			}else {
				// prints the read message
				System.out.println(fileName);
				System.out.println("Sending file ...");
				// creates a DataOutputStream to write data for the socket output stream
				DataOutputStream out =new DataOutputStream(socket.getOutputStream());
				// Sends message to peer/node
				out.writeUTF("Sending");
				// Flushes DataOutputStream
				// Forces any buffered OutputBytes to be written to stream
				out.flush();
				
				// creating file and sending the file
				// stores my path of the folder
				String path = fileHandler.getPath();
				// gets the fileName and adds my path to find file
				File tempFile = new File(path + File.separator + fileName);
				// creates a list of my files
				List<File> files = new ArrayList<File>(Arrays.asList(fileHandler.getListofFiles()));
				// gets the index of tempFile and gets that file from my list of files
				File file = files.get(files.indexOf(tempFile));
				// send my file to my peer
				fileHandler.sendFile(socket, file);
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
