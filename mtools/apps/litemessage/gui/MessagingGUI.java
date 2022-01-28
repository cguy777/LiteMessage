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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mtools.apps.litemessage.Contact;
import mtools.apps.litemessage.ContactManager;
import mtools.apps.litemessage.MessagingControlModule;
import mtools.apps.litemessage.TextDisplayObject;
import mtools.io.MDisplay;

/**
 * This is the window that you be messaging people in.
 * @author Noah
 *
 */
public class MessagingGUI extends JFrame implements TextDisplayObject {
	
	private Contact contact;
	private MessagingControlModule mcm;
	
	private JTextArea convoHistory;
	private JTextArea composeArea;
	private JButton sendButton;
	
	public MessagingGUI(Contact c, ContactManager cm) {
		contact = c;
		mcm = new MessagingControlModule(new MDisplay(), this, cm);

		buildGUI();
		mcm.startInitiateMessageLogicFromGUI(contact);
	}
	
	private void buildGUI() {
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(250, 350);
		//this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle(contact.getName());
		
		
		//Displays the conversation
		convoHistory = new JTextArea(3, 10);
		convoHistory.setEditable(false);
		convoHistory.setWrapStyleWord(true);
		convoHistory.setLineWrap(true);
		JScrollPane convoScroll = new JScrollPane(convoHistory);
		this.add(convoScroll, BorderLayout.CENTER);
		
		
		//Everything below the convo display
		JPanel lowerPanel = new JPanel(new BorderLayout(10, 10));
		this.add(lowerPanel, BorderLayout.SOUTH);
		
		
		//Where you type the message
		
		composeArea = new JTextArea(4, 10);
		composeArea.setLineWrap(true);
		composeArea.setWrapStyleWord(true);
		JScrollPane composeScroll = new JScrollPane(composeArea);
		lowerPanel.add(composeScroll, BorderLayout.CENTER);
		
		
		//Button to send the message
		sendButton = new JButton("Send");
		sendButton.setSize(80, 30);
		sendButton.addActionListener(new SendButtonAction());
		lowerPanel.add(sendButton, BorderLayout.EAST);
		
		this.setVisible(true);
	}
	
	@Override
	public void print(String s) {
		convoHistory.append(s + "\n");
	}
	
	@Override
	public void tearDown() {
		this.dispose();
	}
	
	private class SendButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = composeArea.getText();
			mcm.sendData(message);
			convoHistory.append("You: " + message + "\n");
			composeArea.setText("");
		}
	}
}
