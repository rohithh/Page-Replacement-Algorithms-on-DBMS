/* File LRUK.java */

package bufmgr;
import java.util.HashMap;
import java.util.Map;

  /**
   * class LRUK is a subclass of class Replacer using LRUK
   * algorithm for page replacement
   */
public class LRUK extends  Replacer {

  protected int head;
  private final int correlatedReferencePeriod = 0;    //    Correlated reference period
  public static Map<Integer,Long> LAST = new HashMap<Integer,Long>();     //    Stores LAST(PageNumber,Time)
    private int  frames[];    //    Stores frames in the buffer
    public static long currentTime, prevTime;   //  To store the current time
    private int K;    //  K which is the number of last references as specified by the user
  public static long HIST[][];    //    Used to store HIST(PageNumber, Time)

    
  /**
   * private field
   * number of frames used
   */   
  private int  nframes;
  
 /*
  * getFrames() returns the frames in the buffer when called by test4()
  * 
  */
  
  public int[] getFrames() {
    // TODO Auto-generated method stub
    int[] a = new int[mgr.getNumBuffers()+10];
    for(int i=0;i < 51; i++){
      //a[i] = mgr.frameTable()[i].pageNo.pid;
       a[i] = i;
    }
    return a;
  }


/*
 *  back() computes the backward distance
 */
  public long back(int pageNumber, int i) {
    // TODO Auto-generated method stub
    
    long currentTime = System.currentTimeMillis();
    //System.out.println("HIST[pageNumber][i-1]"+HIST[pageNumber][i-1]);
    return LAST.get(pageNumber) - HIST[pageNumber][i];
    
  }  
  
/*
 *HIST returns the HIST array when called 
 */

  public long HIST(int pageNumber, int i) {
    // TODO Auto-generated method stub
  //  long val = LRUK.HIST.value[pagenumber].poll();
    return HIST[pageNumber][i-1];
  }
  
  
  /**
   * Calling super class the same method
   * Initializing the frames[] with number of buffer allocated
   * by buffer manager
   * set number of frame used to zero
   * and a number of other parameters that are used in the program
   * @param mgr a BufMgr object
   * @see BufMgr
   * @see Replacer
   */
    public void setBufferManager( BufMgr mgr )
     {
        super.setBufferManager(mgr);
  frames = new int [ mgr.getNumBuffers() ];
  nframes = 0;
  head = -1;
  currentTime = 0;
  for(int i = 0; i < 10000; i++){
    LAST.put(i, (long) 0);
  }
  
  //initialize HIST()
  
  HIST = new long[10000][K];
  for(int i = 0; i < 10000; i++){
    for(int j = 0; j < K; j++){
      HIST[i][j] = 0;
    }     
  }
     }

/* public methods */

  /**
   * Class constructor
   */
    
    public LRUK(BufMgr mgrArg)
    {
      super(mgrArg);
      frames = null;
      K = 2;
    }
    
    public LRUK(BufMgr mgrArg,int lastRef)
    {
      super(mgrArg);
      frames = null;
      K = lastRef;
    }
  
    
  /**
   * calll super class the same method
   * pin the page in the given frame number 
   * move the page to the end of list  
   *
   * @param  frameNo   the frame number to pin
   * @exception  InvalidFrameNumberException
   */
    
    
/*
 * Function returns the page number of the current frame being referenced 
 */
    
public int currentPage(int frameNo){
  return mgr.frameTable()[frameNo].pageNo.pid;
}
    
/*
 * Used to pin a page and pin count is increased if page already exists in buffer
 */

