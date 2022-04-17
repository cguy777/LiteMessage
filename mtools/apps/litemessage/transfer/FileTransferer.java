package mtools.apps.litemessage.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import mtools.apps.litemessage.core.networking.StreamBundle;

public class FileTransferer {
	
	StreamBundle sBundle;
	
	public FileTransferer(StreamBundle bundle) {
		sBundle = bundle;
	}
	
	/**
	 * Sends a file to the other client
	 * @param file
	 */
	public void sendFile(File file) {
		try {
			FileReader fReader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(fReader);
			
			while(true) {
				int data = fReader.read();
				
				if(data == -1) {
					break;
				}
				
				sBundle.getOutputStream().writeByte(data);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receiveFile() {
		
	}
}
