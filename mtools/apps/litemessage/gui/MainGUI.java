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


