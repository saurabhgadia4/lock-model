package rtems;

import java.util.ArrayList;

public class LockSet {
	public static ArrayList<Object> locks =
		new ArrayList<Object>(); // to get lock ID
//	public static ArrayList<String> lockDesc = new ArrayList<String>();
	public int heldLocks = 0; // bit set for 32 locks

	private int getLockID(Object lock, String description) {
		int idx = locks.indexOf(lock);
		if (idx == -1) {
			locks.add(lock);
//			lockDesc.add(description);
			idx = locks.size() - 1;
			System.out.println("Lock " + idx + " is " + description);
		}
		assert (idx < 32);
		return idx;
	}

	public void addLock(Object lock, String description) {
		int id = getLockID(lock, description);
		heldLocks |= (2 << id);
	}

	public void removeLock(Object lock) {
		heldLocks &= ~(2 << getLockID(lock, null));
	}

	public int intersect(int current) {
		return heldLocks & current;
	}
}
