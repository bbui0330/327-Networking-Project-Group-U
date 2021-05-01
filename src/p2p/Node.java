package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;

public class Node {
	
	private int port;
	private Path path;
	private List<Node> nodes;
	
	public Node(int port, String path) {
		this.port = port;
        this.path = Paths.get(path);
	}

	
	public void receiveFile() throws IOException {
		// creates a socket for the server
		ServerSocket serverSocket = new ServerSocket(port);	
		// creates a socket for the client
		Socket client = serverSocket.accept();
		System.out.println("Connection Set:  " + client.getRemoteSocketAddress());

	}
	
	public void sendFile() throws IOException {
		
	}
}
