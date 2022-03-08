package mtools.apps.litemessage.test;

import java.io.IOException;
import java.net.SocketException;

import mtools.apps.litemessage.control.logic.CommandParseModule;
import mtools.apps.litemessage.control.logic.CommandType;
import mtools.apps.litemessage.control.logic.ContactManager;
import mtools.apps.litemessage.control.logic.MessageStatusObject;
import mtools.apps.litemessage.control.logic.MessagingControlModule;
import mtools.apps.litemessage.core.Contact;
import mtools.apps.litemessage.core.MessagingState;
import mtools.apps.litemessage.core.TextDisplayObject;
import mtools.apps.litemessage.core.TextInputObject;
import mtools.apps.litemessage.core.networking.ConnectionManager;
import mtools.apps.litemessage.core.networking.StreamBundle;
import mtools.io.MDisplay;

public class TestServerMessagingControlModule extends MessagingControlModule {

	public TestServerMessagingControlModule(MDisplay dis, TextDisplayObject tdo, TextInputObject tio, ConnectionManager conMan, ContactManager cm) {
		super(dis, tdo, tio, conMan, cm);
	}
	
	public void startTestServerLogic() {
		
		try {
			sBundle = connectionMan.waitForSessionNegotiation();
		} catch(Exception e) {
			System.err.println("Error while reaching back to the peer initiating connection.");
			return;
		}
		
		//If null, something failed.  Need to exit and try again.
		if(sBundle == null) {
			return;
		}
		
		try {
			//Grab the info about the other user.
			parseOtherUserData(sBundle.readUTFData());
			otherUser.setIPAddress(sBundle.getSocket().getInetAddress());
			//Send the info about ourselves
			sBundle.writeUTFData(thisUser.getName() + "," + thisUser.getUID());
		} catch (IOException e) {
			System.err.println("Had issue either sending our user info, or receiving their user info.");
			e.printStackTrace();
			return;
		}		
		
		mState.setMessagingState(MessagingState.CURRENTLY_MESSAGING);
		cMan.addContact(otherUser);
		
		displayCurrentConnections();
		
		this.start();
	}
	
	@Override
	public void run() {
		String message;
		CommandParseModule cpm = new CommandParseModule();
		
		while(true) {
			
			try {
				message = sBundle.readUTFData();
			} catch(IOException e) {
				clearConnections();
				System.out.println(otherUser.getName() + " has left or was disconnected...");
				displayCurrentConnections();
				return;
			}
			
			System.out.println(otherUser.getName() + " said: " + message);
			
			if(cpm.evaluateText(message) == CommandType.EXIT) {
				clearConnections();
				System.out.println(otherUser.getName() + " has left...");
				displayCurrentConnections();
				return;
			}
			
			try {
				sBundle.writeUTFData(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void displayCurrentConnections() {
		System.out.println("\n----------------------");
		System.out.println("  Current connections\n");
		
		if(connectionMan.getNumOfConnections() > 0 ) {
			
			System.out.println("Address, Local port, Remote port");
			
			for(int i = 0; i<connectionMan.getNumOfConnections(); i++) {
				System.out.print(connectionMan.getSockets().get(i).getInetAddress().getHostAddress() + ", ");
				System.out.print(connectionMan.getSockets().get(i).getLocalPort() + ", ");
				System.out.println(connectionMan.getSockets().get(i).getPort());
			}
			
		} else {
			System.out.println("There are no active connections.");
		}
		
		System.out.println("\nEnd of connection list");
		System.out.println("----------------------\n");
	}
}
