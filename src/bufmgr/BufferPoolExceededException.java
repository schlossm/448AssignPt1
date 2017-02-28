package bufmgr;

import chainexception.ChainException;

public class BufferPoolExceededException extends ChainException {

	public BufferPoolExceededException(Object e, String message) {
		// TODO Auto-generated constructor stub
		super((Exception) e, message);
	}

}
