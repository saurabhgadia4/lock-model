== Lock model of RTEMS lock in Java, to be used with Java Pathfinder ==

* Code needs to be compiled against jpf.jar (for using Verify).

* Model needs to be "executed" in JPF.

= Wiki page link=
* [https://devel.rtems.org/wiki/GSoC/2015/NestedMutex link] 

== Branch Objective ==

* This branch implements solution for priority inversion problem for uniprocessor as well as SMP architecture with the help of global lock over mutex class.
* It is spawned from uniproc-new1 and further developed.

== Branch Status ==

* We were successfull in developing solution for priority inversion problem for both uniprocessor as well as SMP architecture.
* This branch passes all the test cases present in good set of testcases.
* This is the final branch for global lock solution.

== Test Case Description ==

* perms file under root directory has all the valid combination of test cases configuration. Eg. 010102 -> 01-01-02 Our system consist of a set of threads T = {T0, T1, T2} and a set of mutexes M = {M0, M1, M2}. From above example thread T0 acquires Mutex (0,1), T1 acquires (0,1) and T2 acquires (0, 2).

* From all combinations of thread and mutex we have to further segregate perm file to good and bad combinations.
* Bad combinations are the one which by default lead to deadlock based on locking scheme. eg.011020.
