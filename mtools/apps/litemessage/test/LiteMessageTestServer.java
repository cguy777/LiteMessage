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

package mtools.apps.litemessage.test;

import mtools.apps.litemessage.console.ConsoleTextDisplay;
import mtools.apps.litemessage.console.ConsoleTextInput;
import mtools.apps.litemessage.control.logic.ContactManager;
import mtools.apps.litemessage.control.logic.MessagingControlModule;
import mtools.apps.litemessage.control.logic.SettingsModule;
import mtools.apps.litemessage.core.networking.ConnectionManager;
import mtools.io.MConsole;
import mtools.io.MDisplay;

public class LiteMessageTestServer {
	public static void main(String[] args) throws InterruptedException {
		MDisplay display = new MDisplay("Messaging App", 5);
		display.setDisplayReverse();
		ConsoleTextInput console = new ConsoleTextInput();
		TestServerMessagingControlModule cMod = null;
		SettingsModule sMod = new SettingsModule(console, true);
		
		ContactManager cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		ConsoleTextDisplay ctd = new ConsoleTextDisplay();
		
		ConnectionManager connectionMan = new ConnectionManager();
		connectionMan.setOutgoingPortEnforcement(true);
		
		try {
			connectionMan.setControlPort(sMod.getSettings().controlPort);
			if(!sMod.getSettings().randomDataPorts) {
				connectionMan.setDynamicPortRange(sMod.getSettings().dataPort, sMod.getSettings().dataPort);
			}
		} catch(IllegalArgumentException rpe) {
			rpe.printStackTrace();
		}
		
		System.out.println("---LiteMessage Test Server---");
		System.out.println("\nRunning...\n");
		
		//Main control loop
		while(true) {
			
			if(!connectionMan.isLocalPortUsable(ConnectionManager.CONTROL_PORT)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				continue;
			}
			
			cMod = new TestServerMessagingControlModule(display, ctd, console, connectionMan, cMan);
			cMod.startTestServerLogic();
		}
	}
}
