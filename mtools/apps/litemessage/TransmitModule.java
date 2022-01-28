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
import java.net.Socket;

import mtools.io.*;

/**
 * This class is what reaches out to the other client to establish the
 * initial connection, and also sends data to the other client.
 * @author Noah
 *
 */
public class TransmitModule {
	
	private MDisplay display;
	private MConsole console;
	private Socket txSocket;
	private DataOutputStream txStream;
	private CommandParseModule pMod;
	private String txData;
	private int port;
	private MessageStatusObject mState;
	private Contact thisUser;
	
	/**
	 * The constructor.  Most importantly, it constructs/establishes the {@link Socket}.
	 * @param dis
	 * @param con
	 * @param add
	 * @param p
	 * @param ms
	 */
	public TransmitModule(MDisplay dis, MConsole con, InetAddress add, int p, MessageStatusObject ms, Contact c) throws IOException {
		display = dis;
		console = con;
		mState = ms;
		thisUser = c;
		
		port = p;
		txSocket = new Socket(add, port);
		txStream = new DataOutputStream(txSocket.getOutputStream());
		pMod = new CommandParseModule();
			
		//Send the other client info about us so they can create a contact.
		txStream.writeUTF(thisUser.getName() + "," + thisUser.getUID());

		
	}
	
	/**
	 * Closes the currently established connection
	 */
	public void closeConnection() {
		try {
			txSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//We want to make sure that this gets set or we could have issues
		//with controlling the logic of the program.
		mState.setMessagingState(MessagingStatus.NOT_MESSAGING);
	}
	
	public MessagingStatus getConnectionState() {
		return mState.getMessagingState();
	}
	
	/**
	 * Sends text data to the other client.
	 * Also looks for any special commands
	 * that might've been entered.
	 * @param s
	 */
	public void sendData(String s) {
		CommandType ct = pMod.evaluateText(s);
		
		try {
			txStream.writeUTF(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(ct == CommandType.EXIT) {
			closeConnection();
		}
	}
}
