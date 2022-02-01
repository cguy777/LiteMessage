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

import javax.swing.JOptionPane;

import mtools.apps.litemessage.control.logic.ContactManager;
import mtools.apps.litemessage.core.MessagingState;
import mtools.apps.litemessage.core.networking.ConnectionManager;

public class ReceiveMessageHandler extends Thread {
	
	private MainGUI mainGUI;
	private ConnectionManager connectionMan;
	
	public ReceiveMessageHandler(MainGUI mg, ConnectionManager cm) {
		mainGUI = mg;
		connectionMan = cm;
	}

	@Override
	public void run() {
		
		if(!connectionMan.isLocalPortUsable(ConnectionManager.INIT_STANDARD_PORT)) {
			JOptionPane.showMessageDialog(null, "The default initialization port is not available!  You cannot be contacted until otherwise.", "Network Error", JOptionPane.ERROR_MESSAGE);
		}
		
		while(true) {
			
			//Prevents a bunch of windows from popping up and stealing resources when the port isn't available.
			if(!connectionMan.isLocalPortUsable(ConnectionManager.INIT_STANDARD_PORT)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				
				continue;
			}
			
			MessagingGUI messagingGUI = new MessagingGUI(connectionMan);
			messagingGUI.waitForMessaging(mainGUI.cMan);
			mainGUI.updateContactList();
		}
	}

}
