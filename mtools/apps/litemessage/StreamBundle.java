package mtools.apps.litemessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple object that contains a {@link DataInputStream}, a {@link DataOutputStream}, and the
 * Socket the streams were derived from for convenience.  It is constructed from information
 * provided by a socket.  It has a couple convenience methods included.
 * @author Noah
 *
 */
public class StreamBundle {
	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;
	
	public StreamBundle(Socket s) throws IOException {
		socket = s;
		iStream = new DataInputStream(s.getInputStream());
		oStream = new DataOutputStream(s.getOutputStream());
	}
	
	public String readUTFData() throws IOException {
		return iStream.readUTF();
	}
	
	public void writeUTFData(String data) throws IOException {
		oStream.writeUTF(data);
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public DataInputStream getInputStream() {
		return iStream;
	}
	
	public DataOutputStream getOutputStrea() {
		return oStream;
	}
	
	public void closeStreams() throws IOException {
		iStream.close();
		oStream.close();
	}
}
