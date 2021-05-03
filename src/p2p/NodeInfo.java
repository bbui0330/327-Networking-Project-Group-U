package p2p;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;

public class NodeInfo {
	Socket socket;
	Hashtable<String, File[]> dht;
	
	/**
	 * Constructor
	 * @param socket: socket associated with the current node/peer
	 */
	public NodeInfo(Socket socket) {
		this.socket = socket;
		dht = new Hashtable<>();
	}
	
	/**
	 * Adds a node to the DHT 
	 * @param ip: IP address associated with the current node/peer
	 */
	public void addNode(String ip) {
		FileHandler fileHandler = new FileHandler();
		File[] files = fileHandler.getListofFiles();
		dht.put(ip, files);
	}
	
	/**
	 * Updates the list of files for a specified key
	 * @param ip: IP address associated with the current node/peer
	 */
	public void updateNode(String ip) {
		FileHandler fileHandler = new FileHandler();
		File[] files = fileHandler.getListofFiles();
		// updates the dht
		dht.put(ip, files);
	}
	
	/**
	 * Returns DHT
	 * @return dht for the Nodes
	 */
	public Hashtable<String, File[]> getDHT(){
		return dht;
	}
	
	/**
	 * Sends the DHT
	 * @throws IOException
	 */
	public void sendDHT() throws IOException {
		// creates a ObjectOutputStream to write object for the socket output stream
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		// write the dht to ObjectOutputStream
        oos.writeObject(dht);
        // Flushes the stream
        // Forces any buffered output bytes to be written out to the stream  
        oos.flush();
	}

	/**
	 * Receives the DHT
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void receiveDHT() throws IOException, ClassNotFoundException {
		// creates a ObjectInputStream to read object for the socket input stream
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		Hashtable<String, File[]> temp = (Hashtable<String, File[]>) ois.readObject();
		// iterates through the keys of the received dht
		for(String s: temp.keySet()) {
			dht.put(s, temp.get(s));	// updated the values of the key
		}
	}
}
