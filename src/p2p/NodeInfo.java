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
	
	public NodeInfo(Socket socket) {
		this.socket = socket;
		dht = new Hashtable<>();
	}
	
	public void addNode(Node n) {
		String ip = n.ip;
		File[] files = n.fileHandler.getListofFiles();
		dht.put(ip, files);
	}
	
	public void updateNode(Node n) {
		String ip = n.ip;
		File[] files = n.fileHandler.getListofFiles();
		if(dht.get(ip) != files) {
			dht.replace(ip, dht.get(ip), files);
		}
	}
	
	public Hashtable<String, File[]> getDHT(){
		return dht;
	}
	
	public void sendDHT() throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(dht);
	}

	public void receiveDHT() throws IOException, ClassNotFoundException {
		InputStream is = socket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is);
		Hashtable<String, File[]> temp = (Hashtable) ois.readObject();
		for(String s: temp.keySet()) {
			if(!dht.containsKey(s)) {
				dht.put(s, temp.get(s));
			}
		}
	}
}
