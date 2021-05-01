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
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	FileHandler fileHandler;
	int port;
	String ip;
	String path = System.getProperty("user.dir");	// directory user is in
	String folderPath = path + File.separator + "files";
	File directoryPath = new File(folderPath);

	/**
	 * Constructor
	 * @param type: determine whether the peer is acting as client or server
	 * @param port: port number
	 * @param networkIps: list of IP addresses in the network
	 * @throws IOException
	 */
	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		switch (type) {
		case "Server":
			this.port = port;	// sets the port
			// sets IP address to the IP address of this device
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			// creates a server socket, bound to the specified port
			ServerSocket serverSock = new ServerSocket(port);
			
			Socket server = serverSock.accept();
			System.out.println("Connected");

			// receive file
			receiveFile(server);

			server.close();		// closes the socket
			break;
		case "Client":
			this.port = port;	// sets the port
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
					sendFile(client);
					
					client.close();		// closes the socket
				} catch (ConnectException e) {
					// do nothing
				}
			}
			break;
		}

	}

	/**
	 * Creates a folder if the folder does not already exist
	 */
	private void setDirectory() {
		// checks if the folder exists or not
		if(!directoryPath.exists()) {
			// if the folder doesn't exist, it creates one
			directoryPath.mkdir();
		}
	}

	/**
	 * Returns the folder path
	 * @return absolute path of the folder
	 */
	private String getPath() {
		// returns the absolute path of the folder
		return folderPath;
	}

	/**
	 * Gets all the files in the specified directory
	 * @return list of files
	 */
	private File[] getListofFiles() {
		// makes sure that the folder exists
		setDirectory();
		// returns a list of Files in the specified directory
		return directoryPath.listFiles();
	}
	
	/**
	 * Sends files to another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @throws IOException
	 */
	private void sendFile(Socket socket) throws IOException {
		/* creates a BufferedOutputStream to write data for the socket
		 * output stream
		 * socket.getOutputStream() - output stream for the socket	*/
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		
		// creates a DataOutputStream to write data for BufferedOutputStream
		DataOutputStream dos = new DataOutputStream(bos);
		
		// writes the number of files to DataOutputStream 
		// writeInt() - writes int as 4 bytes, high byte first
		dos.writeInt(getListofFiles().length);

		// iterates through all the files in the specified directory
		for(File file : getListofFiles())
		{
		    long length = file.length();	// gets the file length
		    // writes the size of the file to DataOutputStream
		    // writeLong() - writes long as 8 bytes, high byte first
		    dos.writeLong(length);

		    String name = file.getName();	// gets the file name
		    // writes the name of the file to DataOutputStream
		    // writeUTF() - writes string using modified UTF-8 encoding
		    dos.writeUTF(name);

		    // Creates a FileInputStream by opening a connection to the file
		    FileInputStream fis = new FileInputStream(file);
		    // Creates a BufferedInputStream and saves fis for later use
		    BufferedInputStream bis = new BufferedInputStream(fis);

		    int theByte = 0;
		    
		    // read returns -1 at the end-of-file
		    // bis.read() - the number of bytes read
		    while((theByte = bis.read()) != -1) {
		    	// writes the specified byte to this buffered output stream
		    	bos.write(theByte);
		    }

		    bis.close();	// closes BufferedInputStream
		    System.out.println("Sent " + name);
		}

		dos.close();	// closes DataOutputStream
	}

	/**
	 * Receives the files from another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @throws IOException
	 */
	private void receiveFile(Socket socket) throws IOException {
		// Creates a BufferedInputStream and saves input stream for later use
		// socket.getInputStream() - returns an input stream for the socket
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		// Creates a DataInputStream that uses the BufferedInputStream
		DataInputStream dis = new DataInputStream(bis);

		int filesCount = dis.readInt();	// gets the number of files
		// creates a list that is the same size as the number of files received
		File[] files = new File[filesCount];
		
		for(int i = 0; i < filesCount; i++) {
			long fileLength = dis.readLong();	// gets the file size
		    String fileName = dis.readUTF();	// gets the file name

		    // creates a new file and adds it to files list
		    files[i] = new File(getPath() + File.separator + fileName);

		    // Creates FileOutputStream to write to the file
		    FileOutputStream fos = new FileOutputStream(files[i]);
		    // Creates BufferedOutputStream to write data to FileOutputStream
		    BufferedOutputStream bos = new BufferedOutputStream(fos);

		    for(int j = 0; j < fileLength; j++) {
		    	// Writes the specified byte to the BufferedOutputStream
		    	bos.write(bis.read());
		    }
		    
		    // prints the files that have been received
		    System.out.println("File " + fileName + " downloaded");

		    bos.close();	// closes BufferedOutputStream
		}
	}
	
}
