package p2p;

import java.util.concurrent.ConcurrentSkipListSet;

public class Program {

	public static void main(String[] args) throws Exception {
		// gets all ip addresses on the network
		IpScanner ipScanner = new IpScanner();	// create IpScanner object
		// scans the network to get a list of IP addresses
		ConcurrentSkipListSet<String> networkIps = ipScanner.scan("192.168.0.0", 254);
		// prints IP addresses that are reachable out to the console
        System.out.println("Devices connected to the network:");
        networkIps.forEach(ip -> System.out.println(ip));
	}

}
