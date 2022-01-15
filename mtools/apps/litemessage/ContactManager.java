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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ContactManager {
	private ArrayList<Contact> contacts;
	private Settings settings;
	
	public ContactManager(Settings s) {
		contacts = new ArrayList<Contact>();
		settings = s;
	}
	
	//Parses the string and then returns a new Contact object
	private Contact createContactFromString(String data) {
		int count = 0;
		String username = null;
		String uid = null;
		String ipAddress = null;
		
		
		if(!data.matches("")) {
			
			//Grab the display name.
			while(true) {
				if(data.charAt(count) != ',') {
					if(username == null)
						username = String.valueOf(data.charAt(count));
					else
						username = username + String.valueOf(data.charAt(count));
				} else {
					break;
				}
				count++;
			
			}
		
			//Advance the counter past the comma.
			count++;
		
			///Grab the UID
			while(true) {
				//We'll read to the next comma
				if(data.charAt(count) != ',') {
					if(uid == null)
						uid = String.valueOf(data.charAt(count));
					else
						uid = uid + String.valueOf(data.charAt(count));
				
				} else {
					break;
				}
				count++;
			
			}
			
			//advance past the comma
			count++;
			
			//Grab the IP address
			while(true) {
				//We'll read to the end of the line
				if(count < (data.length())) {
					if(ipAddress == null)
						ipAddress = String.valueOf(data.charAt(count));
					else
						ipAddress = ipAddress + String.valueOf(data.charAt(count));
				
				} else {
					break;
				}
				count++;
			
			}
		}
		
		//Create and configure the Contact object we need to return.
		Contact c = new Contact();
		c.setName(username);
		c.setUID(uid);
		
		try {
			c.setIPAddress(InetAddress.getByName(ipAddress));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return c;
	}
	
	/**
	 * Reads contacts from file.
	 */
	public void readContacts() {
		try {
			FileReader fReader = new FileReader("contacts.cfg");
			BufferedReader bReader = new BufferedReader(fReader);
			
			String data;
			
			while(true) {
				data = bReader.readLine();
				
				//Check if we are at the EOF
				if(data != null) {
					contacts.add(createContactFromString(data));
				} else {
					break;
				}
			}
			
			bReader.close();
			fReader.close();
				
		} catch (Exception e) {
			System.err.println("Can not access contacts.cfg");
			//e.printStackTrace();
		}
	}
	
	public void writeContacts() {
		try {
			FileWriter fWriter = new FileWriter("contacts.cfg");
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			for(int i = 0; i < contacts.size(); i++) {
				String name = contacts.get(i).getName();
				String uid = contacts.get(i).getUID();
				String ipStr = contacts.get(i).getIPAddress().getHostAddress();
				
				bWriter.write(name + "," + uid + "," + ipStr + "\n");
				bWriter.flush();
			}
			
			bWriter.close();
			fWriter.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the number of contacts.
	 * @return
	 */
	public int getNumContacts() {
		return contacts.size();
	}
	
	public ArrayList<Contact> getContacts() {
		return contacts;
	}
	
	/**
	 * Adds a {@link Contact} to an {@link ArrayList}, and
	 * then writes that list to file.
	 * @param c
	 */
	public void addContact(Contact c) {
		
		for(int i = 0; i < contacts.size(); i++) {
			//If the display name matches one of current contacts, we will update depending on
			//if dynamicUIDUpdates is set to true.  If not, we won't do anything
			if(contacts.get(i).getName().matches(c.getName())) {
				if(settings.dynamicUIDUpdates) {
					contacts.get(i).setUID(c.getUID());
					contacts.get(i).setIPAddress(c.getIPAddress());
					//Write them to file now so we don't have to worry about it later.
					writeContacts();
					return;
				} else {
					if(!contacts.get(i).getUID().matches(c.getUID())) {
						System.err.println("This person may not be " + '"' + c.getName() + '"' + ".  Different identifier detected!!");
						System.err.println("If the contact needs to be updated, or you'd like to ignore this in the future, please remove the contact, or enable dynamic UID updates.");
						return;
					//If name and UID match, do nothing.  We already have it.
					} else {
						return;
					}
				}
				
				//If one of the UIDs matches, we'll update that contact
			} else if(contacts.get(i).getUID().matches(c.getUID())) {
				contacts.get(i).setName(c.getName());
				contacts.get(i).setIPAddress(c.getIPAddress());
				//We'll write the contacts to file now so we don't have to worry about it later
				writeContacts();
				return;
			}
		}
		
		//If we can't find a matching UID, we'll just add it.
		contacts.add(c);
		
		//We'll write the contacts to file now so we don't have to worry about it later
		writeContacts();
	}
	
	/**
	 * Returns a {@link Contact} who's name matches String n.
	 * Returns null if it can't be found.  Ignores case.
	 * @param n
	 * @return
	 */
	public Contact getContactByName(String n) {
		for(int i = 0; i < contacts.size(); i++) {
			if(contacts.get(i).getName().compareToIgnoreCase(n) == 0) {
				return contacts.get(i);
			}
		}
		return null;
	}
}
