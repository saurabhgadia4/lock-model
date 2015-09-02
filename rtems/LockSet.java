package rtems;

import java.util.ArrayList;

public class LockSet {
	public static ArrayList<Object> locks =
		new ArrayList<Object>(); // to get lock ID
	public int heldLocks = 0; // bit set for 32 locks

	private int getLockID(Object lock) {
		int idx = locks.indexOf(lock);
		if (idx == -1) {
			locks.add(lock);
			idx = locks.size() - 1;
		}
		assert (idx < 32);
		return idx;
	}

	public void addLock(Object lock) {
		heldLocks |= (2 << getLockID(lock));
	}

	public void removeLock(Object lock) {
		heldLocks &= ~(2 << getLockID(lock));
	}

	public int intersect(LockSet that) {
		return heldLocks & that.heldLocks;
	}
}
