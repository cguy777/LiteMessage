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

package mtools.apps.litemessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import mtools.io.*;


/**
 * The class responsible for receiving message data from the other client
 * it extends {@link Thread} so that it can run in the background and show
 * message data whenever it appears.
 * @author Noah
 *
 */

public class ReceiveModule extends Thread {
	
	private MDisplay display;
	private MConsole console;
	private ServerSocket rxServerSocket;
	private Socket rxSocket;
	private DataInputStream rxStream;
	private CommandParseModule pMod;
	private String rxData;
	private int port;
	private MessageStatusObject mState;
	private Contact otherUser;
	
	/**
	 * The constructor.  Most importantly, it constructs the {@link ServerSocket}.
	 * You must call waitForConnection() after it is constructed.
	 * @param dis
	 * @param con
	 * @param p
	 * @param ms
	 */
	public ReceiveModule(MDisplay dis, MConsole con, int p, MessageStatusObject ms) {
		display = dis;
		console = con;
		port = p;
		mState = ms;
		otherUser = new Contact();
		
		try {
			rxServerSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Not currently in use.  Could be useful in the future if
	 * the flow of the program changes.  It just constructs the {@link ServerSocket}, 
	 * which would've have already been done by this objects constructor.
	 */
	public void initializeServerSocket() {
		try {
			rxServerSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getBindedAddress() {
		return rxSocket.getInetAddress();
	}
	
	/**
	 * Waits for another client to reach out and establish
	 * a connection.  It also grabs info about the other
	 * client and saves it to form the contact table.
	 */
	public void waitForConnection() {
		try {
			rxSocket = rxServerSocket.accept();
			rxStream = new DataInputStream(rxSocket.getInputStream());
			String otherUserData = rxStream.readUTF();
			parseOtherUserData(otherUserData);
			otherUser.setIPAddress(rxSocket.getInetAddress());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * closes both the {@link Socket} and {@link ServerSocket}
	 * It also sets the the messaging state to {@link MessagingStatus.NOT_MESSAGING}
	 */
	public void closeConnection() {
		try {
			rxSocket.close();
			rxServerSocket.close();
			//rxSocket = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mState.setMessagingState(MessagingStatus.NOT_MESSAGING);
	}
	
	/**
	 * Returns the current messaging state.  Used for general program control.
	 * @return
	 */
	public MessagingStatus getConnectionState() {
		return mState.getMessagingState();
	}
	
	public Contact getContact() {
		return otherUser;
	}
	
	/**
	 * Since we need to be able to display messages that come in at any time,
	 * the {@link ReceieveModule} is run as a thread in the background.
	 * Simply enough, this waits for messages to come through and displays them
	 * when they are received.  It also uses the {@link CommandParseModule} to
	 * determine if any special commands were sent.
	 */
	@Override
	public void run() {

			pMod = new CommandParseModule(rxSocket);
			try {
				//if(!rxServerSocket.isBound())
				//rxSocket = rxServerSocket.accept();
			
				//rxStream = new DataInputStream(rxSocket.getInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			while(rxSocket.isConnected()) {
			
				try {
					rxData = rxStream.readUTF();
				} catch (IOException e) {
					closeConnection();
					break;
				}
				
				//Print the received message
				//We'll use System.out instead of
				//our Display class so we can keep
				//a log of the messages on the screen.
				System.out.println(otherUser.getName() + ": " + rxData);
				
				//If the exit command is received, we close connections and display a message.
				if(pMod.evaluateText(rxData) == CommandType.EXIT) {
					closeConnection();
					display.clear();
					display.setBanner("Session ended.  Press enter to return to the Main Menu...");
					display.display();
					
					
					break;
				}
				
				//We'll print a thing to indicate it's ready to type.
				System.out.print("> ");
				
			}

	}
	
	private void parseOtherUserData(String data) {
		int count = 0;
		String username = null;
		String uid = null;
		
		//Grab the display name.
		while(true) {
			if(data.charAt(count) != ',') {
				if(username == null)
					username = String.valueOf(data.charAt(count));
				else
					username = username + String.valueOf(data.charAt(count));
			} else {
				break;
			}
			count++;
			
		}
		
		//Advance the counter past the comma.
		count++;
		
		///Grab the UID
		while(true) {
			//We'll read to the end of the line
			if(count < (data.length())) {
				if(uid == null)
					uid = String.valueOf(data.charAt(count));
				else
					uid = uid + String.valueOf(data.charAt(count));
				
			} else {
				break;
			}
			count++;
			
		}
		
		otherUser.setName(username);
		otherUser.setUID(uid);
	}
}
