package mtools.apps.litemessage.console;

import mtools.apps.litemessage.control.logic.MessagingControlModule;

public class ConsoleReceiveMessageHandler extends Thread {
	
	MessagingControlModule cMan;
	
	public ConsoleReceiveMessageHandler(MessagingControlModule mcm) {
		cMan = mcm;
	}
	
	@Override
	public void run() {
		try {
			cMan.startReceiveMessageLogic();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
