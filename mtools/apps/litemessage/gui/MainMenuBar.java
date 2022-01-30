package mtools.apps.litemessage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.SettingsModule;

public class MainMenuBar extends JMenuBar {
	MainGUI mGUI;
	
	private JMenu fileMenu;
	private JMenuItem settingsMenuItem;
	private JMenuItem removeContactItem;
	private JMenuItem exitMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	public MainMenuBar(MainGUI mg) {
		
		mGUI = mg;		
		
		//*********
		//File menu
		//*********
		fileMenu = new JMenu("File");
		settingsMenuItem = new JMenuItem("Settings");
		settingsMenuItem.addActionListener(new SettingsAction());
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ExitAction());
		removeContactItem = new JMenuItem("Remove Contact");
		removeContactItem.addActionListener(new RemoveContactAction());
		fileMenu.add(settingsMenuItem);
		fileMenu.add(removeContactItem);
		fileMenu.add(exitMenuItem);
		
		
		//*********
		//Help menu
		//*********
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new AboutAction());
		helpMenu.add(aboutMenuItem);
		
		this.add(fileMenu);
		this.add(helpMenu);
	}
	
	private class SettingsAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsGUI sGUI = new SettingsGUI(mGUI.sMod);
		}
	}
	
	private class RemoveContactAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String contact = mGUI.contactList.getSelectedValue();
			
			//Do nothing if we get a bad selection
			if(contact == null || contact.matches(""))
				return;
			
			mGUI.cMan.removeContact(contact);
			mGUI.updateContactList();
		}
	}
	
	private class ExitAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	private class AboutAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "LiteMessage 0.2.0\nCopyright (c) 2022 Noah McLean", "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}