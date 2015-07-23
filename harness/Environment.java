package harness;

import base.Lock;
import rtems.Mutex;
import rtems.RTEMSThread;
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
    assert(args.length == 3);
    int prio1 = 2;
    int prio2 = 2;
    int prio3 = 2;
    switch (Verify.getInt(0, 3)) {
      case 0: prio1 = 3; prio3 = 1; break; // 3, 2, 1: all prios differ
      case 1: prio3 = 1; break; // 2, 2, 1: two prios match, one higher
      case 2: prio3 = 3; break; // 2, 2, 3: two prios match: one lower
      default: // 3: no need to change the defaults 2, 2, 2: all same
    }
    Mutex.setUpdateMethod(model);
    RTEMSThread t0 =
      new TestThread(new int[]{(int)(new String(args[0]).charAt(0)) - '0',
			       (int)(new String(args[0]).charAt(1)) - '0'},
		     prio1);
    t0.start();
    
    //Creating thread 1 trying to acquire lock 2, lock 0
    RTEMSThread t1 =
      new TestThread(new int[]{(int)(new String(args[1]).charAt(0)) - '0',
			       (int)(new String(args[1]).charAt(1)) - '0'},
		     prio2);
    t1.start();

    //creating thread 2 trying to acquire lock1, lock2
    RTEMSThread t2 =
      new TestThread(new int[]{(int)(new String(args[2]).charAt(0)) - '0',
			       (int)(new String(args[2]).charAt(1)) - '0'},
		     prio2);
    t2.start();
  }
}
