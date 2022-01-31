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

package mtools.apps.litemessage.control.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

import mtools.io.*;

/**
 * This class will be used for changing the settings from within the program.
 * @author Noah
 *
 */
public class SettingsModule {
	private MConsole console;
	private MDisplay display;
	private MMenu menu;
	private Settings settings;
	private String username;
	private String uid;
	private boolean isBlank;
	private boolean isConsoleBased;
	
	/**
	 * The constructor.  Also loads the settings into memory upon construction.
	 * @param con
	 * @param consoleBased
	 */
	public SettingsModule(MConsole con, boolean consoleBased) {
		console = con;
		isConsoleBased = consoleBased;
		
		display = new MDisplay();
		menu = new MMenu("Settings Menu.  Please make a selection...");
		settings = new Settings();
		
		menu.addMenuItem("Change display name");
		menu.addMenuItem("Enable/disable dynamic UID updates");
		menu.addMenuItem("Delete Contact");
		menu.addMenuItem("Go back");
		
		username = null;
		uid = null;
		isBlank = false;
		
		//Initially read the settings
		readSettingsFromFile();
	}
	

	
	/**
	 * Reads the settings from the file and configures out {@link Settings} object to match.
	 */
	public void readSettingsFromFile() {
		
		//Open and then read the settings from the file.
		try {
			FileReader fReader = new FileReader("settings.cfg");
			BufferedReader bReader = new BufferedReader(fReader);
			String selfContactInfo = bReader.readLine();
			
			isBlank = false;
			
			int count = 0;
			
			//NEED TO ORGANIZE THE PARSING AND SETTING CODE BETTER
			//Parse the Self Contact info and grab the user/display name
			while(true) {
				//If file is blank, set a default username.
				if(selfContactInfo == null) {
					username = "errnoname";
					//If the first line is null, then the config is blank.
					isBlank = true;
					break;
				}
				if(selfContactInfo.charAt(count) != ',') {
					if(username == null)
						username = String.valueOf(selfContactInfo.charAt(count));
					else
						username = username + String.valueOf(selfContactInfo.charAt(count));
					
				} else {
					break;
				}
				count++;
				
			}
			
			//Advance the counter past the comma.
			count++;
			
			///Grab the UID
			while(true) {
				//If file is blank, set a default UID.
				//if(selfContactInfo == null) {
				if(isBlank) {
					uid = "1234567";
					break;
				}
				
				//We'll read to the end of the line
				if(count < (selfContactInfo.length())) {
					if(uid == null)
						uid = String.valueOf(selfContactInfo.charAt(count));
					else
						uid = uid + String.valueOf(selfContactInfo.charAt(count));
					
				} else {
					break;
				}
				count++;
				
			}
			
			//if the name is the default value, we'll get a new name from the user.
			if(username.matches("errnoname")) {
				getDisplayName();
				
			} else {
				settings.thisUser.setName(username);
			}
			
			//If the UID is the default value, we will generate a new one
			if(uid.matches("1234567")) {
				settings.thisUser.generateUID();
			} else {
				settings.thisUser.setUID(uid);
			}
			
			boolean dUIDUpdate;
			String uidup = bReader.readLine();
			//default behavior is false, unless explicitly stated to be true
			if(isBlank) {
				dUIDUpdate = false;
			} else {
				if(uidup.matches("true")) {
					dUIDUpdate = true;
				} else {
					dUIDUpdate = false;
				}
			}
			settings.dynamicUIDUpdates = dUIDUpdate;
			
			bReader.close();
			fReader.close();
			
			//If anything was default (or blank), it means it was changed.
			//So we will write those changes to the settings.cfg file.
			if(username.matches("errnoname") || uid.matches("1234567") || isBlank) {
				writeSettingsToFile();
			}
		//If we can't read settings.cfg, we'll try to create a default one.
		} catch (IOException e) {
			System.err.println("Can not access settings file, or file is corrupt!!! (settings.cfg)");
			try {
				FileWriter fWriter = new FileWriter("settings.cfg");
				BufferedWriter bWriter = new BufferedWriter(fWriter);
				
				settings.thisUser.setName("errnoname");
				settings.thisUser.setUID("1234567");
				settings.dynamicUIDUpdates = false;
				
				System.err.println("Default settings file has been created.");
				bWriter.close();
				fWriter.close();
				
				//We'll start over and make sure at least the defaults can be read.
				readSettingsFromFile();
				
			} catch (IOException e1) {
				System.err.println("Can not write default settings file.  Please check user and application permissions.");
			}
		}
	}
	
