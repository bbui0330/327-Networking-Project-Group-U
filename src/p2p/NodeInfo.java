package p2p;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		if(dht.containsKey(ip)) {
			updateNode(ip);
			return;
		}
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
		// checks if the list of files in the dht are the same as the directory
		if(dht.get(ip) != files) {
			// updates dht if the list is incorrect
			dht.replace(ip, dht.get(ip), files);
		}
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
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(dht);
        oos.flush();
	}

	/**
	 * Receives the DHT
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void receiveDHT() throws ClassNotFoundException {
		System.out.println("Line 1");
		InputStream is;
		try {
			is = socket.getInputStream();
			System.out.println("Line 2");
			ObjectInputStream ois = new ObjectInputStream(is);
			System.out.println("Line 3");
			Hashtable<String, File[]> temp = (Hashtable<String, File[]>) ois.readObject();
			System.out.println("Line 4");
			for(String s: temp.keySet()) {
				if(!dht.containsKey(s)) {
					dht.put(s, temp.get(s));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
