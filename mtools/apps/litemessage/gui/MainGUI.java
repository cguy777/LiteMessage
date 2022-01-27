package mtools.apps.litemessage.gui;

import javax.swing.JFrame;
import javax.swing.JList;

import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.SettingsModule;
import mtools.io.MConsole;

public class MainGUI extends JFrame {
	
	private ContactManager cMan;
	
	public MainGUI() {

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 500);
		this.setResizable(false);
		
		MConsole console = new MConsole();
		SettingsModule sMod = new SettingsModule(console, false);
		
		this.setTitle("LiteMessage - " + sMod.getSettings().thisUser.getName());
		
		cMan = new ContactManager(sMod.getSettings());
		cMan.loadContacts();
		
		
		
		String[] s = new String[cMan.getNumContacts()];
		
		for(int i = 0; i<cMan.getContacts().size(); i++) {
			s[i] = cMan.getContacts().get(i).getName();
		}
		
		JList<String> contactList = new JList<String>(s);
		this.add(contactList);
		
	}
}
