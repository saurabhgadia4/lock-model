package rtems;
import base.Lock;
import base.Condition;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

public class Mutex extends Lock {
	RTEMSThread holder;
	int id;
	int nestCount;
	int priorityBefore=-1;
	MyComparator comparator = new MyComparator();
	PriorityQueue<RTEMSThread> waitQueue = new PriorityQueue<RTEMSThread>(7, comparator);
	public Object mutexLock = new Object();
	public static final int REC_UPDATE = 1;
  	public static final int NONREC_UPDATE = 0;
	public static int USE_MODEL=NONREC_UPDATE;


	public Mutex(int idx){

		this.id = idx;
		this.nestCount = 0;
		this.priorityBefore = -1;
		this.holder=null;
	}

	public static void setUpdateMethod(int method)
	{
		USE_MODEL = method;
	}

	public synchronized void lock() {
		synchronized(this.mutexLock)
		{
			RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
			while((holder!=null) && (holder!=thisThread))
			{
				assert (thisThread.currentPriority == thisThread.getPriority());
				try{
					thisThread.state = Thread.State.WAITING;
					updatePriority(thisThread.currentPriority);
					if(waitQueue.contains(thisThread)==false){
						System.out.println("Adding thread :" + thisThread.getId() + " in waitQ of mutex: "+id);
						waitQueue.offer(thisThread);
					}
					thisThread.wait = waitQueue;
					thisThread.trylock = this;
					wait();
							
					}catch (InterruptedException e) 
					{}
				
			}
			assert thisThread.getState() != Thread.State.WAITING;
			if(holder==null)
			{
				synchronized(thisThread.rtemsThreadLock)
				{
					holder = thisThread;
					holder.pushMutex(this);
					assert nestCount==0;
				}
			}
			nestCount++;
			thisThread.resourceCount++;
		}
		
	}

	public synchronized void unlock() {
		Mutex topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		assert nestCount>0;
		assert thisThread.resourceCount>0;
		synchronized(this.mutexLock)
		{

			nestCount--;
			thisThread.resourceCount--;
			if(nestCount==0)
			{
					synchronized(thisThread.rtemsThreadLock)
					{
						topMutex = thisThread.mutexOrderList.get(0);
						assert this==topMutex;		
						topMutex = thisThread.mutexOrderList.remove(0);
						thisThread.setPriority(priorityBefore);
						thisThread.currentPriority = priorityBefore;	
					}
				
					validator();
					assert holder!=null;
					assert holder.wait==null;
					assert holder.trylock==null;
				
					holder = waitQueue.poll();			
					if(holder != null){
					assert holder.state==Thread.State.WAITING;
					holder.state = Thread.State.RUNNABLE;
					holder.wait = null;
					holder.trylock = null;
					holder.pushMutex(this);
					notifyAll();
						}
			
			}
		}
					
	}

/*
Validator function checks that after stepping down the priority, on unlock() operation, 
there should be no higher priority thread contending on any of the mutex still held by holder. 
*/

	public void validator(){
		RTEMSThread chkThr;
		Mutex chkMtx;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
			Iterator<Mutex> mItr = thisThread.mutexOrderList.iterator();
			while (mItr.hasNext()){
				chkMtx = mItr.next();
				System.out.println("--->Mutex: "+chkMtx.id);
				chkThr = chkMtx.waitQueue.peek();
				if(chkThr!=null)
				{
					System.out.println("------>Thread-id: "+ chkThr.getId()+" priority: "+ chkThr.getPriority());
					assert (thisThread.getPriority()<=chkThr.getPriority());	
				}
			}

	}

	public boolean priorityRaiseFilter(int priority){
		int holderPriority = holder.getPriority();
		return (priority < holderPriority);
	}

	public void updatePriority(int priority)
	{
		RTEMSThread parentThread;

		synchronized(holder.rtemsThreadLock)
		{
			if(USE_MODEL==REC_UPDATE)
			{
				updateRecPriority(priority);
			}
			else
			{
				updateNonRecPriority(priority);
			}

			if(holder.wait!=null){
				assert holder.trylock!=null;
				reEnqueue();
				parentThread = holder.trylock.holder;
				if(parentThread.currentPriority > holder.currentPriority)
				{
					holder.trylock.updatePriority(holder.currentPriority);
				}
			}
		}
	}

	public void updateNonRecPriority(int priority)
	{
		if(holder.currentPriority > priority){
		holder.currentPriority = priority;
		holder.setPriority(priority);
		}

	}

	public void updateRecPriority(int priority)
	{
		int i;
		Mutex candidate;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		int mutexIdx = this.holder.getMutexIndex(this);
		int stopflag = 0;
		assert this.holder!=null;	
		assert this.holder!= thisThread;	
		//Assertion check
		assert mutexIdx!=-1;
		for(i=mutexIdx-1;i>=0;i--)
		{
			candidate = holder.mutexOrderList.get(i);
			if(candidate.priorityBefore < priority){
				stopflag = 1;
				break;
			}
			candidate.priorityBefore = priority;	
			
		}
		if(stopflag==0)
		{
			if(holder.currentPriority > priority)
			{
				holder.currentPriority = priority;
				holder.setPriority(priority);	
			}	
				
		}
		
	
	}
	
	public void reEnqueue()
	{
		PriorityQueue<RTEMSThread> pqueue;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		pqueue = holder.wait;
		pqueue.remove(holder);
		pqueue.offer(holder);
	}
}

class MyComparator implements Comparator<RTEMSThread>
{
	@Override
	public int compare(RTEMSThread t1, RTEMSThread t2)
	{
		return t1.getPriority() - t2.getPriority();
	}
}
