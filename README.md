== Lock model of RTEMS lock in Java, to be used with Java Pathfinder ==

* Code needs to be compiled against jpf.jar (for using Verify).

* Model needs to be "executed" in JPF.

= Wiki page link=
* [https://devel.rtems.org/wiki/GSoC/2015/NestedMutex link] 

== Branch Objective ==

* This is a sub-branch of rtemsjpf-0.6-b2
* This branch demonstrates the solution for nested mutex problem in RTEMS targetted for uniprocessor implementation.
* Instead of using global lock we are using Verify.beginAtomic and Verify.endAtomic JPF modules to enforce disable_preemption behavior of RTEMS.