package p2p;

import java.net.Socket;
import java.util.ArrayList;

public class ClientServer {
	
	public ClientServer(int numberOfNodes)
    {
        ArrayList<Node> arrayOfNodes = createNodes( numberOfNodes );
    }

    public static ArrayList<Node> createNodes(int count)
    {
        System.out.println("Creating a network of "+ count + " nodes...");
        ArrayList< Node > arrayOfNodes = new ArrayList<Node>();

        for( int i =1 ; i<=count; i++)
        {
            arrayOfNodes.add( new Node( 0 ) ); //providing 0, will take any free node
        }
        return arrayOfNodes;
    }
}