	/**
	 * Writes whatever settings are configured in our {@link Settings} object to the cfg file.
	 */
	public void writeSettingsToFile() {
		try {
			FileWriter fWriter = new FileWriter("settings.cfg");
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			bWriter.write(settings.thisUser.getName() + "," + settings.thisUser.getUID());
			bWriter.newLine();
			
			if(settings.dynamicUIDUpdates == true) {
				bWriter.write("true");
			} else {
				bWriter.write("false");
			}
			
			bWriter.flush();
			bWriter.close();
			fWriter.close();
		} catch (IOException e) {
			System.err.println("Can not write to settings file!!! (settings.cfg)");
		}
	}
	
	/**
	 * Call this from the console main menu.
	 */
	public void configSettingsFromConsole(ContactManager cm) {
		menu.display();
		
		int selection = 0;
		try {
			selection = console.getInputInt();
		} catch(Exception e) {
			return;
		}
		
		switch(selection) {
		
		//Set Username
		case 0:
			while(true) {
				display.clear();
				display.setBanner("Enter a display name.  Do not use any commas!!!");
				display.display();
				System.out.print("> ");
			
				String name = console.getInputString();
				if(name.contains(",") || name == "") {
					continue;
				}
				settings.thisUser.setName(name);
				break;
			}
					
			break;
		
		//Enable/disable dynamic UID updates
		case 1:
			display.clear();
			display.setBanner("Dynamic UID");
			display.addLine("Enabling dynamic UID will allow the IP address of a contact to be updated "
					+ "even if the computer's generated unique ID does not match.  In other words, "
					+ "enabling this could easily allow somebody to impersonate another individual "
					+ "while chatting with you.  However, this is useful if your group switches between "
					+ "computers frequently.");
			display.addLine(" ");
			display.addLine("0. Enable");
			display.addLine("1. Disable");
			display.addLine("2. Return to main menu");
			display.display();
			
			System.out.print("> ");
			
			int choice = 0;
			
			try {
				choice = console.getInputInt();
			} catch(Exception e) {
				return;
			}
			
			if(choice == 0) {
				settings.dynamicUIDUpdates = true;
			} else if(choice == 1) {
				settings.dynamicUIDUpdates = false;
			}
			break;
		
		//Remove Contact
		case 2:
			display.clear();
			System.out.println("Delete Contact\n");
			for(int i = 0; i<cm.getNumContacts(); i++) {
				System.out.println(i + ". " + cm.getContacts().get(i).getName());
			}
			
			System.out.print("\nPlease select the contact you wish to delete.\n> ");
			
			int contact = 0;
			
			try {
				contact = console.getInputInt();
			} catch(Exception e) {
				return;
			}
			
			if(contact < cm.getNumContacts() && contact >= 0) {
				cm.removeContact(contact);
			}
			
			break;
			
		//Go back to the main menu
		case 3:
			break;
		}
		
		//We write the settings back out to file.
		writeSettingsToFile();
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	
	
	//******************************
	//Internal private methods below
	//******************************
	
	
	
	/**
	 * This will grab the display name from either the GUI
	 * or console, depending on the isConsoleBased flag.
	 */
	private void getDisplayName() {		
		if(isConsoleBased) {
			getDisplayNameFromConsole();
		} else {
			getDisplayNameFromGUI();
		}
		
	}
	
	/**
	 * Prompts the user for their display name through the console.
	 */
	private void getDisplayNameFromConsole() {
		while(true) {
			display.clear();
			display.setBanner("Enter a display name.  Do not use any commas!!!");
			display.display();
		
			String name = console.getInputString();
			
			if(name.contains(",") || name.matches(""))
				continue;
				
			settings.thisUser.setName(name);
			break;
		}
	}
	
	/**
	 * Provides a dialog box asking the user to enter their display name.
	 */
	private void getDisplayNameFromGUI() {
		while(settings.thisUser.getName() == null || settings.thisUser.getName().matches("errnoname")) {
			String name = JOptionPane.showInputDialog(null, "Enter display name.  Do not use commas.");
			
			//Check to make sure the user didn't just click cancel.
			if(name == null)
				continue;

			//Check to make sure they didn't enter any commas or weird stuff.
			if(name.contains(",") || name.matches(""))
				continue;

			settings.thisUser.setName(name);
			break;
		}
	}
}