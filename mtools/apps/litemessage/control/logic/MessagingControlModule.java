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

package mtools.apps.litemessage.control.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import javax.swing.JOptionPane;

import mtools.apps.litemessage.Contact;
import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.MessagingState;
import mtools.apps.litemessage.TextDisplayObject;
import mtools.apps.litemessage.networking.ConnectionManager;
import mtools.apps.litemessage.networking.StreamBundle;
import mtools.io.MConsole;
import mtools.io.MDisplay;

/**
 * This is used to control data flow in a single chat session.
 * @author Noah
 *
 */
public class MessagingControlModule extends Thread {
	
	private MDisplay display;
	private MessageStatusObject mState;
	private Contact thisUser;
	private Contact otherUser;
	private ContactManager cMan;
	CommandParseModule cpm;
	MConsole console;
	TextDisplayObject displayObject;
	ConnectionManager connectionMan;
	StreamBundle sBundle;
	
	/**
	 * The constructor.
	 * @param dis
	 */
	public MessagingControlModule(MDisplay dis, TextDisplayObject tdo, MConsole con, ConnectionManager conMan, ContactManager cm) {
		display = dis;
		cMan = cm;
		thisUser = cMan.getSelfContact();
		otherUser = new Contact();
		mState = new MessageStatusObject();
		cpm = new CommandParseModule();
		displayObject = tdo;
		console = con;
		connectionMan = conMan;
	}
	