 public void pin(int frameNo) throws InvalidFrameNumberException
 {
   int currentPage;
    super.pin(frameNo);
    long val;
    if(frameNo >= 0) {
      long lastTimeCalled = LAST.get(currentPage(frameNo));   //  returns time t when page p was LAST called 
        long correlatedPeriodOfReferencedPage;
        /*  The algorithm given is implemented here
         * if p is already in the buffer it does the below steps
         */
     
        currentTime = System.currentTimeMillis(); //  Time is incremented
        
        if(currentTime - lastTimeCalled > correlatedReferencePeriod ){
          currentPage = currentPage(frameNo);
          correlatedPeriodOfReferencedPage = lastTimeCalled - HIST[currentPage][0] ;

          for(int i = 2-1; i <= K-1 ; i++){
            HIST[currentPage][i] = HIST[currentPage][i-1] + correlatedPeriodOfReferencedPage;
          }
          
          HIST[currentPage][0] = currentTime;
          LAST.put(currentPage(frameNo), currentTime);
        }
        else {    //  It is a correlated reference
            LAST.put(currentPage(frameNo), currentTime);
        }
    }
    
 }

  /**
   * This updates the entire queue by moving all elements one spot behind
   */
 
public void update(int pageNumber){
  int i;
  for( i = K-1; i >= 1; i--){     
      HIST[pageNumber][i] = HIST[pageNumber][i-1];
  }
}

/*
 *  Updates HIST to add record of page recently brought into memory
 */

public void updateHist(int frameNo){
  update(currentPage(frameNo));
  HIST[currentPage(frameNo)][0] = prevTime;
  LAST.put(currentPage(frameNo), prevTime);
}

 
 /*
  * Chooses a frame from buffer pool and returns it by applying the LRUK policy
  */


 public int pick_victim() throws BufferPoolExceededException
 {
     int i;
     int numBuffers = mgr.getNumBuffers();
   //  System.out.println("Printing table...\n");
    for(i = 0; i < numBuffers; i++){
      if(i%5 ==0){
      //  System.out.println();
      }
    //  System.out.print("\t"+mgr.frameTable()[i].pageNo.pid);
      
    }
     System.out.println();
   currentTime = System.currentTimeMillis();
   
    //    Check for any empty pages in buffer

    for( i = 0; i < numBuffers; i++){
      if(mgr.frameTable()[i].pageNo.pid == -1){
        LAST.put(currentPage(i), currentTime);
        update(i);
        HIST[i][0] = currentTime;
        state_bit[i].state = Pinned;        // Pin this victim so that other
          (mgr.frameTable())[i].pin(); 
          head = i;
        return i;
      }
    }
   

    //    Check if all pages are pinned or not
   

      for(i = 0 ; i < 50 ;i++){
        if(state_bit[i].state != Pinned){
          break;
        }
      }
    
      if(i == numBuffers)
        throw new BufferPoolExceededException (null, "BUFMGR: BUFFER_EXCEEDED.");
    
      updateHist(head);
        int victim = 0;
    long histValue;
    long lastTimeCalled;
    int currentPage = 0;
    long min = currentTime;
    for( i = 0;i < numBuffers; i++){
      currentPage = currentPage(i);
        lastTimeCalled = LAST.get(currentPage);
      histValue = HIST[currentPage][K-1];
        
      if((currentTime - lastTimeCalled  > correlatedReferencePeriod) && (histValue < min) && state_bit[i].state != Pinned ){        
        /*
         * if eligible for replacement and max backward k distance so far
         */
        
        head = i;
        victim = i;
        min = histValue;
      }
    }
      
      prevTime = currentTime;
      state_bit[head].state = Pinned;        // Pin this victim so that other
        (mgr.frameTable())[head].pin();   

    return victim;
 }
 
  /**
   * get the page replacement policy name
   *
   * @return  return the name of replacement policy used
   */  
    public String name() { return "LRUK"; }
 
  /**
   * print out the information of frame usage
   */  
 public void info()
 {
    super.info();

    System.out.print( "LRUK REPLACEMENT");
    
    for (int i = 0; i < nframes; i++) {
        if (i % 5 == 0)
  System.out.println( );
  System.out.print( "\t" + mgr.frameTable()[i].pageNo.pid);
        
    }
    System.out.println();
 }
 
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

