package mtools.apps.litemessage.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple object that contains a {@link DataInputStream}, a {@link DataOutputStream}, and the
 * Socket the streams were derived from for convenience.  It is constructed from information
 * provided by a socket.  It has a couple convenience methods included.  The primary purpose of
 * this class is to provide easy access to a Socket's IO streams.  While the Socket is also copied
 * into this class, it is really only there for for reference purposes.  The preferred way of interacting with
 * the Socket is either with the {@link ConnectionManager} or directly with the Socket OUTSIDE of
 * this class.
 * @author Noah
 *
 */
public class StreamBundle {
	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;
	
	/**
	 * Constructs the StreamBundle by deriving DataInputStreams and DataOutputStreams from the
	 * Socket that is passed.
	 * @param s
	 * @throws IOException
	 */
	public StreamBundle(Socket s) throws IOException {
		socket = s;
		iStream = new DataInputStream(s.getInputStream());
		oStream = new DataOutputStream(s.getOutputStream());
	}
	
	/**
	 * Convenience method to easily grab a string from the DataInputStream.
	 * @return
	 * @throws IOException
	 */
	public String readUTFData() throws IOException {
		return iStream.readUTF();
	}
	
	/**
	 * Convenience method to easily send a string with the DataOutputStream.
	 * @param data
	 * @throws IOException
	 */
	public void writeUTFData(String data) throws IOException {
		oStream.writeUTF(data);
	}
	
	/**
	 * Returns the Socket that is associated with this StreamBundle.  This should only
	 * be used for reference purposes.  Preferred Socket interaction is through the
	 * {@link ConnectionManager} or directly with the Socket itself, if it was created
	 * externally, without the use of a ConnectionManager.
	 * @return
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * Returns the DataInputStream associated with this StreamBundle.
	 * @return {@link DataInputStream}
	 */
	public DataInputStream getInputStream() {
		return iStream;
	}
	
	/**
	 * Returns the DataOutputStream associated with this StreamBundle.
	 * @return {@link DataOutputStream}
	 */
	public DataOutputStream getOutputStream() {
		return oStream;
	}
	
	/**
	 * Closes the DataInputStream and DataOutputStream associated with this StreamBundle.
	 * IT DOES NOT close the Socket.
	 * @throws IOException
	 */
	public void closeStreams() throws IOException {
		iStream.close();
		oStream.close();
	}
}
