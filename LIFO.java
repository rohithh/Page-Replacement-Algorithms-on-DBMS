package bufmgr;
import bufmgr.BufMgr;
import diskmgr.*;
class LIFO extends Replacer {
	  
	int numBuffers;
	private int nframes;
	private int frames[];
	protected int head,headStore;		//		The head which acts as a pointer to potentially replaceable frames
	
	  /** Creates a LIFO object. */
	  public LIFO(BufMgr javamgr)
	    {
	      super(javamgr);
	      
	    }
	  
	  public void setBufferManager( BufMgr mgr )
	     {
	        super.setBufferManager(mgr);
	        numBuffers = mgr.getNumBuffers();
	        frames = new int [ mgr.getNumBuffers() ];
	        nframes = 0;
	        head = mgr.getNumBuffers();
	        headStore = head;
	     }
	  
	 
	  
	  /** Picks up the victim frame to be replaced according to
	   * the LIFO algorithm.  Pin the victim so that other
	   * process can not pick it as a victim.
	   *
	   * @return -1 if no frame is available.
	   *         head of the list otherwise.
	   * @throws BufferPoolExceededException.
	   */
	  public int pick_victim() 
	    throws BufferPoolExceededException, 
		   PagePinnedException 
	    {
		  
	      int num = 0;

	      int numBuffers = mgr.getNumBuffers();
	      int frame;
	      int victim;
	      
	      // First we check if the Buffer has space
	      
	       if ( nframes < numBuffers ) {
	           frame = nframes++;
	           state_bit[frame].state = Pinned;
	           (mgr.frameTable())[frame].pin();
	           return frame;
	       }
	      
	       //	If buffer is full, use LIFO technique to reomve a page from memory

	       
	       		if(head!=0)
	       			head = (head-1) % numBuffers;
   				else
   					head = numBuffers-1;	
	       		headStore = head;	//	Used to move head back to oldest frame
	       		while ( state_bit[head].state != Available ) {
	    	
		    		if ( num == 2*numBuffers ) {
		    		  
		    		  throw new BufferPoolExceededException (null, "BUFMGR: BUFFER_EXCEEDED.");
		    		  
		    		}
		    		++num;
		    		if(head!=0)
		    			head = (head-1) % numBuffers;
		    		else
		    			head = numBuffers-1;
		    	      }
		    
	      // Make sure pin count is 0.
	      /** need to convert assert to a similar function. */
	      // assert( (mgr.frameTable())[head].pin_count() == 0 );
	      
	      if ((mgr.frameTable())[head].pin_count() != 0) {
	    	throw new PagePinnedException (null, "BUFMGR: PIN_COUNT IS NOT 0.");
	      }
	      
	      state_bit[head].state = Pinned;        // Pin this victim so that other
	      (mgr.frameTable())[head].pin();    
	      // process can't pick it as victim (???)
	      
	      victim = head;
	      head = headStore;
	      
	      return victim;
	    }
	  
	  /** Returns the name of the LIFO algorithm as a string.
	   *
	   * @return "LIFO", the name of the algorithm.
	   */
	  public final String name() { return "LIFO"; }
	  
	  /** Displays information from LIFO replacement algorithm. */ 
	  public void info()
	    {
	      super.info();
	      System.out.println ("LIFO hand:\t" + head);
	      System.out.println ("\n\n");
	      for (int i = 0; i < nframes; i++) {
	          if (i % 5 == 0)
	  	System.out.println( );
	  	System.out.print( "\t" + mgr.frameTable()[i].pageNo.pid);
	          
	      }
	      System.out.println();
	    }
	  
	  /*
	   * The unpin() method unpins a page when invoked
	   * If the page has a pin count > 0 it decreases it
	   * and if pin count = 0, it changes its state to Available
	   */
	  
	  public boolean unpin( int frameNo ) throws InvalidFrameNumberException, PageUnpinnedException
	  {
		  
	    if ((frameNo < 0) || (frameNo >= (int)mgr.getNumBuffers())) {
	      
	      throw new InvalidFrameNumberException (null, "BUFMGR: BAD_BUFFRAMENO.");
	      
	    }

	   
	    (mgr.frameTable())[frameNo].unpin();

	    if ((mgr.frameTable())[frameNo].pin_count() == 0)
	        state_bit[frameNo].state = Available;
	    return true;

	  }
	  
}