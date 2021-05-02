package p2p;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileHandler {
	String path;
	String folderPath;
	File directoryPath;

	/**
	 * Constructor
	 */
    public FileHandler() {
    	path = System.getProperty("user.dir");	// directory user is in
    	folderPath = path + File.separator + "files";
    	directoryPath = new File(folderPath);
    }

    /**
	 * Receives the files from another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @throws IOException
	 */
	public void receiveFiles(Socket socket) throws IOException {
		// Creates a BufferedInputStream and saves input stream for later use
		// socket.getInputStream() - returns an input stream for the socket
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		// Creates a DataInputStream that uses the BufferedInputStream
		DataInputStream dis = new DataInputStream(bis);

		int filesCount = dis.readInt();	// gets the number of files
		// creates a list that is the same size as the number of files received
		File[] files = new File[filesCount];
		
		for(int i = 0; i < filesCount; i++) {
			long fileLength = dis.readLong();	// gets the file size
		    String fileName = dis.readUTF();	// gets the file name

		    // creates a new file and adds it to files list
		    files[i] = new File(getPath() + File.separator + fileName);

		    // Creates FileOutputStream to write to the file
		    FileOutputStream fos = new FileOutputStream(files[i]);
		    // Creates BufferedOutputStream to write data to FileOutputStream
		    BufferedOutputStream bos = new BufferedOutputStream(fos);

		    for(int j = 0; j < fileLength; j++) {
		    	// Writes the specified byte to the BufferedOutputStream
		    	bos.write(bis.read());
		    }
		    
		    // prints the files that have been received
		    System.out.println("File " + fileName + " downloaded");

		    bos.close();	// closes BufferedOutputStream
		}
	}

	/**
	 * Sends files to another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @throws IOException
	 */
	public void sendFiles(Socket socket, File file) throws IOException {
		/* creates a BufferedOutputStream to write data for the socket
		 * output stream
		 * socket.getOutputStream() - output stream for the socket	*/
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		
		// creates a DataOutputStream to write data for BufferedOutputStream
		DataOutputStream dos = new DataOutputStream(bos);
		
		// writes the number of files to DataOutputStream 
		// writeInt() - writes int as 4 bytes, high byte first
//		dos.writeInt(files.length);
		dos.writeInt(1);	// only sending one file

//		// iterates through all the files in the specified directory
//		for(File file : files)
//		{
		long length = file.length();	// gets the file length
		// writes the size of the file to DataOutputStream
		// writeLong() - writes long as 8 bytes, high byte first
		dos.writeLong(length);

		String name = file.getName();	// gets the file name
		// writes the name of the file to DataOutputStream
		// writeUTF() - writes string using modified UTF-8 encoding
		dos.writeUTF(name);

		// Creates a FileInputStream by opening a connection to the file
		FileInputStream fis = new FileInputStream(file);
		// Creates a BufferedInputStream and saves fis for later use
		BufferedInputStream bis = new BufferedInputStream(fis);

		int theByte = 0;

		// read returns -1 at the end-of-file
		// bis.read() - the number of bytes read
		while((theByte = bis.read()) != -1) {
			// writes the specified byte to this buffered output stream
			bos.write(theByte);
		}

		bis.close();	// closes BufferedInputStream
		System.out.println("Sent " + name);
//		}

		dos.close();	// closes DataOutputStream
	}
	
	/**
	 * Creates a folder if the folder does not already exist
	 */
	private void setDirectory() {
		// checks if the folder exists or not
		if(!directoryPath.exists()) {
			// if the folder doesn't exist, it creates one
			directoryPath.mkdir();
		}
	}

	/**
	 * Returns the folder path
	 * @return absolute path of the folder
	 */
	public String getPath() {
		// returns the absolute path of the folder
		return folderPath;
	}

	/**
	 * Gets all the files in the specified directory
	 * @return list of files
	 */
	public File[] getListofFiles() {
		// makes sure that the folder exists
		setDirectory();
		// returns a list of Files in the specified directory
		return directoryPath.listFiles();
	}
	
	/**
	 * Compares two files to see if content is the same
	 * @param file1: first file to be compared
	 * @param file2: second file to be compared
	 * @return true if files are the same, false otherwise
	 * @throws IOException
	 */
	public boolean compareFiles(File file1, File file2) throws IOException {
		long start = System.nanoTime();
        FileChannel ch1 = new RandomAccessFile(file1, "r").getChannel();
        FileChannel ch2 = new RandomAccessFile(file2, "r").getChannel();
        if (ch1.size() != ch2.size()) {
            return false;
        }
        long size = ch1.size();
        ByteBuffer m1 = ch1.map(FileChannel.MapMode.READ_ONLY, 0L, size);
        ByteBuffer m2 = ch2.map(FileChannel.MapMode.READ_ONLY, 0L, size);
        for (int pos = 0; pos < size; pos++) {
            if (m1.get(pos) != m2.get(pos)) {
                System.out.println("Files differ at position " + pos);
                return false;
            }
        }
        return true;
	}

	public void receiveFile(Socket socket, File file) throws IOException {
		byte [] mybytearray  = new byte [(int)file.length()];
		InputStream is = socket.getInputStream();
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int bytesRead = is.read(mybytearray,0,mybytearray.length);
		int current = bytesRead;

		do {
			bytesRead =
					is.read(mybytearray, current, (mybytearray.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);

		bos.write(mybytearray, 0 , current);
		bos.flush();
	}
	
	public void sendFile(Socket socket, File file) throws IOException {
        byte [] mybytearray  = new byte [(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        OutputStream os = socket.getOutputStream();
        System.out.println("Sending " + file.getName() + "(" + mybytearray.length + " bytes)");
        os.write(mybytearray,0,mybytearray.length);
        os.flush();
	}
}