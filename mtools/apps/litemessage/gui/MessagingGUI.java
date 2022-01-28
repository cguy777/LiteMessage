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
import mtools.io.MDisplay;

/**
 * This is the window that you be messaging people in.
 * @author Noah
 *
 */
public class MessagingGUI extends JFrame {
	
	private Contact contact;
	private MessagingControlModule mcm;
	
	private JTextArea convoHistory;
	private JTextArea composeArea;
	private JButton sendButton;
	
	public MessagingGUI(Contact c, ContactManager cm) {
		contact = c;
		mcm = new MessagingControlModule(new MDisplay(), cm);

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
	
	private class SendButtonAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			mcm.sendData(composeArea.getText());
			composeArea.setText("");
		}
	}
}
