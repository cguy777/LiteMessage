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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import mtools.apps.litemessage.control.logic.SettingsModule;

public class SettingsGUI extends JFrame {
	
	SettingsModule sMod;
	
	JTabbedPane tabbedPane;
	
	JPanel standardSettings;
	JPanel advancedSettings;
	
	JPanel lowerPanel;
	
	JLabel nameLabel;
	JTextField displayName;
	JCheckBox dynamicUID;
	
	JTextField controlPortField;
	JTextField dataPortField;
	JLabel controlPortLabel;
	JLabel dataPortLabel;
	
	JButton saveButton;
	JButton cancelButton;
	
	public SettingsGUI(SettingsModule sm) {
		sMod = sm;
		
		//Main window frame
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(235, 175);
		this.setLayout(new BorderLayout(10, 10));
		this.setResizable(true);
		this.setTitle("LiteMessage - Settings");
		this.setType(Type.UTILITY);
		
		//Tabbed Pane
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 235, 150);
		this.add(tabbedPane, BorderLayout.CENTER);
		
		//Settings panels
		standardSettings = new JPanel();
		standardSettings.setLayout(null);
		tabbedPane.addTab("Standard", standardSettings);
		advancedSettings = new JPanel();
		advancedSettings.setLayout(new GridLayout(2, 2, 10, 10));
		tabbedPane.addTab("Advanced", advancedSettings);
		
		//Standard Settings
		//Display name label and field
		nameLabel = new JLabel("Display Name");
		nameLabel.setBounds(10, 10, 80, 25);
		nameLabel.setToolTipText("Display name.  Can not include commas.");
		nameLabel.setVisible(true);
		displayName = new JTextField(sMod.getSettings().thisUser.getName(), 30);
		displayName.setBounds(90, 10, 120, 25);
		displayName.setVisible(true);
		displayName.setToolTipText("Display name.  Can not include commas.");
		standardSettings.add(nameLabel);
		standardSettings.add(displayName);
		
		//Dynamic UID check box
		dynamicUID = new JCheckBox("Enable Dynamic UID Updates", sMod.getSettings().dynamicUIDUpdates);
		dynamicUID.setBounds(10, 40, 200, 25);
		dynamicUID.setVisible(true);
		standardSettings.add(dynamicUID);
		
		
		//Advanced Settings
		//Port fields and labels
		controlPortLabel = new JLabel("Control Port");
		controlPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		controlPortLabel.setToolTipText("The port that control and negotiation data is received on. Change requires program restart.");
		advancedSettings.add(controlPortLabel);
		controlPortField = new JTextField(String.valueOf(sMod.getSettings().controlPort), 0);
		controlPortField.setMaximumSize(new Dimension(30, 30));
		controlPortField.setToolTipText("The port that control and negotiation data is received on. Change requires program restart.");
		advancedSettings.add(controlPortField);
		
		dataPortLabel = new JLabel("Data Port");
		dataPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dataPortLabel.setToolTipText("The port that chat messages are received on. Change requires program restart.");
		advancedSettings.add(dataPortLabel);
		dataPortField = new JTextField(String.valueOf(sMod.getSettings().dataPort), 0);
		dataPortField.setMaximumSize(new Dimension(30, 30));
		dataPortField.setToolTipText("The port that chat messages are received on. Change requires program restart.");
		advancedSettings.add(dataPortField);
		
		
		//Lower panel
		lowerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		lowerPanel.setBounds(0, 150, 235, 30);
		this.add(lowerPanel, BorderLayout.SOUTH);
		
		//Save button
		saveButton = new JButton("Save");
		//saveButton.setBounds(10, 150, 95, 30);
		saveButton.setVisible(true);
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.addActionListener(new SaveAction(this));
		lowerPanel.add(saveButton);
		
		//Cancel button
		cancelButton = new JButton("Cancel");
		//cancelButton.setBounds(110, 150, 95, 30);
		cancelButton.setVisible(true);
		cancelButton.addActionListener(new CancelAction(this));
		lowerPanel.add(cancelButton);
		
		
		
		this.setVisible(true);
	}
	
	
	
	private class SaveAction implements ActionListener {

		private JFrame frame;
		
		public SaveAction(JFrame jf) {
			frame = jf;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			//If this is the case, we will not do anything except inform the user the error of their ways.
			if(displayName.getText().contains(",") || displayName.getText().matches("")) {
				JOptionPane.showMessageDialog(null, "Display name cannot be blank, and must not contain commas!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			int controlPort = 0;
			int dataPort = 0;
			
			try {
				controlPort = Integer.parseInt(controlPortField.getText());
				dataPort = Integer.parseInt(dataPortField.getText());
				
				//Input validation
				if(controlPort < 1 || controlPort > 65535) {
					throw new NumberFormatException();
				} if(dataPort < 1 || dataPort > 65535) {
					throw new NumberFormatException();
				} if(controlPort == dataPort) {
					throw new NumberFormatException();
				}
				
			} catch(NumberFormatException ex) {
				JOptionPane.showMessageDialog(null, "Port numbers must be between 1 and 65535, and can not be equal to each other.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			sMod.getSettings().thisUser.setName(displayName.getText());
			sMod.getSettings().dynamicUIDUpdates = dynamicUID.isSelected();
			sMod.getSettings().controlPort = controlPort;
			sMod.getSettings().dataPort = dataPort;
			sMod.writeSettingsToFile();
			
			frame.dispose();
		}
	}
	
	private class CancelAction implements ActionListener {

		private JFrame frame;
		
		public CancelAction(JFrame jf) {
			frame = jf;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}
}
