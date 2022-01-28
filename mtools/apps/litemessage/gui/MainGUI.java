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

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import mtools.apps.litemessage.Contact;
import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.SettingsModule;
import mtools.io.MConsole;

public class MainGUI extends JFrame {
	
	private ContactManager cMan;
	private JList<String> contactList;
	private JButton messageButton;
	
	public MainGUI() {

		//Main GUI Frame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 500);
		this.setResizable(false);
		this.setLayout(null);
		
		MConsole console = new MConsole();
		SettingsModule sMod = new SettingsModule(console, false);
		
		this.setTitle("LiteMessage - " + sMod.getSettings().thisUser.getName());
		
		
		//Contacts List
		cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		String[] s = new String[cMan.getNumContacts()];
		
		for(int i = 0; i<cMan.getContacts().size(); i++) {
			s[i] = cMan.getContacts().get(i).getName();
		}
		
		contactList = new JList<String>(s);
		
		contactList.setSize(100, 500);
		contactList.setBounds(10, 10, 120, 300);
		this.add(contactList);
		
		
		//Connect To Button
		messageButton = new JButton("Connect To...");
		messageButton.setVisible(true);
		messageButton.setBounds(10, 320, 120, 20);
		messageButton.addActionListener(new ConnectButtonAction());
		this.add(messageButton);
		
		
		
		
		
		this.setVisible(true);
		
	}
	
	private class ConnectButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Contact selectedContact;
			MessagingGUI mGUI;
			
			try {
				
				selectedContact = cMan.getContactByName(contactList.getSelectedValue());
				mGUI = new MessagingGUI(selectedContact, cMan);
			
			} catch(Exception ex) {
				//Nothing is selected.  So do nothing.
				return;
			}
			
			
		}
		
	}
}


