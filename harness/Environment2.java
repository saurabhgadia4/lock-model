package harness;

import base.Lock;
import rtems.Mutex;
import rtems.RTEMSThread;
import gov.nasa.jpf.vm.Verify;

public class Environment2 { // 2 threads
  public final static int N_THREADS = 2;
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
    assert(args.length == N_THREADS);
    int prio1 = 2;
    int prio2 = 2;
    switch (Verify.getInt(0, 1)) {
      case 0: prio1 = 1; break; // 1, 2: prios differ
      default: // 1: no need to change the defaults 2, 2: all same
    }
    Mutex.setUpdateMethod(model);
    RTEMSThread t0 =
      new TestThread(new int[]{(int)(new String(args[0]).charAt(0)) - '0',
			       (int)(new String(args[0]).charAt(1)) - '0',
			       (int)(new String(args[0]).charAt(2)) - '0'},
		     prio1);
    t0.start();
    
    //Creating thread 1 trying to acquire lock 2, lock 0
    RTEMSThread t1 =
      new TestThread(new int[]{(int)(new String(args[1]).charAt(0)) - '0',
			       (int)(new String(args[1]).charAt(1)) - '0',
			       (int)(new String(args[1]).charAt(2)) - '0'},
		     prio2);
    t1.start();

    try{
      t0.join();
      t1.join();
      }catch(InterruptedException e){
        e.printStackTrace();
    }
  }
}
