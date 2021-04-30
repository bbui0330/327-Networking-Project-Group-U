package p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class IPAddress {
	long initialT = System.currentTimeMillis();
	ArrayList<String> ipAddresses = new ArrayList<>(); 

    public void checkHosts(String subnet) throws Exception{
   int timeout=1000;
   //int port= 139;	//139;
   for (int i=1;i<255;i++){
       String host=subnet + "." + i;
       //if(isReachableByTcp(host, port, timeout)) {
       if (InetAddress.getByName(host).isReachable(timeout)){
    	   ipAddresses.add(host);
           System.out.println(host + " is reachable");
       }
   }
   long finalT = System.currentTimeMillis();
   System.out.println("Scan Completed taking " + (finalT - initialT) + " miliseconds approximately!");
    }
    
    public ArrayList<String> getIpAddresses(){
    	return ipAddresses;
    }
    
    public static boolean isReachableByTcp(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, timeout);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
