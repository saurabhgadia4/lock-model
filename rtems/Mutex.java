package rtems;
import base.Lock;
import base.Condition;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.concurrent.locks.*;

public class Mutex extends Lock {
	RTEMSThread holder;
	int id;
	int nestCount;
	int priorityBefore=-1;
	MyComparator comparator = new MyComparator();
	PriorityQueue<RTEMSThread> waitQueue = new PriorityQueue<RTEMSThread>(7, comparator);
	public Lock wq_lock = new ReentrantLock();
	public Condition wq_c1 = wq_lock.newCondition();
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

	public void lock() {
		ReentrantLock lock_exh;
		wq_lock.lock();
		try
		{
			RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();

			while((holder!=null) && (holder!=thisThread))
			{
					
					thisThread.current_lock.lock();
					try{
						holder.current_lock.lock();
						try
						{
							if (holder.set_default_lock==1){
								lock_exh = holder.current_lock;
								holder.current_lock = holder.default_lock;
								holder.wait = null;
								holder.trylock = null;
								holder.set_default_lock = 0;
								lock_exh.unlock();
								holder.current_lock.lock();

							}
							assert (thisThread.currentPriority == thisThread.getPriority());
							thisThread.state = Thread.State.WAITING;
							updatePriority(thisThread.currentPriority);
							if(waitQueue.contains(thisThread)==false){
								System.out.println("Adding thread :" + thisThread.getId() + " in waitQ of mutex: "+id);
								waitQueue.offer(thisThread);
							}
							thisThread.wait = waitQueue;
							thisThread.trylock = this;
							thisThread.current_lock = wq_lock;
							thisThread.set_default_lock = 0;
						} finally {
							holder.current_lock.unlock();
						}

					} finally {
						thisThread.current_lock.lock();
					}
					wq_c1.wait();
			
			}
			assert thisThread.getState() != Thread.State.WAITING;
			if(holder==null)
			{
				thisThread.current_lock.lock();
				try
				{
					holder = thisThread;
					holder.pushMutex(this);
					assert nestCount==0;
				}
				finally {
					thisThread.current_lock.unlock();
				}
			}
			//here holder == thisThread
			if(holder.set_default_lock==1)
			{
				holder.current_lock = holder.default_lock;
				holder.wait = null;
				holder.trylock = null;
				holder.set_default_lock = 0;
			}
			nestCount++;
			thisThread.resourceCount++;
		} finally
		{
			wq_lock.unlock();
		}
	}

	public void unlock() {
		Mutex topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		wq_lock.lock();
		try
		{


			assert nestCount>0;
			assert thisThread.resourceCount>0;
			nestCount--;
			thisThread.resourceCount--;
			if(nestCount==0)
			{
				thisThread.current_lock.lock();
				try
				{
					topMutex = thisThread.mutexOrderList.get(0);
					assert this==topMutex;		
					topMutex = thisThread.mutexOrderList.remove(0);
					thisThread.setPriority(priorityBefore);
					thisThread.currentPriority = priorityBefore;	
				
					assert holder!=null;
					assert holder.wait==null;
					assert holder.trylock==null;
					candidateThr = waitQueue.poll(); 
					//holder = waitQueue.poll();			
				}
				finally {
					thisThread.current_lock.unlock();
				}

				//candidateThr just can't get modified here as it is waiting. Only its priority can be changed
				//which we should not worry as itself it is at top of waitqueue and its priority can just go high
				//so we are good.
				//At this point candidate still has reference to parent thread i.e holder of this mutex as
				//we have not yet changed holder.
				//----------------------------------->>>waiting here for updaterecpriority to release candidateThr intrinsic locks
				if(candidateThr != null)
				{
				
					holder = candidateThr;
					assert holder.state==Thread.State.WAITING;
					holder.state = Thread.State.RUNNABLE;
					//holder.wait = null;
					//holder.trylock = null;

					//holder.current_lock = holder.default_lock;
					holder.set_default_lock = 1;
					holder.pushMutex(this);
					wq_c1.notifyAll();							
				
				}
				else
				{
					holder = null;
				}
			
			}
			validator();
		}
		finally{
			wq_lock.unlock();
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
		wq_lock.lock();
		try
		{
			Iterator<Mutex> mItr = thisThread.mutexOrderList.iterator();
			while (mItr.hasNext()){
				chkMtx = mItr.next();
				chkMtx.wq_lock.lock();
				try
				{
					System.out.println("--->Mutex: "+chkMtx.id);
					chkThr = chkMtx.waitQueue.peek();	
					
						if(chkThr!=null)
						{
							System.out.println("------>Thread-id: "+ chkThr.getId()+" priority: "+ chkThr.getPriority());
							assert (thisThread.getPriority()<=chkThr.getPriority());	
						}
				}finally {
					chkMtx.wq_lock.unlock();
				
				}
				
				
			}
		} finally {
			wq_lock.unlock();
		}
	}

	public void updatePriority(int priority)
	{

		RTEMSThread trylockHolder;
		int preUpdateHolderPriority = holder.currentPriority;
		int postUdateHolderPriority = -1;
		if(USE_MODEL==REC_UPDATE)
		{
			updateRecPriority(priority);
		}
		else
		{
			updateNonRecPriority(priority);
		}
		postUdateHolderPriority = holder.currentPriority;

		if((holder.wait!=null) &&(preUpdateHolderPriority!=postUdateHolderPriority)){ 
			
			assert holder.trylock!=null;
			reEnqueue();
			trylockHolder = holder.trylock.holder;
			trylockHolder.current_lock.lock();
			try
			{
				//just need to check whether parentThread still has the holder in it. To confirm that poll has not yet happened
				//i.e holder is not candidate thread choosen by 
				if(trylockHolder.currentPriority > holder.currentPriority)
				{
					holder.trylock.updatePriority(holder.currentPriority);
				}
			} finally {
				trylockHolder.current_lock.unlock();
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
			candidate.wq_lock.lock();
			try
			{
				if(candidate.priorityBefore < priority)
				{
					stopflag = 1;
					break;
				}
				candidate.priorityBefore = priority;	
			} finally {
				candidate.wq_lock.unlock();
			}
				
			
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