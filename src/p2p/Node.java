package p2p;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Node {
	
	
	/*
	 * private int port; private Path path; private List<Node> nodes;
	 * 
	 * public Node(int port, String path) { this.port = port; this.path =
	 * Paths.get(path); }
	 * 
	 * 
	 * public void receiveFile() throws IOException { // creates a socket for the
	 * server ServerSocket serverSocket = new ServerSocket(port);
	 * System.out.println("Listening:"); // creates a socket for the client Socket
	 * client = serverSocket.accept(); System.out.println("Connection Set:  " +
	 * client.getRemoteSocketAddress());
	 * 
	 * }
	 * 
	 * public void sendFile() throws IOException {
	 * 
	 * }
	 */
	 
	private int port;
    private ArrayList<Node> contacts;
    Node preNode;
    Node postNode;
    private String directoryLocation = "";

    Node(int port)
    {
        this.port = port;
        this.setDirectoryLocation( port+"");
//        startClientServer( port );
        
        new Thread(new Runnable() {
	        public void run(){
	        	startClientServer( port );
	        }
        }).start();
        
    }
    
    public void setDirectoryLocation(String directoryLocation) {
    	this.directoryLocation = directoryLocation;
    }

    private void sendRequest(String fileName, String host, int port) throws UnknownHostException, IOException
    {
        Socket socket = new Socket(host, port);//machine name, port number
        PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
        out.println(fileName);

        out.close();
        socket.close();

    }

    private void startClientServer( int portNum )
    {
        try 
        {
            // Establish the listen socket.
            ServerSocket server = new ServerSocket(0);
            System.out.println("listening on port " + server.getLocalPort());

            while( true )
            {
                // Listen for a TCP connection request.
                Socket connection = server.accept();

                // Construct an object to process the HTTP request message.
                HttpRequestHandler request = new HttpRequestHandler( connection );

                // Create a new thread to process the request.
                Thread thread = new Thread(request);

                // Start the thread.
                thread.start();

                System.out.println("Thread started for "+ portNum);
            }

        } 
        catch (Exception e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
