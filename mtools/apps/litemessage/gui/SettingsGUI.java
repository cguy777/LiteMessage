package mtools.apps.litemessage.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mtools.apps.litemessage.SettingsModule;

public class SettingsGUI extends JFrame {
	
	SettingsModule sMod;
	
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
		
		//Display name field
		displayName = new JTextField(sMod.getSettings().thisUser.getName(), 30);
		displayName.setBounds(10, 10, 200, 25);
		displayName.setVisible(true);
		displayName.setToolTipText("Display name.  Can not include commas.");
		this.add(displayName);
		
		//Dynamic UID check box
		dynamicUID = new JCheckBox("Enable Dynamic UID Updates", sMod.getSettings().dynamicUIDUpdates);
		dynamicUID.setBounds(10, 40, 200, 25);
		dynamicUID.setVisible(true);
		this.add(dynamicUID);
		
		//Save button
		saveButton = new JButton("Save and Close");
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
				JOptionPane.showMessageDialog(null, "Display cannot be blank, and must not contain commas!", "Error", JOptionPane.ERROR_MESSAGE);
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
