/* Copyright 2022 Noah McLean
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the
 *    names of its contributors may be used to endorse or
 *    promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mtools.apps.litemessage.core.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Used for keeping track of what connections are established and what
 * ports are being used
 * @author Noah
 *
 */
public class ConnectionManager {
	
	private int INIT_STANDARD_PORT = 5676;
	
	private int FIRST_DYNAMIC_PORT = 49152;
	private int LAST_DYNAMIC_PORT = 65535;
	
	ArrayList<Socket> sockets;
	
	public ConnectionManager() {
		sockets = new ArrayList<Socket>();
	}
	
	/**
	 * Changes the port that the dynamic sockets are negotiated over.  Default is 5676.
	 * @param newPortNumber
	 */
	public void setStandardPort(int newPortNumber) {
		INIT_STANDARD_PORT = newPortNumber;
	}
	
	/**
	 * Changes the port range that the dynamic sockets can be created over.  Default values
	 * are the IANA ephemeral port range (49152-65535).
	 * @param lowPort
	 * @param highPort
	 */
	public void setDynamicPortRange(int lowPort, int highPort) {
		FIRST_DYNAMIC_PORT = lowPort;
		LAST_DYNAMIC_PORT = highPort;
	}
	
	/**
	 * Attempts to initiate a connection with another device by reaching out and connecting
	 * on a predetermined port.  The default port is 5676.  It will then negotiate for
	 * a new port that is, by default, within the IANA ephemeral port range (49152-65535).
	 * A {@link StreamBundle} is then returned based off of this new socket connection.
	 * @param ipAddress
	 * @return
	 * @throws IOException
	 */
	public StreamBundle initSessionNegotiation(InetAddress ipAddress) throws IOException {
		Socket initSocket = new Socket(ipAddress, INIT_STANDARD_PORT);
		DataInputStream initInputStream = new DataInputStream(initSocket.getInputStream());
		
		String port = initInputStream.readUTF();
		int portNumber = 0;
		
		try {
			portNumber = Integer.parseInt(port);
			initInputStream.close();
			initSocket.close();
		} catch(Exception e) {
			System.err.println("Error while negotiating connection.");
			e.printStackTrace();
			return null;
		}
		
		Socket dataSocket = new Socket(ipAddress, portNumber);
		sockets.add(dataSocket);
		return new StreamBundle(dataSocket);
	}
	
	/**
	 * Waits for a connection on a predetermined port.  The default port is 5676.  It will
	 * then negotiate for a new port that is, by default, within the IANA ephemeral port
	 * range (49152-65535). A {@link StreamBundle} is then returned based off of this new
	 * socket connection.
	 * @return
	 * @throws IOException
	 */
	public StreamBundle waitForSessionNegotiation() throws IOException {
		ServerSocket tempServerSocket = new ServerSocket(INIT_STANDARD_PORT);
		Socket initSocket = tempServerSocket.accept();
		tempServerSocket.close();
		
		DataOutputStream initStream = new DataOutputStream(initSocket.getOutputStream());
		
		tempServerSocket = createUsableServerSocket();
		
		try {
			initStream.writeUTF(String.valueOf(tempServerSocket.getLocalPort()));
			initStream.close();
			initSocket.close();
		} catch(Exception e) {
			System.err.println("Error while negotiating connection.");
			e.printStackTrace();
			tempServerSocket.close();
			return null;
		}
		
		Socket dataSocket = tempServerSocket.accept();
		tempServerSocket.close();
		sockets.add(dataSocket);
		return new StreamBundle(dataSocket);
	}
	
	/**
	 * Searches through the array list looking for the matching socket based off of an IP address and a port number.
	 * Accepts both the remote IP address and the remote port number of the connection for extra assurance.
	 * Returns null if the socket can't be found.
	 * @param connectedPeer
	 * @param portNumber
	 * @return
	 */
	public Socket getAConnectedSocket(InetAddress connectedPeer, int portNumber) {
		for(int i = 0; i<sockets.size(); i++) {
			if(sockets.get(i).getInetAddress().equals(connectedPeer) && sockets.get(i).getPort() == portNumber) {
				return sockets.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Returns the ArrayList of currently active sockets that reached out to a peer and originated locally.
	 * @return
	 */
	public ArrayList<Socket> getSockets() {
		return sockets;
	}
	
	/**
	 * Returns the number of established connections that have been negotiated off of the default
	 * initializing port.  This is determined by the size of the sockets ArrayList.  The sockets
	 * ArrayList contains every active socket. 
	 * @return
	 */
	public int getNumOfConnections() {
		return sockets.size();
	}
	
	/**
	 * Searches through ephemeral/dynamic ports until an open one is available.  It then creates
	 * and returns a server socket using that port.  Will return null if a usable port can't be found.
	 * @return
	 */
	private ServerSocket createUsableServerSocket() {
		ServerSocket newServerSocket = null;
		
		int portOffset = 0;
		
		while(newServerSocket == null) {
			try {
				if(FIRST_DYNAMIC_PORT + portOffset > LAST_DYNAMIC_PORT)
					return null;
				
				newServerSocket = new ServerSocket(FIRST_DYNAMIC_PORT + portOffset);

			} catch (IOException e) {
				portOffset++;
			}
		}
		
		//Re-implement commented line below to a log
		//System.out.println("Found port " + (FIRST_DYNAMIC_PORT + portOffset));
		return newServerSocket;
	}
	
	/**
	 * Closes the socket that is referenced from the IP address, and then removes it from the 
	 * active socket array list.
	 * @param remoteIPAddress
	 */
	public void closeSocket(InetAddress remoteIPAddress) {
		for(int i = 0; i<sockets.size(); i++) {
			if(sockets.get(i).getInetAddress().equals(remoteIPAddress)) {
				try {
					sockets.get(i).close();
					sockets.remove(i);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Closes the socket that matches the socket that is passed, and removes if from the active
	 * socket array list.
	 * @param s
	 */
	public void closeSocket(Socket s) {
		for(int i = 0; i<sockets.size(); i++) {
			if(sockets.get(i) == s) {
				try {
					sockets.get(i).close();
					sockets.remove(i);
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * for testing
	 */
	public void listServerSocketAvailability() {
		ServerSocket newServerSocket = null;
		
		int portOffset = 0;
		while(true) {
			try {
				if(FIRST_DYNAMIC_PORT + portOffset > LAST_DYNAMIC_PORT) {
					break;
				}
				
				newServerSocket = new ServerSocket(FIRST_DYNAMIC_PORT + portOffset);
				newServerSocket.close();
				
				//System.out.println("Found port " + (STARTING_DYNAMIC_PORT + portOffset));
			} catch (IOException e) {
				System.out.println("Can't use " + (FIRST_DYNAMIC_PORT + portOffset));
			}
			
			portOffset++;
		}
	}
	
	public static void main(String[]args) throws UnknownHostException, IOException {
		ConnectionManager cm = new ConnectionManager();
		cm.listServerSocketAvailability();
	}
}
