package mtools.apps.litemessage.console;

import mtools.apps.litemessage.core.TextInputObject;
import mtools.io.MConsole;

/**
 * A wrapper for {@link MConsole} to be TextInputObject compliant
 * @author Noah
 *
 */
public class ConsoleTextInput extends MConsole implements TextInputObject {

	@Override
	public String readString() {
		return this.getInputString();
	}

}
