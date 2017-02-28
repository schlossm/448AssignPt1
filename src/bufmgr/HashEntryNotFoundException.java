package bufmgr;

import chainexception.ChainException;

public class HashEntryNotFoundException extends ChainException{

	public HashEntryNotFoundException(Object e, String message) {
		// TODO Auto-generated constructor stub
		super((Exception) e, message);
	}

}
