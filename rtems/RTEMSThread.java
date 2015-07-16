package rtems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	public PriorityQueue<RTEMSThread> wait;
	public int resourceCount;
	public Thread.State state;
	public int currentPriority;
	public int realPriority;
	public List<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.
	public Mutex trylock;

	public RTEMSThread(int priority) {
		this.mutexOrderList = new ArrayList<Mutex>();
		this.state = this.getState();
		this.setPriority(priority);
		this.currentPriority = this.realPriority = this.getPriority();
		//System.out.println("setting priority = "+getPriority() + " for thread: "+getId());
		this.trylock = null;
	}

	public void setCurrentPriority(int priority){
		currentPriority = priority;
	}

	public void setRealPriority(int priority){
		realPriority = priority;
	}

	public int getMutexIndex(Mutex obj){
		synchronized(this)
		{
			return mutexOrderList.indexOf(obj);	
		}
		
	}

	public void pushMutex(Mutex obj){
		synchronized(this)
		{
			assert !(mutexOrderList.contains(this));
			obj.priorityBefore = currentPriority;
			mutexOrderList.add(0, obj);
			assert wait == null;
			assert trylock == null;	
		}
		
	}
}
