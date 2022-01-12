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

package mtools.apps.litemessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import mtools.io.*;

/**
 * This class will be used for changing the settings from within the program.
 * @author Noah
 *
 */
public class SettingsModule {
	MConsole console;
	MDisplay display;
	MMenu menu;
	Settings settings;
	
	
	public SettingsModule(MConsole con) {
		console = con;
		display = new MDisplay();
		menu = new MMenu("Settings Menu.  Please make a selection...");
		settings = new Settings();
		
		menu.addMenuItem("Set Username");
		
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
			settings.userName = bReader.readLine();
			bReader.close();
			fReader.close();
		} catch (IOException e) {
			System.err.println("Cannot access settings file!!! (settings.cfg)");
		}
	}
	
	/**
	 * Writes whatever settings are configured in our {@link Settings} object to the cfg file.
	 */
	public void writeSettingsToFile() {
		try {
			FileWriter fWriter = new FileWriter("settings.cfg");
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			bWriter.write(settings.userName);
			bWriter.newLine();
		
			bWriter.flush();
			bWriter.close();
			fWriter.close();
		} catch (IOException e) {
			System.err.println("Can not write to settings file!!! (settings.cfg)");
		}
	}
	
	/**
	 * Call this from the main menu.
	 */
	public void run() {
		menu.display();
		
		int selection = console.getInputInt();
		
		switch(selection) {
		
		//Set Username
		case 0:
			display.clear();
			display.setBanner("Enter the username you would like displayed...");
			display.display();
			
			settings.userName = console.getInputString();
		}
		
		
		//We write the settings back out to file.
		writeSettingsToFile();
	}
	
	public Settings getSettings() {
		return settings;
	}
}
