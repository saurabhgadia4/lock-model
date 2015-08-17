== Lock model of RTEMS lock in Java, to be used with Java Pathfinder ==

* Code needs to be compiled against jpf.jar (for using Verify).

* Model needs to be "executed" in JPF.

= Wiki page link=
* [https://devel.rtems.org/wiki/GSoC/2015/NestedMutex link] 

== Branch Objective ==

* This branch implements solution for priority inversion problem by removing global lock over mutex class for SMP
  architecture.
* It is spawned from rtemsjpf-0.6-b1 on which test cases with configuration 01-01-12, 01-01-21 and 01-02-12 were failing.

== Branch Status ==

* We were successfull in removing global lock and passing the test cases which were failing on rtemsjpf-0.6-b1.
* This branch passes all the test cases present in good set of testcases.
* This is the final branch for SMP architecture.