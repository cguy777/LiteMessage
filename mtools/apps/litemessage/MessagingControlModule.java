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
	int INIT_STANDARD_PORT = 5676;
	int ACCEPT_STANDARD_PORT = 5677;
	
	MConsole console;
	MDisplay display;
	MMenu menu;
	TransmitModule txMod;
	ReceiveModule rxMod;
	InetAddress distantAddress;
	MessageStatusObject mState;
	
	/**
	 * The constructor.
	 * @param dis
	 */
	public MessagingControlModule(MDisplay dis) {
		display = dis;
		console = new MConsole();
		menu = new MMenu();
		mState = new MessageStatusObject();
	}
	
	/**
	 * Initiates a messaging session with another client.  The other client
	 * has to be waiting for connections first.  This grabs the IP address
	 * of the other client from the console, and then constructs the {@link TransmitModule}
	 * and {@link ReceiveModule}
	 */
	public void startInitiateMessageLogic() {
		display.clear();
		display.setBanner("Input IP address");
		display.display();
		
		//We'll drop down a line and print a thing to indicate it's ready to type.
		System.out.print("\n> ");
		String addressFromUser = console.getInputString();
		InetAddress address = null;
		
		
		try {
			address = InetAddress.getByName(addressFromUser);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		display.clear();
		display.setBanner("Making Connection...");
		display.display();
		
		//We initiate the TransmitModule first, and it's constructor will
		//reach out and let the other client know that we are attempting to connect
		txMod = new TransmitModule(display, console, address, INIT_STANDARD_PORT, mState);
		rxMod = new ReceiveModule(display, console, ACCEPT_STANDARD_PORT, mState);

		//The other client will then reach back to us
		//We will wait for the full circuit to be established.
		rxMod.waitForConnection();
		rxMod.start();
		
		display.setBanner("Connected");
		display.display();
		
		mState.setMessagingState(MessagingStatus.INITIATED_MESSAGING);
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
		
		txMod = new TransmitModule(display, console, address, ACCEPT_STANDARD_PORT, mState);
		
		display.setBanner("Connected");
		display.display();
		
		rxMod.start();
		
		mState.setMessagingState(MessagingStatus.ACCECPTED_MESSAGING);
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
