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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Used for keeping track of what connections are established and what
 * ports are being used
 * @author Noah
 *
 */
public class ConnectionManager {
	
	/**
	 * This is the port on which sessions are initiated by default, and then is handed
	 * over to one of the dynamic ports.
	 */
	public static final int CONTROL_PORT = 5676;
	public static final int FIRST_DYNAMIC_PORT = 49152;
	public static final int LAST_DYNAMIC_PORT = 65535;
	public static final int DEFAULT_NEGOTIATION_TIMEOUT = 2000;
	
	private int controlPort;
	private int firstDynamicPort;
	private int lastDynamicPort;
	private int negotiationTimeout;
	private boolean outgoingPortEnforcement;
	
	private ServerSocket serverSocket;
	
	private ArrayList<Socket> sockets;
	
	public ConnectionManager() {
		sockets = new ArrayList<Socket>();
		
		controlPort = CONTROL_PORT;
		firstDynamicPort = FIRST_DYNAMIC_PORT;
		lastDynamicPort = LAST_DYNAMIC_PORT;
		negotiationTimeout = DEFAULT_NEGOTIATION_TIMEOUT;
		outgoingPortEnforcement = false;
	}
	
	/**
	 * Changes the port that the dynamic sockets are negotiated over.  Default is 5676.
	 * @param newPortNumber
	 */
	public void setControlPort(int newPortNumber) throws PortRangeException {
		if(newPortNumber < 1 || newPortNumber > 65535)
			throw new PortRangeException("Specified ports must be between 1 and 65535.");
		
		if(newPortNumber >= firstDynamicPort && newPortNumber <= lastDynamicPort)
			throw new PortRangeException("Control port cannot be within the range of data ports.");
			
		controlPort = newPortNumber;
	}
	
	/**
	 * Changes the port range that the dynamic sockets can be created over.  Default values
	 * are the IANA ephemeral port range (49152-65535).  Throws a {@link PortRangeException}
	 * if low port is greater than high port, or if low Port is less than 1, or high
	 * port is greater than 65535.
	 * @param lowPort
	 * @param highPort
	 * @throws PortRangeException
	 */
	public void setDynamicPortRange(int lowPort, int highPort) throws PortRangeException {
		if(lowPort > highPort)
			throw new PortRangeException("Specified low port cannot be greater than specified high port.");
		
		if(lowPort < 1 || highPort > 65535)
			throw new PortRangeException("Specified ports must be between 1 and 65535.");
		
		if(controlPort >= lowPort && controlPort <= highPort)
			throw new PortRangeException("Control port cannot be within the range of data ports.");
		
		firstDynamicPort = lowPort;
		lastDynamicPort = highPort;
	}
	
	/**
	 * Sets the amount of time, in milliseconds, that the ServerSocket used during port negotiation
	 * will take to timeout.  Default is 2000 milliseconds.  A value of zero means that it will never
	 * timeout.  This is not recommended as the Thread could hang forever waiting for negotiation to
	 * finish.
	 * @param time
	 */
	public void setNegotiationTimeout(int milliseconds) {
		negotiationTimeout = milliseconds;
	}
	
	/**
	 * Attempts to initiate a connection with another device by reaching out and connecting
	 * on a predetermined port.  The default port is 5676 (This port is configurable).
	 * It will then negotiate for a new port that is, by default, within the IANA ephemeral
	 * port range (49152-65535, this range is configurable).  A {@link StreamBundle} is
	 * then returned based off of this new socket connection.
	 * @param ipAddress
	 * @return
	 * @throws IOException
	 */
	public StreamBundle initSessionNegotiation(InetAddress ipAddress) throws IOException {
		Socket initSocket = new Socket(ipAddress, controlPort);
		DataInputStream initInputStream = new DataInputStream(initSocket.getInputStream());
		
		String port = initInputStream.readUTF();
		int portNumber = 0;
		
		try {
			portNumber = Integer.parseInt(port);
			initInputStream.close();
			initSocket.close();
			
			//If enabled, and negotiated port is outside of set range
			//Then throw an exception.
			if(outgoingPortEnforcement) {
				if(portNumber < firstDynamicPort || portNumber > lastDynamicPort) {
					throw new PortRangeException("Negotiated port was outside of acceptable configured ports.");
				}
			}
		} catch(Exception e) {
			System.err.println("ConnectionManager: Error while negotiating connection.");
			e.printStackTrace();
			return null;
		}
		Socket dataSocket = new Socket(ipAddress, portNumber);
		sockets.add(dataSocket);
		return new StreamBundle(dataSocket);
	}
	
