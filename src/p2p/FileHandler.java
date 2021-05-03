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
	 * Receive a file from another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @throws IOException
	 */
	public void receiveFile(Socket socket) throws IOException {
		// Creates a BufferedInputStream and saves input stream for later use
		// socket.getInputStream() - returns an input stream for the socket
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		// Creates a DataInputStream that uses the BufferedInputStream
		DataInputStream dis = new DataInputStream(bis);

		long fileLength = dis.readLong();	// gets the file size
		String fileName = dis.readUTF();	// gets the file name

		// creates a new file and adds it to files list
		File file = new File(getPath() + File.separator + fileName);

		// Creates FileOutputStream to write to the file
		FileOutputStream fos = new FileOutputStream(file);
		// Creates BufferedOutputStream to write data to FileOutputStream
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		for(int j = 0; j < fileLength; j++) {
			// Writes the specified byte to the BufferedOutputStream
			bos.write(bis.read());
		}

		// prints the files that have been received
		System.out.println("File " + fileName + " downloaded");

		// Flushes BufferedOutputStream 
		// Forces any buffered-output bytes to be written out to output stream
		bos.flush();
	}
	
	/**
	 * Sends a file from another node/peer
	 * @param socket: socket associated with the current node/peer
	 * @param file
	 * @throws IOException
	 */
	public void sendFile(Socket socket, File file) throws IOException {
		/* creates a BufferedOutputStream to write data for the socket
		 * output stream
		 * socket.getOutputStream() - output stream for the socket	*/
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		
		// creates a DataOutputStream to write data for BufferedOutputStream
		DataOutputStream dos = new DataOutputStream(bos);

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

		int theByte = 0; // tracks current byte

		// read returns -1 at the end-of-file
		// bis.read() - the number of bytes read
		while((theByte = bis.read()) != -1) {
			// writes the specified byte to this buffered output stream
			bos.write(theByte);
		}

		// Flushes DataOutputStream 
		// Forces any buffered output bytes to be written out to the stream
		dos.flush();
		// Flushes BufferedOutputStream
		// Forces any buffered output bytes to be written out to the stream
		bos.flush();
		System.out.println("Sent " + name);
	}
	
}