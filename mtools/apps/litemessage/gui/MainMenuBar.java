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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mtools.apps.litemessage.AppInfo;
import mtools.apps.litemessage.control.logic.ContactManager;
import mtools.apps.litemessage.control.logic.SettingsModule;

/**
 * The menu bar at the top of the main window
 * @author Noah
 *
 */
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
			JOptionPane.showMessageDialog(null, AppInfo.getAboutInfoString(), "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}