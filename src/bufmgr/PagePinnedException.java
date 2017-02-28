package bufmgr;

import chainexception.ChainException;

public class PagePinnedException extends ChainException {

	public PagePinnedException(Object e, String message) {
		// TODO Auto-generated constructor stub
		super((Exception) e, message);
	}

}
