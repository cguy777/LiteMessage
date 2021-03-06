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

import mtools.apps.litemessage.core.Contact;
import mtools.apps.litemessage.core.MessagingState;
import mtools.apps.litemessage.core.TextDisplayObject;
import mtools.apps.litemessage.core.TextInputObject;
import mtools.apps.litemessage.core.networking.ConnectionManager;
import mtools.apps.litemessage.core.networking.StreamBundle;
import mtools.io.MDisplay;
import mtools.logging.MLog;

/**
 * This is used to control data flow in a single chat session.
 * @author Noah
 *
 */
public class MessagingControlModule extends Thread {
	
	protected MDisplay display;
	protected MessageStatusObject mState;
	protected Contact thisUser;
	protected Contact otherUser;
	protected ContactManager cMan;
	protected CommandParseModule cpm;
	protected TextDisplayObject displayObject;
	protected TextInputObject inputObject;
	protected ConnectionManager connectionMan;
	protected StreamBundle sBundle;
	
	/**
	 * The constructor.
	 * @param dis
	 */
	public MessagingControlModule(MDisplay dis, TextDisplayObject tdo, TextInputObject tio, ConnectionManager conMan, ContactManager cm) {
		display = dis;
		cMan = cm;
		thisUser = cMan.getSelfContact();
		otherUser = new Contact();
		mState = new MessageStatusObject();
		cpm = new CommandParseModule();
		displayObject = tdo;
		inputObject = tio;
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
		String input = inputObject.readString();
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
			MLog.fileLog.log("Could not establish a connection with " + input);
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
			MLog.fileLog.log("Could not establish a connection with " + address.getHostAddress());
			return;
		}
		
		if(sBundle == null) {
			System.err.println("Made contact with peer, but could not negotiate a connection.");
			MLog.fileLog.log("Made contact with peer, but could not negotiate a connection (" + address.getHostAddress() + ").");
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
			MLog.fileLog.log("Had issue either sending our user info, or receiving their user info (" + address.getHostAddress() + ")." );
			e.printStackTrace();
		}
		
		
		this.start();
		
		display.setBanner("Connected with " + otherUser.getName());
		display.display();
		MLog.fileLog.log("Connected with " + otherUser.getName());
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		
		cMan.addContact(otherUser);
		
		System.out.print("> ");
		
