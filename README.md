== Lock model of RTEMS lock in Java, to be used with Java Pathfinder ==

* Code needs to be compiled against jpf.jar (for using Verify).

* Model needs to be "executed" in JPF.

= Wiki page link=
* [https://devel.rtems.org/wiki/GSoC/2015/NestedMutex link] 

== Branch Objective ==

* This branch deals with attempt to remove biglock from JPF model.
* This branch is attempt to solve nested mutex problem for SMP architecture and is build upon rtemsjpf-0.6-global-free branch.
* RTEMSThread and Mutex classes consists of data members with similar nomenclature as compared to legacy RTEMS code. 

