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
import mtools.apps.litemessage.console.ConsoleReceiveMessageHandler;
import mtools.apps.litemessage.console.ConsoleTextDisplay;
import mtools.apps.litemessage.console.ConsoleTextInput;
import mtools.apps.litemessage.console.MenuModule;
import mtools.apps.litemessage.control.logic.CommandParseModule;
import mtools.apps.litemessage.control.logic.CommandType;
import mtools.apps.litemessage.control.logic.ContactManager;
import mtools.apps.litemessage.control.logic.MessagingControlModule;
import mtools.apps.litemessage.control.logic.SettingsModule;
import mtools.apps.litemessage.core.MessagingState;
import mtools.apps.litemessage.core.networking.ConnectionManager;
import mtools.io.*;

public class LiteMessage {
	
	static MessagingControlModule cMod;
	static ConsoleReceiveMessageHandler rmh;
	static MDisplay display;
	static ConsoleTextDisplay ctd;
	static ConsoleTextInput console;
	static ConnectionManager connectionMan;
	static ContactManager cMan;
	
	public static void main(String[] args) {
		display = new MDisplay("Messaging App", 5);
		display.setDisplayReverse();
		console = new ConsoleTextInput();
		MMenu menu = new MMenu();
		
		connectionMan = new ConnectionManager();
		SettingsModule sMod = new SettingsModule(console, true);
		MenuModule menuMod = new MenuModule(console, menu, sMod.getSettings().thisUser.getName());
		CommandParseModule cpm = new CommandParseModule();
		
		cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		ctd = new ConsoleTextDisplay();
		
		boolean justExited = true;
		
		//Main control loop
		while(true) {
				
			if(justExited) {
				initializeRxComms();
				justExited = false;
			}
				
			if(cMod.getMessagingState() != MessagingState.CURRENTLY_MESSAGING)
				menuMod.displayMainMenu();
				
			//We'll drop down a line and print a thing to indicate it's ready to type.
			System.out.print("> ");
			String input = console.getInputString();
			
			if(cMod.getMessagingState() == MessagingState.CURRENTLY_MESSAGING) {
				cMod.sendData(input);
					
				if(cpm.evaluateText(input) == CommandType.EXIT) {
					justExited = true;
					initializeRxComms();
				}
			
			} else {
					
				int choice = 0;
					
				try {
					choice = Integer.parseInt(input);
				} catch(NumberFormatException nfe) {
					continue;
				}
					
				switch(choice) {
				//Message Somebody
				case 0:
					cMod.clearConnections();
					cMod = new MessagingControlModule(display, ctd, console, connectionMan, cMan);
					cMod.startInitiateMessageLogic();
					break;
			
				//Receive Messages
				case 1:
					cMod.clearConnections();
					cMod = new MessagingControlModule(display, ctd, console, connectionMan, cMan);
					cMod.startReceiveMessageLogic();
					break;
			
				//Change settings
				case 2:
					sMod.configSettingsFromConsole(cMan);
					break;
				
				//Exit
				case 3:
					System.exit(0);
			
				}
			}
		
		}
	}
	
	private static void initializeRxComms() {
		cMod = new MessagingControlModule(display, ctd, console, connectionMan, cMan);
		rmh = new ConsoleReceiveMessageHandler(cMod);
		rmh.start();
	}

}
