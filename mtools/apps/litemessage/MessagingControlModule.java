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

import java.net.InetAddress;
import java.net.UnknownHostException;

import mtools.io.MConsole;
import mtools.io.MDisplay;
import mtools.io.MMenu;

/**
 * this is the brain of the messaging app.  It initiates, and manages
 * the connections to other clients.  It is also responsible for passing
 * messages typed in the console to the {@link TransmitModule}.
 * @author Noah
 *
 */
public class MessagingControlModule {
	private int INIT_STANDARD_PORT = 5676;
	private int ACCEPT_STANDARD_PORT = 5677;
	
	private MConsole console;
	private MDisplay display;
	private MMenu menu;
	private TransmitModule txMod;
	private ReceiveModule rxMod;
	private InetAddress distantAddress;
	private MessageStatusObject mState;
	private Contact thisUser;
	private ContactManager cMan;
	
	/**
	 * The console constructor.
	 * @param dis
	 */
	public MessagingControlModule(MDisplay dis, ContactManager cm) {
		display = dis;
		cMan = cm;
		thisUser = cMan.getSelfContact();
		console = new MConsole();
		menu = new MMenu();
		mState = new MessageStatusObject();
	}
	
	/**
	 * Initiates a messaging session with another client.  The other client
	 * has to be waiting for connections first.  This grabs the IP address or contact
	 * of the other client from the console, and then constructs the {@link TransmitModule}
	 * and {@link ReceiveModule}
	 */
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
			Contact c;
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
			txMod = new TransmitModule(display, console, address, INIT_STANDARD_PORT, mState, thisUser);
			rxMod = new ReceiveModule(display, console, ACCEPT_STANDARD_PORT, mState);
		} catch(Exception e) {
			System.err.println("Could not establish a connection.");
			return;
		}
		//The other client will then reach back to us
		//We will wait for the full circuit to be established.
		rxMod.waitForConnection();
		
		rxMod.start();
		display.setBanner("Connected with " + rxMod.getContact().getName());
		display.display();
		
		mState.setMessagingState(MessagingStatus.INITIATED_MESSAGING);
		
		cMan.addContact(rxMod.getContact());
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
			return;
		}
		
		display.clear();
		display.setBanner("Making Connection...");
		display.display();
		
		//We initiate the TransmitModule first, and it's constructor will
		//reach out and let the other client know that we are attempting to connect
		try {
			txMod = new TransmitModule(display, console, address, INIT_STANDARD_PORT, mState, cMan.getSelfContact());
			rxMod = new ReceiveModule(display, console, ACCEPT_STANDARD_PORT, mState);
		} catch(Exception e) {
			System.err.println("Could not establish a connection.");
			return;
		}
		//The other client will then reach back to us
		//We will wait for the full circuit to be established.
		rxMod.waitForConnection();
		
		rxMod.start();
		display.setBanner("Connected with " + rxMod.getContact().getName());
		display.display();
		
		mState.setMessagingState(MessagingStatus.INITIATED_MESSAGING);
		
		cMan.addContact(rxMod.getContact());
	}
	
	/**
	 * Waits for a messaging session to be initiated
	 * from another client.  The other client has to reach out AFTER
	 * This method has been called.  This constructs the {@link ReceiveModule},
	 * and then grabs the IP address of the other client from the {@link ServerSocket}
	 * when it's established.  It then constructs the {@link TransmitModule}, which
	 * completes the comms circuit.
	 */
	public void startReceiveMessageLogic() {
		display.clear();
		display.setBanner("Awaiting Connection...");
		display.display();
		
		InetAddress address = null;
		
		//We construct the ReceiveModule and then wait for a connection before
		//Constructing the TransmitModule.
		rxMod = new ReceiveModule(display, console, INIT_STANDARD_PORT, mState);
		rxMod.waitForConnection();
		address = rxMod.getBindedAddress();
		
		try {
			txMod = new TransmitModule(display, console, address, ACCEPT_STANDARD_PORT, mState, thisUser);
		} catch(Exception e) {
			System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		display.setBanner("Connected with " + rxMod.getContact().getName());
		display.display();
		
		rxMod.start();
		
		mState.setMessagingState(MessagingStatus.ACCECPTED_MESSAGING);
		cMan.addContact(rxMod.getContact());
	}
	
	/**
	 * closes all of the sockets/connections.
	 */
	public void clearConnections() {
		txMod.closeConnection();
		rxMod.closeConnection();
		mState.setMessagingState(MessagingStatus.NOT_MESSAGING);
	}
	
	public MessagingStatus getMessagingState() {
		return mState.getMessagingState();
	}
	
	public MessageStatusObject getMessageStateObject() {
		return mState;
	}

	/**
	 * Transmits a message message, acquired from the console,
	 * through the {@link TransmitModule}.  The TransmitModule
	 * also parses it for any special commands.
	 * @param s
	 */
	public void sendData(String s) {
		txMod.sendData(s);
	}
}
