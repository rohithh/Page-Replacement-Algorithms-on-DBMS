package bufmgr;
import bufmgr.BufMgr;
import diskmgr.*;
class FIFO extends Replacer {
	  
	private int nframes;
	private int frames[];
	protected int head;		//		The head which acts as a pointer to potentially replaceable frames
	
	  /** Creates a FIFO object. */
	  public FIFO(BufMgr javamgr)
	    {
	      super(javamgr);
	      
	    }
	  
	  public void setBufferManager( BufMgr mgr )
	     {
	        super.setBufferManager(mgr);
		frames = new int [ mgr.getNumBuffers() ];
		nframes = 0;
		head = -1;
	     }
	  
	  /** Picks up the victim frame to be replaced according to
	   * the FIFO algorithm.  Pin the victim so that other
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
	      int victim,headStore;
	      int numBuffers = mgr.getNumBuffers();
	      int frame;


	      // First we check if the Buffer has space
	      
	       if ( nframes < numBuffers ) {
	    	  // System.out.println("nframes < numBuffers");
	           frame = nframes++;
	           frames[frame] = frame;
	           state_bit[frame].state = Pinned;
	           (mgr.frameTable())[frame].pin();
	           return frame;
	       }
	      
	       //	If buffer is full, use FIFO technique to reomve a page from memory

	       
		      head = (head+1) % numBuffers;
		      headStore = head;
		      while ( state_bit[head].state != Available ) {
		    		
		    		
		    		if ( num == 2*numBuffers ) {
		    		  
		    		  throw new BufferPoolExceededException (null, "BUFMGR: BUFFER_EXCEEDED.");
		    		  
		    		}
		    		++num;
		    		head = (head+1) % numBuffers;
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
	  
	  /** Returns the name of the FIFO algorithm as a string.
	   *
	   * @return "FIFO", the name of the algorithm.
	   */
	  public final String name() { return "FIFO"; }
	  
	  /** Displays information from FIFO replacement algorithm. */ 
	  public  void info()
	    {
	      super.info();
	      System.out.println ("FIFO hand:\t" + head);
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