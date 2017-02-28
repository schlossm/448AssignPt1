package bufmgr;

import chainexception.ChainException;

public class PageUnpinnedException extends ChainException {

	public PageUnpinnedException(Object e, String message) {
		// TODO Auto-generated constructor stub
		super((Exception) e,message);
	}
	
}
