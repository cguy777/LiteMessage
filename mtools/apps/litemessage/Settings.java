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

/**
 * A class just to store settings.  {@link Contact} thisUser is a contact of this client.
 * It contains the display name, unique ID, and IP address (when known).
 * 
 * dyanmicUIDUpdates is a boolean that determines if a contact's unique ID should
 * be updated when a change is detected.  When true, if a new contact with the same name as an
 * existing contact is detected, the contact is updated with the new UID.  When false, if a
 * new contact with the same name is detected, it does not update the contact info.  This
 * prevents somebody from spoofing somebody else's name, and then having their IP entered
 * as the correct IP for the contact.  Can be used as a small layer of assurance and security.
 * It is set to false by default for a default secure posture.
 * @author Noah
 *
 */
public class Settings {
	Contact thisUser;
	boolean dynamicUIDUpdates;
	
	/**
	 * Sets the settings to the default values.  Might be useful if we have trouble
	 * reading from our settings file
	 */
	public Settings() {
		thisUser = new Contact();
		dynamicUIDUpdates = false;
	}
}