	public void startInitiateMessageLogic() {
		display.clear();
		display.clearBanner();
		display.display();
		
		//Display all of the known contacts and assign them a number.
		for(int i = 0; i < cMan.getNumContacts(); i++) {
			System.out.println(i + ". " + cMan.getContacts().get(i).getName());
		}
		
		System.out.println("\nPlease select one of the contacts above (if available), or enter a hostname or IP address...");
		//We'll drop down a line and print a thing to indicate it's ready to type.
		System.out.print("\n> ");
		String input = console.getInputString();
		InetAddress address = null;
		int contactSelection;
		
		//Check for an actual number, and also if the selection is a valid contact selection.
		try {
			contactSelection = Integer.parseInt(input);
			if(contactSelection > (cMan.getNumContacts() - 1) && contactSelection < 0) {
				return;
			}
		} catch (NumberFormatException e) {
			contactSelection = -1;
		}
		
		try {
			if(contactSelection == -1) {
				//Was not a valid contact selection
				//We'll assume it's an IP or hostname
				address = InetAddress.getByName(input);
			} else {
				//Was a valid contact selection
				address = cMan.getContacts().get(contactSelection).getIPAddress();
			}
			
		} catch (Exception e) {
			System.err.println("Could not establish a connection.");
			return;
		}
		
		display.clear();
		display.setBanner("Making Connection...");
		display.display();
		
		//We initiate the TransmitModule first, and it's constructor will
		//reach out and let the other client know that we are attempting to connect
		try {
			sBundle = connectionMan.initSessionNegotiation(address);
		} catch(Exception e) {
			System.err.println("Could not establish a connection.");
			return;
		}
		
		
		
		try {
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		
		this.start();
		
		display.setBanner("Connected with " + otherUser.getName());
		display.display();
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		
		cMan.addContact(otherUser);
	}
	
	
	/**
	 * To be called only from the GUI variant.
	 * Initiates a messaging session with another client.  The other client
	 * has to be waiting for connections first.  This then constructs the {@link TransmitModule}
	 * and {@link ReceiveModule}
	 * 
	 * @param contact the contact we want to make a connection with
	 */
	public void startInitiateMessageLogicFromGUI(Contact contact) {
		InetAddress address = null;
		
		try {
			address = contact.getIPAddress();
		} catch (Exception e) {
			System.err.println("Could not establish a connection.");
			JOptionPane.showMessageDialog(null, "Could not establish connection", "Error", JOptionPane.ERROR_MESSAGE);
			displayObject.tearDown();
			return;
		}

		displayObject.println("Making Connection...");
		
		//We initiate the TransmitModule first, and it's constructor will
		//reach out and let the other client know that we are attempting to connect
		try {
			sBundle = connectionMan.initSessionNegotiation(address);
		} catch(Exception e) {
			System.err.println("Could not establish a connection.");
			JOptionPane.showMessageDialog(null, "Could not establish connection", "Error", JOptionPane.ERROR_MESSAGE);
			displayObject.tearDown();
			return;
		}
		
		try {
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		this.start();
		
		displayObject.println("Connected with " + otherUser.getName() + "\n");
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		
		cMan.addContact(otherUser);
	}
	
	/**
	 * To be called only from the GUI variant.
	 * Initiates a messaging session with another client.  The other client
	 * has to be waiting for connections first.  This then constructs the {@link TransmitModule}
	 * and {@link ReceiveModule}
	 * 
	 * @param ipAddress the IP Address we want to make a connection with.
	 */
	public void startInitiateMessageLogicFromGUI(String ipAddress) {
		InetAddress address = null;
		
		try {
			address = InetAddress.getByName(ipAddress);
		} catch (Exception e) {
			System.err.println("Could not establish a connection.");
			JOptionPane.showMessageDialog(null, "Could not establish connection", "Error", JOptionPane.ERROR_MESSAGE);
			displayObject.tearDown();
			return;
		}

		displayObject.println("Making Connection...");
		
		//We initiate the TransmitModule first, and it's constructor will
		//reach out and let the other client know that we are attempting to connect
		try {
			sBundle = connectionMan.initSessionNegotiation(address);
		} catch(Exception e) {
			System.err.println("Could not establish a connection.");
			JOptionPane.showMessageDialog(null, "Could not establish connection", "Error", JOptionPane.ERROR_MESSAGE);
			displayObject.tearDown();
			return;
		}
		
		try {
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		this.start();
		
		displayObject.println("Connected with " + otherUser.getName() + "\n");
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		
		cMan.addContact(otherUser);
	}
	
	/**
	 * For use with the console.  Waits for a messaging session to be initiated
	 * from another client.  The other client has to reach out AFTER
	 * This method has been called.  This constructs the {@link ReceiveModule},
	 * and then grabs the IP address of the other client from the {@link ServerSocket}
	 * when it's established.  It then constructs the {@link TransmitModule}, which
	 * completes the comms circuit.
	 */
	public void startReceiveMessageLogic() {
		
		displayObject.println("Awaiting Connection...");
		
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		this.start();
		
		display.setBanner("Connected with " + otherUser.getName());
		display.display();
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
	}
	
	public void startReceiveMessageLogicFromGUI() {
				
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		this.start();
		
		displayObject.println("Connected with " + otherUser.getName());
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
	}
	
	/**
	 * Used for testing.
	 */
	public void startTestServerLogic() {
		
		System.out.println("Awaiting Connection...");
		
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		System.out.println("Connected with " + otherUser.getName());
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
		
		String message;
		CommandParseModule cpm = new CommandParseModule();
		
		
		while(true) {
			
			try {
				message = sBundle.readUTFData();
			} catch(IOException e) {
				clearConnections();
				return;
			}
			
			System.out.println(otherUser.getName() + " said: " + message);
			
			if(cpm.evaluateText(message) == CommandType.EXIT) {
				clearConnections();
				return;
			}
			
			try {
				sBundle.writeUTFData(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * closes all of the sockets/connections.
	 */
	public void clearConnections() {
		
		try {
			sBundle.closeStreams();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connectionMan.closeSocket(sBundle.getSocket());
		
		mState.setMessagingState(MessagingState.NOT_MESSAGING);
	}
	
	public MessagingState getMessagingState() {
		return mState.getMessagingState();
	}
	
	public MessageStatusObject getMessageStateObject() {
		return mState;
	}
	
	/**
	 * Returns the contact of who we are currently connected with.
	 * @return
	 */
	public Contact getConnectedContact() {
		return otherUser;
	}

	/**
	 * Transmits a message message, acquired from the console,
	 * through the {@link TransmitModule}.  The TransmitModule
	 * also parses it for any special commands.
	 * @param s
	 */
	public void sendData(String s) {
		//Don't do anything if we aren't currently connected with anybody
		if(mState.getMessagingState() != MessagingState.CURRENTLY_MESSAGING)
			return;
			
		try {
			sBundle.writeUTFData(s);
		} catch(Exception e) {
			System.err.println("LiteMessage: could not send data...");
		}
		
		if(cpm.evaluateText(s) == CommandType.EXIT) {
			clearConnections();
			displayObject.tearDown();
		}
	}
	
	@Override
	public void run() {
		String rxData = null;
		while(true) {
			try {
				rxData = sBundle.readUTFData();
			} catch (SocketException se) {
				displayObject.println("Connection was reset.  You have been disconnected...");
				clearConnections();
				return;
			} catch (IOException e) {
				displayObject.println("You have been disconnected...");
				clearConnections();
				return;
			}
			
			if(cpm.evaluateText(rxData) == CommandType.EXIT) {
				clearConnections();
				
				displayObject.println(otherUser.getName() + " has left...");
				return;
			}
			
			displayObject.println(otherUser.getName() + ": " + rxData);
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
