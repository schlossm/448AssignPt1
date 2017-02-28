package bufmgr;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import diskmgr.FileIOException;
import diskmgr.InvalidPageNumberException;


import chainexception.ChainException;
import global.Page;
import global.PageId;
import global.Minibase;

public class BufMgr 
{
	//buffer pool
	Page[] frames;
	//hashmap that stores the index of a page's page id 
	HashTable hashTable;
	//frame descriptor array
	Descriptor[] bufDescr;
	//Stack that will be used for MRU replacement policy
	Stack<Integer> replaceCandidates;
	
	/**
	* Create the BufMgr object.
	* Allocate pages (frames) for the buffer pool in main memory and
	* make the buffer manage aware that the replacement policy is
	* specified by replacerArg (e.g., LH, Clock, LRU, MRU, LFU, etc.).
	*
	* @param numbufs number of buffers in the buffer pool
	* @param lookAheadSize: Please ignore this parameter
	* @param replacementPolicy Name of the replacement policy, that parameter will be set to "MRU" (you
	can safely ignore this parameter as you will implement only one policy)
	 * @return 
	*/
	public BufMgr(int numbufs, int lookAheadSize, String replacementPolicy)
	{
		frames = new Page[numbufs];
		
		hashTable = new HashTable(numbufs);
		replaceCandidates = new Stack<Integer>();
		bufDescr = new Descriptor[numbufs];
		
		for (int i = 0; i < numbufs; i++) {
			frames[i] = new Page();
			bufDescr[i] = new Descriptor(i);
			//System.out.println("Pushed " + i);
			replaceCandidates.push(i);
		}
	}

