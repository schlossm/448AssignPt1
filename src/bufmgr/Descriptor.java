package bufmgr;

import global.PageId;

public class Descriptor {
	public PageId pageno;
	public int pin_count;
	public boolean dirtybit;
	public int index;
	
	
	public Descriptor (int index) {
		
		pin_count = 0;
		this.pageno = new PageId();
		this.dirtybit = false;
		this.index = index;
		
	}
	

	
	
}
