== Lock model of RTEMS lock in Java, to be used with Java Pathfinder ==

* Code needs to be compiled against jpf.jar (for using Verify).

* Model needs to be "executed" in JPF.

= Wiki page link=
* [https://devel.rtems.org/wiki/GSoC/2015/NestedMutex link] 

== Branch Objective ==

* This branch deals with attempt to remove biglock from JPF model.
* This branch is spawned from rtemsjpf-0.6-global-free.
* This branch will replicate TCB more closely to RTEMS project.
* This branch deals with attempt to solve nested mutex problem for SMP architecture without using global lock.
* It is spawned from rtemsjpf-0.6-b1 on which test cases with configuration 01-01-12, 01-01-21 and 01-02-12 were failing.
* Motivation for this branch was to pass test cases which failed on rtemsjpf-0.6-b1.
* At last this branch successfully passes above test cases.
* We still need test all "good" testcases again to validate this branch.