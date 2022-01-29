package mtools.apps.litemessage.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mtools.apps.litemessage.SettingsModule;

public class MainMenuBar extends JMenuBar {
	
	SettingsModule sMod;
	
	private JMenu fileMenu;
	private JMenuItem exitMenuItem;
	private JMenuItem settingsMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	public MainMenuBar(SettingsModule sm) {
		
		sMod = sm;
		
		//*********
		//File menu
		//*********
		fileMenu = new JMenu("File");
		settingsMenuItem = new JMenuItem("Settings");
		settingsMenuItem.addActionListener(new settingsAction());
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new exitAction());
		fileMenu.add(settingsMenuItem);
		fileMenu.add(exitMenuItem);
		
		
		//*********
		//Help menu
		//*********
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new aboutAction());
		helpMenu.add(aboutMenuItem);
		
		this.add(fileMenu);
		this.add(helpMenu);
	}
	
	private class settingsAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsGUI sGUI = new SettingsGUI(sMod);
		}
	}
	
	private class exitAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	private class aboutAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "LiteMessage 0.2.0\nCopyright (c) 2022 Noah McLean", "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}