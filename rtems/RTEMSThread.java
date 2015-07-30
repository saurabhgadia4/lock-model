package rtems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	public PriorityQueue<RTEMSThread> wait;
	public int resourceCount;
	public Thread.State state;
	public int currentPriority;
	public int realPriority;
	public List<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.
	public Mutex trylock;
	public java.util.concurrent.locks.Lock current_lock;
	public java.util.concurrent.locks.Lock default_lock =
	  new ReentrantLock();
	public int set_default_lock;

	public RTEMSThread(int priority) {
		this.mutexOrderList = new ArrayList<Mutex>();
		this.state = this.getState();
		this.setPriority(priority);
		this.currentPriority = this.realPriority = this.getPriority();
		this.trylock = null;
		this.set_default_lock = 0;
		current_lock = default_lock;
	}

	public void setCurrentPriority(int priority){
		currentPriority = priority;
	}

	public void setRealPriority(int priority){
		realPriority = priority;
	}

	public int getMutexIndex(Mutex obj){
		return mutexOrderList.indexOf(obj);	
	}

	public void pushMutex(Mutex obj){
		assert !(mutexOrderList.contains(this));
		obj.priorityBefore = currentPriority;
		mutexOrderList.add(0, obj);
		assert wait == null;
		assert trylock == null;	
	}
}