	/**
	* Pin a page.
	* First check if this page is already in the buffer pool.
	* If it is, increment the pin_count and return a pointer to this
	* page.
	* If the pin_count was 0 before the call, the page was a
	* replacement candidate, but is no longer a candidate.
	* If the page is not in the pool, choose a frame (from the
	* set of replacement candidates) to hold this page, read the
	* page (using the appropriate method from {\em diskmgr} package) and pin it.
	* Also, must write out the old page in chosen frame if it is dirty
	* before reading new page.__ (You can assume that emptyPage==false for
	* this assignment.)
	*
	* @param pageno page number in the Minibase.
	* @param page the pointer point to the page.
	* @param emptyPage true (empty page); false (non-empty page)
	 * @throws IOException 
	*/
	public void pinPage(PageId pageno, Page page, boolean emptyPage) throws ChainException, IOException
	{
		
		//Check if page is in frames
		if (hashTable.containsKey(pageno.pid)) {
			//Get frame index from hash table
			int position = hashTable.get(pageno.pid);
			
			//If the pin count was 0, remove from replacement stack
			if (bufDescr[position].pin_count == 0) {	
				//System.out.println("Popped " + replaceCandidates.peek());
				replaceCandidates.pop();
			}
			
			bufDescr[position].pin_count++;
			page.setPage(frames[position]);
				
			
		}
		else {
			//If there are no valid unused frames throw an error
			if (replaceCandidates.isEmpty() == true) {
				throw new BufferPoolExceededException(null, "BUFMGR: NO_UNPINNED_FRAMES.");
			}
			//Pop a frame to be replaced
			int position = replaceCandidates.pop();
			//System.out.println("Popped " + position);
			//If the frames page id is valid, then flush to disk if the dirty bit is set, and if the old page is in the hash map, remopve it
			if (bufDescr[position].pageno.pid != -1) {
				if (bufDescr[position].dirtybit) {
					flushPage(bufDescr[position].pageno);
				}
				if (hashTable.containsKey(bufDescr[position].pageno.pid)) {
					hashTable.remove(bufDescr[position].pageno.pid);
				}
				
			}
			
				
			
			
			page.setPage(frames[position]);
			//Read in the page and set its values in the buffer descriptor
			Minibase.DiskManager.read_page(pageno, page);
			bufDescr[position].pageno.pid = pageno.pid;
			bufDescr[position].pin_count = 1;
			bufDescr[position].dirtybit = false;
			//Add new page info into the hash table
			hashTable.put(pageno.pid, position);
			
			
			
		}
		
	}
	/**
	* Unpin a page specified by a pageId.
	* This method should be called with dirty==true if the client has
	* modified the page.
	* If so, this call should set the dirty bit
	* for this frame.
	* Further, if pin_count>0, this method should
	* decrement it.
	*If pin_count=0 before this call, throw an exception
	* to report error.
	*(For testing purposes, we ask you to throw
	* an exception named PageUnpinnedException in case of error.)
	*
	* @param pageno page number in the Minibase.
	* @param dirty the dirty bit of the frame
	 * @throws Exception 
	*/
	public void unpinPage(PageId pageno, boolean dirty) throws ChainException
	{
		
		if (hashTable.containsKey(pageno.pid) == false) {
			throw new HashEntryNotFoundException(null, "BUFMGR: HASH_ENTRY_NOT_FOUND");
		}
		else {
			
			int index = hashTable.get(pageno.pid);
			if (bufDescr[index].pin_count == 0) {
				//can't unpin a page whose pin count is already 0
				throw new PageUnpinnedException(null, "BUFMGR: PAGE_NOT_PINNED.");
			}
			else {
				//Set the dirtybit and decrement pin count
				bufDescr[index].dirtybit = dirty;
				bufDescr[index].pin_count--;
				//If the pin count now == 0, add the page to the replacement stack
				if (bufDescr[index].pin_count == 0) {
					//System.out.println("Pushed " + index);
					replaceCandidates.push(index);
				}
			}
			
		}
		
		
		
		
	}
	/**
	* Allocate new pages.
	* Call DB object to allocate a run of new pages and
	* find a frame in the buffer pool for the first page
	* and pin it. (This call allows a client of the Buffer Manager
	* to allocate pages on disk.) If buffer is full, i.e., you
	* can't find a frame for the first page, ask DB to deallocate
	* all these pages, and return null.
	*
	* @param firstpage the address of the first page.
	* @param howmany total number of allocated new pages.
	*
	* @return the first page id of the new pages.__ null, if error.
	 * @throws ChainException 
	 * @throws IOException 
	*/
	public PageId newPage(Page firstpage, int howmany) throws IOException, ChainException
	{
		//if there are no available frames, return null
		if (getNumUnpinned() == 0) {
			return null;
		}
		PageId firstId;
		
		firstId = Minibase.DiskManager.allocate_page(howmany);
		pinPage(firstId, firstpage, false);
		
		return firstId;
	}
	/**
	* This method should be called to delete a page that is on disk.
	* This routine must call the method in diskmgr package to
	* deallocate the page.
	*
	* @param globalPageId the page number in the data base.
	*/
	public void freePage(PageId globalPageId) throws ChainException
	{
		
		if (hashTable.get(globalPageId.pid) == null) {
			return;
		}
		
		int index = hashTable.get(globalPageId.pid);
		Descriptor d = bufDescr[index];
		if (d.pin_count > 0) {
			throw new PagePinnedException(null, "BUFMGR: TRIED_TO_FREE_PINNED_PAGE.");
		}
		hashTable.remove(globalPageId.pid);
		Minibase.DiskManager.deallocate_page(globalPageId);
	}
	/**
	* Used to flush a particular page of the buffer pool to disk.
	* This method calls the write_page method of the diskmgr package.
	*
	* @param pageid the page number in the database.
	 * @throws IOException 
	 * @throws FileIOException 
	 * @throws InvalidPageNumberException 
	*/
	public void flushPage(PageId pageid) throws InvalidPageNumberException, FileIOException, IOException
	{
		if (hashTable.get(pageid.pid) == null) {
			return;
		}
		int position = hashTable.get(pageid.pid);
		Minibase.DiskManager.write_page(pageid, frames[position]);
		
	}
	/**
	* Used to flush all dirty pages in the buffer pool to disk
	 * @throws IOException 
	 * @throws FileIOException 
	 * @throws InvalidPageNumberException 
	*
	*/
	public void flushAllPages() throws InvalidPageNumberException, FileIOException, IOException
	{
		for (int i = 0; i < frames.length; i++) {
			if (bufDescr[i].dirtybit) {
				Minibase.DiskManager.write_page(bufDescr[i].pageno, frames[i]);
				bufDescr[i].dirtybit = false;
			}
		}
		
	}
	/**
	* Returns the total number of buffer frames.
	*/
	public int getNumBuffers()
	{
		return frames.length;
	}
	/**
	* Returns the total number of unpinned buffer frames.
	*/
	public int getNumUnpinned()
	{
		int count = 0;
		for (int i = 0; i < frames.length; i++) {
			if (bufDescr[i].pin_count == 0) {
				count++;
			}
		}
		return count;
	}
}

