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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import mtools.apps.litemessage.Contact;
import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.MessagingControlModule;
import mtools.apps.litemessage.SettingsModule;
import mtools.io.MConsole;

public class MainGUI extends JFrame {
	
	private ContactManager cMan;
	private JList<String> contactList;
	private JScrollPane contactScroll;
	private JButton messageButton;
	private JButton contactSomebodyNew;
	
	public MainGUI() {
		
		buildGUI();
		
	}
	
	/**
	 * Used to update the contact list GUI component when need.
	 */
	public void updateContactList() {
		String[] s = new String[cMan.getNumContacts()];
		
		for(int i = 0; i<cMan.getContacts().size(); i++) {
			s[i] = cMan.getContacts().get(i).getName();
		}
		
		contactList.setListData(s);
	}
	
	private void buildGUI() {
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
		
		contactList = new JList<String>();
		updateContactList();
		
		contactList.setSize(100, 500);
		contactScroll = new JScrollPane(contactList);
		contactScroll.setBounds(10, 10, 200, 300);
		this.add(contactScroll);
		
		
		//Connect To Button
		messageButton = new JButton("Connect To...");
		messageButton.setVisible(true);
		messageButton.setBounds(10, 320, 95, 20);
		messageButton.addActionListener(new ConnectButtonAction());
		messageButton.setMargin(new Insets(0, 0, 0, 0));
		this.add(messageButton);
		
		
		//Not Listed Button
		contactSomebodyNew = new JButton("Not Listed");
		contactSomebodyNew.setVisible(true);
		contactSomebodyNew.setBounds(115, 320, 95, 20);
		contactSomebodyNew.addActionListener(new ContactSomebodyNewAction());
		this.add(contactSomebodyNew);
		
		
		
		
		
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
				
				//There might have been a change to the contact list, so we'll update it.
				updateContactList();
			} catch(Exception ex) {
				//Nothing is selected.  So do nothing.
				return;
			}	
		}	
	}
	
	private class ContactSomebodyNewAction implements ActionListener {

		MessagingGUI mGUI;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String address = JOptionPane.showInputDialog("Enter an IP address or hostname");
				
				//Quick input validation.
				if(address.matches("") || address == null) {
					JOptionPane.showMessageDialog(null, "Nothing was entered!", "Error", JOptionPane.ERROR_MESSAGE);;
					return;
				}
				mGUI = new MessagingGUI(address, cMan);
				
				//If it's somebody new, there should be a new contact to list, so we'll update it.
				updateContactList();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}


