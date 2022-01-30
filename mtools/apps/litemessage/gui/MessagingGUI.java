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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
	public MessagingControlModule mcm;
	
	private JPanel mainPanel;
	private JTextArea convoHistory;
	JScrollPane convoScroll;
	private JTextArea composeArea;
	private JButton sendButton;	
	
	/**
	 * The constructor.  Constructs the Messaging GUI. 
	 * However, the window will not be visible until we call
	 * one of the {@link initiateMessaging()} methods.  Or 
	 * you can call {@link waitForessagig()} and it will be
	 * made visible when somebody reaches to us.
	 */
	public MessagingGUI() {
		buildGUI();
	}
	
	/**
	 * Initiates a messaging session with a known contact.  Also makes
	 * the GUI window visible.
	 * @param c
	 * @param cm
	 */
	public void initiateMessaging(Contact c, ContactManager cm) {
		this.setVisible(true);
		this.setTitle("Connecting...");
		contact = c;
		mcm = new MessagingControlModule(new MDisplay(), this, cm);
		mcm.startInitiateMessageLogicFromGUI(contact);
		this.setTitle(mcm.getConnectedContact().getName());
	}
	
	/**
	 * Initiates a messaging session with an unknown through the use of an
	 * IP address or hostname.  Also makes the GUI window visible.
	 * @param ipAddress
	 * @param cm
	 */
	public void initiateMessaging(String ipAddress, ContactManager cm) {
		this.setVisible(true);
		this.setTitle("Connecting...");
		mcm = new MessagingControlModule(new MDisplay(), this, cm);
		mcm.startInitiateMessageLogicFromGUI(ipAddress);
		this.setTitle(mcm.getConnectedContact().getName());
		contact = mcm.getConnectedContact();
	}
	
	public void waitForMessaging(ContactManager cm) {
		mcm = new MessagingControlModule(new MDisplay(), this, cm);
		mcm.startReceiveMessageLogic();
		this.setVisible(true);
		this.setTitle(mcm.getConnectedContact().getName());
	}
	
	public void clearConnections() {
		mcm.clearConnections();
	}
	
	private void buildGUI() {
		this.setLayout(new BorderLayout(0, 0));
		this.setSize(250, 350);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(new CloseWindowAction());
		mainPanel = new JPanel(new BorderLayout(10, 10));
		this.add(mainPanel);
		
		
		//Displays the conversation
		convoHistory = new JTextArea(10, 10);
		convoHistory.setEditable(false);
		convoHistory.setWrapStyleWord(true);
		convoHistory.setLineWrap(true);
		convoScroll = new JScrollPane(convoHistory);
		mainPanel.add(convoScroll, BorderLayout.CENTER);
		
		
		//Everything below the convo display
		JPanel lowerPanel = new JPanel(new BorderLayout(10, 10));
		this.add(lowerPanel, BorderLayout.SOUTH);
		
		
		//Where you type the message
		
		composeArea = new JTextArea(3, 10);
		composeArea.setLineWrap(true);
		composeArea.setWrapStyleWord(true);
		composeArea.addKeyListener(new EnterKeyAction());
		JScrollPane composeScroll = new JScrollPane(composeArea);
		lowerPanel.add(composeScroll, BorderLayout.CENTER);
		
		
		//Button to send the message
		sendButton = new JButton("Send");
		sendButton.setSize(80, 30);
		sendButton.addActionListener(new SendButtonAction());
		lowerPanel.add(sendButton, BorderLayout.EAST);
	}
	
	@Override
	public void print(String s) {
		convoHistory.append(s + "\n");
		convoScroll.getVerticalScrollBar().setValue(convoScroll.getVerticalScrollBar().getMaximum());
		
	}
	
	@Override
	public void tearDown() {
		this.dispose();
	}
	
	private class SendButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = composeArea.getText();
			//Don't want to process a blank message.
			if(!message.matches("")) {
				mcm.sendData(message);
				print("You: " + message);
				composeArea.setText("");
			}
		}
	}
	
	private class EnterKeyAction implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == 10) {
				String message = composeArea.getText();
				//Don't want to process a blank message.
				if(!message.matches("")) {
					mcm.sendData(message);
					print("You: " + message);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == 10) {
				composeArea.setText("");
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			
		}
		
	}
	
	private class CloseWindowAction implements WindowListener {

		@Override
		public void windowClosed(WindowEvent e) {
			mcm.sendData("cmd-exit");
			mcm.clearConnections();
		}
		
		@Override
		public void windowOpened(WindowEvent e) {
			
		}

		@Override
		public void windowClosing(WindowEvent e) {

		}

		@Override
		public void windowIconified(WindowEvent e) {
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			
		}

		@Override
		public void windowActivated(WindowEvent e) {
			
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			
		}
		
	}

}
