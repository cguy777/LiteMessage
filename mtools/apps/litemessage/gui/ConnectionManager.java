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

package mtools.apps.litemessage.gui;

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
	
	private final int INIT_STANDARD_PORT = 5676;
	private final int ACCEPT_STANDARD_PORT = 5677;
	
	private final int FIRST_DYNAMIC_PORT = 49152;
	private final int LAST_DYNAMIC_PORT = 65535;
	
	ArrayList<Socket> sockets;
	ArrayList<ServerSocket> serverSockets;
	
	public ConnectionManager() {
		sockets = new ArrayList<Socket>();
		serverSockets = new ArrayList<ServerSocket>();
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
	 * Returns the ArrayList of currently active server sockets.
	 * @return
	 */
	public ArrayList<ServerSocket> getServerSockets() {
		return serverSockets;
	}
	
	/**
	 * 
	 * @param ipAddress
	 * @param portNumber
	 * @return
	 * @throws IOException
	 */
	public Socket createSocket(InetAddress ipAddress, int portNumber) throws IOException {
		Socket newSocket = new Socket(ipAddress, portNumber);
		sockets.add(newSocket);
		return newSocket;
	}
	
	/**
	 * Searches through ephemeral ports until an open one is available.  It then creates
	 * and returns a server socket using that port.  It also adds that server socket to 
	 * the server socket array list.  Will return null if a usable port can't be found.
	 * @return
	 */
	public ServerSocket createUsableServerSocket() {
		ServerSocket newServerSocket = null;
		
		int portOffset = 0;
		
		while(newServerSocket == null) {
			try {
				if(FIRST_DYNAMIC_PORT + portOffset > LAST_DYNAMIC_PORT)
					return null;
				
				System.out.println("Trying port " + (FIRST_DYNAMIC_PORT + portOffset));
				newServerSocket = new ServerSocket(FIRST_DYNAMIC_PORT + portOffset);

			} catch (IOException e) {
				portOffset++;
			}
		}
		
		System.out.println("Found port " + (FIRST_DYNAMIC_PORT + portOffset));
		serverSockets.add(newServerSocket);
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
	 * Closes the server socket that is connected to the referenced IP address, and then removes it from the 
	 * active server socket array list.
	 * @param ipAddress
	 */
	public void closeServerSocket(InetAddress remoteIPAddress) {
		for(int i = 0; i<serverSockets.size(); i++) {
			if(serverSockets.get(i).getLocalSocketAddress().equals(remoteIPAddress)) {
				try {
					serverSockets.get(i).close();
					serverSockets.remove(i);
				} catch(IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Closes the server socket that matches the server socket that is passed, and then removes it from the 
	 * active server socket array list.
	 * @param ss
	 */
	public void closeServerSocket(ServerSocket ss) {
		for(int i = 0; i<serverSockets.size(); i++) {
			if(serverSockets.get(i) == ss) {
				try {
					serverSockets.get(i).close();
					serverSockets.remove(i);
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
	
	public static void main(String[]args) {
		ConnectionManager cm = new ConnectionManager();
	}
	
}
