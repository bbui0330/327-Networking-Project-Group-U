package p2p;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author bryb
 *
 */
public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		IpScanner ipScanner = new IpScanner();
		ConcurrentSkipListSet networkIps = ipScanner.scan("192.168.0.0", 254);
        System.out.println("Devices connected to the network:");
        networkIps.forEach(ip -> System.out.println(ip));
	}

}
