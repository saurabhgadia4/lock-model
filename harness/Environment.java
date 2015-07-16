package harness;

import base.Lock;
import rtems.Mutex;
import rtems.RTEMSThread;

//import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.vm.Verify;

public class Environment {
  public final static int N_THREADS = 3;
  /*
  Mutex.REC_UPDATE -  for solution model.
  Mutex.NONREC_UPDATE -  for RTEMS default model.
  */

  public final static int model = Mutex.REC_UPDATE;
  static final Lock[] locks = {createLock(0), createLock(1), createLock(2)};

  static Lock createLock(int id) {
    // factory method to swap out lock impl. in one place
    return new /*Prio*/Mutex(id);
  }

  public final static void main(String[] args) {
    /*int li1 = Verify.getInt(0, locks.length - 1);
    int li2 = Verify.getInt(0, locks.length - 1);
    int li3 = Verify.getInt(0, locks.length - 1);*/
    int li1 = 0;
    int li2 = 1;
    Mutex.setUpdateMethod(model);
    RTEMSThread t0 = new TestThread(new int[]{li1, li2}, 3);
    //t0.setPriority(Verify.getInt(1, 3));
    t0.setPriority(3);
    t0.setRealPriority(3);
    t0.setCurrentPriority(3);
    System.out.println("Thread 0 has priority " + t0.getPriority() +
		       " and uses locks " + li1 + //", " + li2 +
		       ", and " + li2 + ".");
    t0.start();
    
    //Creating thread 1 trying to acquire lock 2, lock 0
    RTEMSThread t1 = new TestThread(new int[]{0, 2}, 2);
    t1.start();

    //creating thread 2 trying to acquire lock1, lock2
    RTEMSThread t2 = new TestThread(new int[]{2, 0}, 1);
    t2.start();

    System.exit(1);
  }
}
