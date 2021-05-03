package p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class IpScanner {
	 
    /**
     * Scans the network in order to get all the IP addresses on the network.
     * @param networkId: the network id, or the subnet (i.e. 192.168.0)
     * @param numOfIps: the number of IP addresses that will be tested (i.e. 255)
     * @return ConcurrentSkipListSet: the list of IP addresses
     */
    public ConcurrentSkipListSet<String> scan(String networkId, int numOfIps) {
        // Creates a thread pool that reuses a fixed number of threads (100)
    	ExecutorService executorService = Executors.newFixedThreadPool(255);
        // stores all the IP addresses in a ConcurrentSkipListSet
        ConcurrentSkipListSet<String> ipsSet = new ConcurrentSkipListSet<>();

        // creates a new AtomicInteger with the initial value of 0
        AtomicInteger ips = new AtomicInteger(0);
        // timeout
        int timeout = 1000;
        
        
        while (ips.get() <= numOfIps) {
        	// ips.getAndIncrement() automatically increments the current value by one
            String ip = networkId + ips.getAndIncrement();
            // an asynchronous execution mechanism which is capable of executing tasks 
            // concurrently in the background
            executorService.submit(() -> {
                try {
                	// Determines the IP address of ip
                    InetAddress inAddress = InetAddress.getByName(ip);
                    if (inAddress.isReachable(timeout)) { 
                    	// adds the IP address to the array if reachable
                        ipsSet.add(ip);	
                    }
                }
                catch (IOException e) {

                }
            });
        }
        // A shutdown in which previously submitted tasks are executed
        // but no new tasks will be accepted
        executorService.shutdown();
        try {
        	// blocks until all tasks have been completed
        	// or until a timeout occurs 
        	// timeout is currently set to 1 minute
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return ipsSet;
    }

}
