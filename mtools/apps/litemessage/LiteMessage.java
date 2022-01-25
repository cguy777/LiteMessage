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

import mtools.io.*;

public class LiteMessage {

	public static void main(String[] args) {
		MDisplay display = new MDisplay("Messaging App", 5);
		display.setDisplayReverse();
		MConsole console = new MConsole();
		MMenu menu = new MMenu();
		MenuModule menuMod = new MenuModule(console, menu);
		MessagingControlModule cMod = null;
		SettingsModule sMod = new SettingsModule(console);
		
		ContactManager cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		//Main control loop
		while(true) {
			menuMod.displayMainMenu();
			//We'll drop down a line and print a thing to indicate it's ready to type.
			System.out.print("\n> ");
			int choice = console.getInputInt();
			
			switch(choice) {
			//Message Somebody
			case 0:
				cMod = new MessagingControlModule(display, sMod.getSettings().thisUser, cMan);
				cMod.startInitiateMessageLogic();
				break;
			
			//Receive Messages
			case 1:
				cMod = new MessagingControlModule(display, sMod.getSettings().thisUser, cMan);
				cMod.startReceiveMessageLogic();
				break;
			
			//Change settings
			case 2:
				sMod.configSettingsFromConsole();
				break;
				
			//Exit
			case 3:
				System.exit(0);
				
			}
			
			//Save ourselves some exception trouble by checking if things were initialized or not
			if(cMod != null) {
				while(cMod.getMessagingState() != MessagingStatus.NOT_MESSAGING) {
					//We'll print a thing to indicate it's ready to type.
					System.out.print("> ");
					String data = console.getInputString();
					if(cMod.getMessagingState() != MessagingStatus.NOT_MESSAGING) {
						cMod.sendData(data);
					}
				}
				try {
					cMod.clearConnections();
				} catch(Exception e) {
					//Not doing anything
					//Just helps catch an exception caused by a inputting a bad IP address
					//or contact name
				}
			}
		
		}
	}

}
