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
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import mtools.apps.litemessage.Contact;
import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.control.logic.SettingsModule;
import mtools.apps.litemessage.networking.ConnectionManager;
import mtools.io.MConsole;

public class MainGUI extends JFrame {
	
	public ContactManager cMan;
	public SettingsModule sMod;
	
	public ConnectionManager connectionMan;
	
	public JList<String> contactList;
	public JScrollPane contactScroll;	
	public JPanel mainPanel;
	public JPanel lowerPanel;
	public JButton messageButton;
	public JButton contactSomebodyNew;
	
	public MainGUI() {
		sMod = new SettingsModule(new MConsole(), false);
		
		cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		connectionMan = new ConnectionManager();
		
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
		}
		
		buildGUI();
		
		ReceiveMessageHandler rmh = new ReceiveMessageHandler(this, connectionMan);
		rmh.start();
	}
	
	/**
	 * Used to update the contact list GUI component when needed.
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
		this.setJMenuBar(new MainMenuBar(this));
		this.setSize(230, 300);
		this.setTitle("LiteMessage - " + sMod.getSettings().thisUser.getName());
		mainPanel = new JPanel(new BorderLayout(5, 5));
		this.add(mainPanel);
		
		
		//Contacts List
		contactList = new JList<String>();
		updateContactList();
		
		contactList.setSize(100, 100);
		contactList.setFont(new Font(contactList.getFont().getName(), Font.BOLD, 15));
		contactList.setBackground(new Color(0xf0f0f0));
		contactScroll = new JScrollPane(contactList);
		contactScroll.setSize(200, 300);
		mainPanel.add(contactScroll, BorderLayout.CENTER);
		
		//Lower panel
		lowerPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.add(lowerPanel, BorderLayout.SOUTH);
				
		//Connect To Button
		messageButton = new JButton("Connect To...");
		messageButton.setVisible(true);
		messageButton.setSize(95, 30);
		messageButton.addActionListener(new ConnectButtonAction());
		messageButton.setMargin(new Insets(0, 0, 0, 0));
		lowerPanel.add(messageButton, BorderLayout.WEST);
		
		
		//Not Listed Button
		contactSomebodyNew = new JButton("Not Listed");
		contactSomebodyNew.setVisible(true);
		contactSomebodyNew.setSize(95, 30);
		contactSomebodyNew.addActionListener(new ContactSomebodyNewAction());
		lowerPanel.add(contactSomebodyNew, BorderLayout.EAST);
	
		
		
		this.setVisible(true);
	}
	
	private class ConnectButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Contact selectedContact;
			MessagingGUI mGUI;
			
			try {
				selectedContact = cMan.getContactByName(contactList.getSelectedValue());
				mGUI = new MessagingGUI(connectionMan);
				mGUI.initiateMessaging(selectedContact, cMan);
				
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
				String address = JOptionPane.showInputDialog(null, "Enter an IP address or hostname", "Connect to...", JOptionPane.QUESTION_MESSAGE);
				
				//Quick input validation.
				if(address == null || address.matches("")) {
					return;
				}
				
				mGUI = new MessagingGUI(connectionMan);
				mGUI.initiateMessaging(address, cMan);
				
				//If it's somebody new, there should be a new contact to list, so we'll update it.
				updateContactList();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}