		//Checking for possibly fishy contact info.
		if(otherUser.getUIDProblem()) {
			System.err.println("This person may not be " + '"' + otherUser.getName() + '"' + ".  Different identifier detected!");
			System.err.println("> If the contact needs to be updated, or you'd like to ignore this in the future, please remove the contact, or enable dynamic UID updates.");
			MLog.fileLog.log("UID mismatch with name \"" + otherUser.getName() + '"');
			
			System.out.print("> ");
		}
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
			MLog.fileLog.log("Could not establish a connection with " + ipAddress);
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
			MLog.fileLog.log("Could not establish a connection with " + address.getHostAddress());
			JOptionPane.showMessageDialog(null, "Could not establish connection", "Error", JOptionPane.ERROR_MESSAGE);
			displayObject.tearDown();
			return;
		}
		
		if(sBundle == null) {
			MLog.fileLog.log("Made contact with peer, but could not negotiate a connection (" + address.getHostAddress() + ").");
			JOptionPane.showMessageDialog(null, "Made contact with peer, but could not negotiate a connection.", "Error", JOptionPane.ERROR_MESSAGE);
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
			MLog.fileLog.log("Had issue either sending our user info, or receiving their user info. (" + address.getHostAddress() + ").");
			JOptionPane.showMessageDialog(null, "Had issue either sending our user info, or receiving their user info.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		this.start();
		
		displayObject.println("Connected with " + otherUser.getName() + "\n");
		MLog.fileLog.log("Connected with " + otherUser.getName());
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		
		cMan.addContact(otherUser);
		
		//Checking for possibly fishy contact info.
		if(otherUser.getUIDProblem()) {
			JOptionPane.showMessageDialog(null, "This person may not be " + '"' + otherUser.getName() + '"' + ".  Different identifier detected!", "Warning", JOptionPane.WARNING_MESSAGE);
			JOptionPane.showMessageDialog(null, "If the contact needs to be updated, or you'd like to ignore this in the future, please remove the contact, or enable dynamic UID updates.", "Information", JOptionPane.INFORMATION_MESSAGE);
			MLog.fileLog.log("UID mismatch with name \"" + otherUser.getName() + '"');
		}
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
		
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			//System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Sombody else attempted to initiate contact.  Error while sending our user info, or receiving their user info.");
			MLog.fileLog.log("Sombody else attempted to initiate contact.  Error while sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		display.setBanner("Connected with " + otherUser.getName());
		display.display();
		MLog.fileLog.log("Connected with " + otherUser.getName());
		System.out.print("> ");
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
		
		//Checking for possibly fishy contact info.
		if(otherUser.getUIDProblem()) {
			System.out.println("This person may not be " + '"' + otherUser.getName() + '"' + ".  Different identifier detected!");
			System.out.println("> If the contact needs to be updated, or you'd like to ignore this in the future, please remove the contact, or enable dynamic UID updates.");
			MLog.fileLog.log("UID mismatch with name \"" + otherUser.getName() + '"');
			
			System.out.print("> ");
		}
		
		this.start();
	}
	
	public void startReceiveMessageLogicFromGUI() {
				
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			System.err.println("Somebody else attempted to initiate contact.  Error while reaching back to the peer initiating connection.");
			MLog.fileLog.log("Somebody else attempted to initiate contact.  Error while reaching back to the peer initiating connection.");
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Error encountered while sending our user info, or receiving their user info.");
			MLog.fileLog.log("Error encountered while sending our user info, or receiving their user info.");
			e.printStackTrace();
		}
		
		this.start();
		
		displayObject.println("Connected with " + otherUser.getName());
		MLog.fileLog.log("Connected with " + otherUser.getName());
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
		
		//Checking for possibly fishy contact info.
		if(otherUser.getUIDProblem()) {
			JOptionPane.showMessageDialog(null, "This person may not be " + '"' + otherUser.getName() + '"' + ".  Different identifier detected!", "Warning", JOptionPane.WARNING_MESSAGE);
			JOptionPane.showMessageDialog(null, "If the contact needs to be updated, or you'd like to ignore this in the future, please remove the contact, or enable dynamic UID updates.", "Information", JOptionPane.INFORMATION_MESSAGE);
			MLog.fileLog.log("UID mismatch with name \"" + otherUser.getName() + '"');
		}
	}	
	
	/**
	 * closes all of the sockets/connections.  However, it does
	 * not close any ServerSockets.
	 */
	public void clearConnections() {
		
		try {
			sBundle.closeStreams();	
		} catch (IOException e) {
		} catch (NullPointerException npe) {
			//If the sBundle is null, there was never a connection to begin with
			//So do nothing.
		}
		
		try {
			connectionMan.closeSocket(sBundle.getSocket());
		} catch(Exception e) {
			
		}
		
		mState.setMessagingState(MessagingState.NOT_MESSAGING);
	}
	
	/**
	 * Closes any open ServerSoket in the ConnectionManager.
	 * Mainly just used for the Console version because program
	 * flow is convoluted in that version.
	 */
	public void clearServerSocket() {
		try {
			connectionMan.closeServerSocket();
		} catch(Exception e) {
			
		}
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
				MLog.fileLog.log("Connection with " + otherUser.getName() + " has ended");	
				clearConnections();
				return;
			} catch (IOException e) {
				displayObject.println("You have been disconnected...");
				MLog.fileLog.log("Connection with " + otherUser.getName() + " has ended");
				clearConnections();
				return;
			}
			
			if(cpm.evaluateText(rxData) == CommandType.EXIT) {
				clearConnections();
				
				displayObject.println(otherUser.getName() + " has left...");
				MLog.fileLog.log("Connection with " + otherUser.getName() + " has ended");
				return;
			}
			
			displayObject.println(otherUser.getName() + ": " + rxData);
		}
	}
	
	protected void parseOtherUserData(String data) {
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
