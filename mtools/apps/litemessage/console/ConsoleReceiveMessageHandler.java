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

package mtools.apps.litemessage.console;

import javax.swing.JOptionPane;

import mtools.apps.litemessage.control.logic.MessagingControlModule;
import mtools.apps.litemessage.core.networking.ConnectionManager;
import mtools.logging.MLog;

/**
 * Just handles incoming connections from the console version.
 * @author Noah
 *
 */
public class ConsoleReceiveMessageHandler extends Thread {
	
	MessagingControlModule cMan;
	ConnectionManager connectionMan;
	
	public ConsoleReceiveMessageHandler(MessagingControlModule mcm, ConnectionManager conMan) {
		cMan = mcm;
		connectionMan = conMan;
		
		if(!connectionMan.isLocalPortUsable(connectionMan.getControlPort())) {
			System.err.println("***The configured control port (" + connectionMan.getControlPort() + ") is not available.  You cannot be contacted until otherwise!***");
			MLog.fileLog.log("Port " + connectionMan.getControlPort() + " is unavailable for use as the control port.");
		}
	}
	
	//Just sits and waits for an incoming connection
	@Override
	public void run() {
		try {
			cMan.startReceiveMessageLogic();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
