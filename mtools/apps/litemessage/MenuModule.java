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

import mtools.io.*;

/**
 * Simply stores the menus for the programs and
 * allows you to call them up.  Also contains a
 * {@link MenuState} which tells you what menu
 * you are in.  However, the MenuState object
 * is currently not utilized anywhere else in the
 * program.  It implements {@link IOModule}, but
 * that functionality is also not utilized at
 * this time.  Accurate as of 7 Jan 22.
 * 
 * @author Noah
 *
 */
public class MenuModule implements IOModule {
	
	MenuState mState;
	MMenu menu;
	MConsole console;
	int menuChoice;

	public MenuModule(MConsole con, MMenu m) {
		menu = m;
		console = con;
	}
	
	public void startMenuControlModule() {
		
	}
	
	/**
	 * Configures the main menu and then prints it on the console.
	 */
	public void displayMainMenu() {
		menu.clearMenu();
		menu.setBanner("Welcome to LiteMessage.  Please make a selection...");
		menu.addMenuItem("Message Somebody");
		menu.addMenuItem("Receive Messages");
		menu.addMenuItem("Change Settings");
		menu.addMenuItem("Help");
		menu.addMenuItem("Exit");
		menu.display();
		mState = MenuState.IN_MAIN_MENU;
	}
	
	public void displaySettingsMenu() {
		
		
		mState = MenuState.IN_SETTINGS_MENU;
		
		mState = MenuState.IN_MAIN_MENU;
	}
	
	public void displayHelp() {
		
		
		mState = MenuState.IN_HELP_MENU;
		
		mState = MenuState.IN_MAIN_MENU;
	}
	
	/**
	 * Removes the menus from the console window and changes the state to {@link MenuState.NOT_IN_A_MENU}
	 */
	public void tearDownMenus() {
		menu.clearMenu();
		menu.display();
		mState = MenuState.NOT_IN_A_MENU;
	}
	
	public MenuState getMenuState() {
		return mState;
	}

	public int getMenuChoice() {
		return menuChoice;
	}
	
	@Override
	public void sendData(String s) {
		menuChoice = console.getInputInt();
	}
}
