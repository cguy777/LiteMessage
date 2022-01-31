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

package mtools.apps.litemessage.core;

import java.net.InetAddress;

/**
 * Simple class for storing information related to another user/contact
 * @author Noah
 *
 */
public class Contact {
	
	private String name;
	private InetAddress ipAddress;
	private String uniqueID;
	
	/**
	 * Sets everything to their default values
	 */
	public Contact() {
		name = "errnoname";
		ipAddress = null;
		uniqueID = "1234567";
	}
	
	/**
	 * Call this to generate a unique ID for this contact.
	 * This should really only be called once for the "Self" contact.
	 */
	public void generateUID() {
		//Generate a somewhat random number up to 9,999,999
		long uidL = (long) ((float) (System.currentTimeMillis() * Math.random()) % 10000000);
		String uidS = Long.toString(uidL);
		
		uniqueID = uidS;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void setIPAddress(InetAddress ip) {
		ipAddress = ip;
	}
	
	public void setUID(String uid) {
		uniqueID = uid;
	}
	
	public String getName() {
		return name;
	}
	
	public InetAddress getIPAddress() {
		return ipAddress;
	}
	
	public String getUID() {
		return uniqueID;
	}
}
