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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mtools.apps.litemessage.control.logic.SettingsModule;

public class SettingsGUI extends JFrame {
	
	SettingsModule sMod;
	
	JLabel nameLabel;
	JTextField displayName;
	JCheckBox dynamicUID;
	JButton saveButton;
	JButton cancelButton;
	
	public SettingsGUI(SettingsModule sm) {
		sMod = sm;
		
		//Main window frame
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(235, 150);
		this.setLayout(null);
		this.setResizable(false);
		this.setTitle("LiteMessage - Settings");
		this.setType(Type.UTILITY);
		
		//Display name label and field
		nameLabel = new JLabel("Display Name");
		nameLabel.setBounds(10, 10, 80, 25);
		nameLabel.setVisible(true);
		displayName = new JTextField(sMod.getSettings().thisUser.getName(), 30);
		displayName.setBounds(90, 10, 120, 25);
		displayName.setVisible(true);
		displayName.setToolTipText("Display name.  Can not include commas.");
		this.add(nameLabel);
		this.add(displayName);
		
		//Dynamic UID check box
		dynamicUID = new JCheckBox("Enable Dynamic UID Updates", sMod.getSettings().dynamicUIDUpdates);
		dynamicUID.setBounds(10, 40, 200, 25);
		dynamicUID.setVisible(true);
		this.add(dynamicUID);
		
		//Save button
		saveButton = new JButton("Save");
		saveButton.setBounds(10, 70, 95, 30);
		saveButton.setVisible(true);
		saveButton.setMargin(new Insets(0, 0, 0, 0));
		saveButton.addActionListener(new SaveAction(this));
		this.add(saveButton);
		
		//Cancel button
		cancelButton = new JButton("Cancel");
		cancelButton.setBounds(115, 70, 95, 30);
		cancelButton.setVisible(true);
		cancelButton.addActionListener(new CancelAction(this));
		this.add(cancelButton);
		
		
		
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
			
			sMod.getSettings().thisUser.setName(displayName.getText());
			sMod.getSettings().dynamicUIDUpdates = dynamicUID.isSelected();
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