	/**
	 * Waits for a connection on a predetermined port.  The default port is 5676.  It will
	 * then negotiate for a random new port that is, by default, within the IANA ephemeral port
	 * range (49152-65535). This range is configurable.  A {@link StreamBundle} is then
	 * returned based off of this new socket connection.
	 * @return
	 * @throws IOException
	 */
	public StreamBundle waitForSessionNegotiation() throws IOException {
		serverSocket = new ServerSocket(controlPort);
		Socket initSocket = serverSocket.accept();
		serverSocket.close();
		
		DataOutputStream initStream = new DataOutputStream(initSocket.getOutputStream());
		
		serverSocket = createUsableServerSocket();
		
		try {
			initStream.writeUTF(String.valueOf(serverSocket.getLocalPort()));
			initStream.close();
			initSocket.close();
		} catch(Exception e) {
			System.err.println("ConnectionManager: Error while negotiating connection.");
			e.printStackTrace();
			serverSocket.close();
			return null;
		}
		
		serverSocket.setSoTimeout(negotiationTimeout);
		Socket dataSocket = null;
		
		try {
			dataSocket = serverSocket.accept();
		} catch(SocketTimeoutException ste) {
			System.err.println("ConnectionManager: Error while negotiating connection.");
			ste.printStackTrace();
			serverSocket.close();
			return null;
		}
		
		serverSocket.close();
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
	 * Returns true if we are able to establish a ServerSocket on the specified port.
	 * Returns false if one can't be made.
	 * @param port
	 * @return
	 */
	public boolean isLocalPortUsable(int port) {
		try {
			ServerSocket tempServerSocket = new ServerSocket(port);
			tempServerSocket.close();
			return true;
		} catch (IOException e) {
			return false;
		}
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
	 * Closes the ServerSocket if it is waiting for a connection.
	 * It will throw a SocketException if the ServerSocket is waiting for a connection.
	 * It can also throw a NullPointerException if the ServerSocket wasn't initialized.
	 * It can also throw an IOException if there are any other issues closing the ServerSocket.
	 * @throws IOException
	 */
	public void closeServerSocket() throws IOException {
		serverSocket.close();
	}
	
	/**
	 * for testing
	 */
	public void listServerSocketAvailability() {
		ServerSocket newServerSocket = null;
		
		int portOffset = 0;
		while(true) {
			try {
				if(firstDynamicPort + portOffset > lastDynamicPort) {
					break;
				}
				
				newServerSocket = new ServerSocket(firstDynamicPort + portOffset);
				newServerSocket.close();
			} catch (IOException e) {
				System.out.println("Can't use " + (firstDynamicPort + portOffset));
			}
			
			portOffset++;
		}
	}
	
	/**
	 * Determines if an outgoing connection is limited to ports
	 * within the set dynamic port range.  By default, the dynamic
	 * port range only limits incoming connections; it does not
	 * limit negotiated outgoing connections.  Setting this to
	 * true will enforce the dynamic port range on outgoing
	 * connections as well.  Could cause exceptions to be thrown
	 * if it is set and a port requested is outside of the enforced
	 * range.
	 * @param onlyListed
	 */
	public void setOutgoingPortEnforcement(boolean portEnforcement) {
		outgoingPortEnforcement = portEnforcement;
	}
	
	/**
	 * Randomly searches through ephemeral/dynamic ports until an open one is available.  It then creates
	 * and returns a server socket using that port.  Will return null if no usable ports are available.
	 * @return
	 */
	private ServerSocket createUsableServerSocket() {
		
		ServerSocket newServerSocket = null;
		int startingPort = selectRandomStartingPort();
		int hopefulPort = startingPort;
		boolean returnedToFirst = false;
		
		while(newServerSocket == null) {
			try {
				
				newServerSocket = new ServerSocket(hopefulPort);

			} catch (IOException e) {
				
				if(returnedToFirst) {
					if(hopefulPort == startingPort) {
						return null;
					} else {
						hopefulPort++;
					}
				} else {
					if(hopefulPort >= lastDynamicPort) {
						hopefulPort = firstDynamicPort;
						returnedToFirst = true;
					} else {
						hopefulPort++;
					}
				}
			}
		}
		
		return newServerSocket;
	}
	
	private int selectRandomStartingPort() {
		int portRangeSize = lastDynamicPort - firstDynamicPort;
		
		double rand = Math.random();
		rand = rand * portRangeSize;
		
		int port = (int) (firstDynamicPort + Math.round(rand));
		
		return port;
	}	
	
	/*
	public static void main(String[]args) throws UnknownHostException, IOException, PortRangeException {
	}
	*/
}
