import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

/* Create only necessary permutations of strings representing lock id
   choices.

   We exploit the fact that the two-character subsequences
   represent lock choices of each thread, whose order does not matter. */

// prints cyclic configurations to stderr, non-cyclic ones to stdout
// (cycle detection up to length 3)
// Usage: scala GeneratePermutations > good 2> bad

object GeneratePermutations {
  val Charset = "012"
  val Perms = Charset.toCharArray.toList.permutations.toList

  // check if any of the input strings is found in the hash set
  def found(strs: List[String], in: HashSet[String]): Boolean = {
    for (str <- strs) {
      if (in.contains(str)) {
	return true
      }
    }
    return false
  }

  def generateIso(in: String) = { // generate all isomorphisms over alphabet
    val results = new ListBuffer[String]
    for (p <- Perms) {
      results +=
	new String(in.toCharArray.toList.map(ch =>
					     p(ch.asInstanceOf[Char] - 48)).toArray)
	// map each character to the nth character of the mapped version
	// note that for a quicker look-up, we take advantage of the
	// fact that the input alphabet ranges from "0" up. For the
	// general case, it would be necessary to look up the position of
	// ch in CharSet.
    }
    results.toList
  }

  // sort the string as a combination of strings of length 2
  def sortedLockIDs(in: String, runLength: Int) = {
    new String(in.grouped(runLength).toList.sorted.flatten.toArray)
  }

  def isCyclic(elements: List[String]): Boolean = {
    // cycle of length 2
    for (i <- 0 to elements.size - 1) {
      for (pair <- elements(i).combinations(2)) {
	if (pair.charAt(0) != pair.charAt(1)) {
	  for (j <- i + 1 to elements.size - 1) {
	    if (elements(j).contains(pair.reverse)) {
	      return true
	    }
	  }
	}
      }
    }

    // cycle of length 3
    for (s0 <- elements) {
      for (pair0 <- s0.combinations(2)) {
	if (pair0.charAt(0) != pair0.charAt(1)) {
	  for (s1 <- elements) {
	    for (pair1 <- s1.combinations(2)) {
	      if (pair1.charAt(0) != pair1.charAt(1) &&
		  pair1.charAt(0) == pair0.charAt(1)) {
		for (s2 <- elements) {
		  for (pair2 <- s2.combinations(2)) {
		    if (pair2.charAt(0) != pair2.charAt(1) &&
			pair2.charAt(0) == pair1.charAt(1) &&
			pair0.charAt(0) == pair2.charAt(1)) {
		      return true
		    }
		  }
		}
	      }
	    }
	  }
	}
      }
    }

    // no cycle
    return false
  }

  def main(args: Array[String]) {
    val locks = List("0", "1", "2")
    val n = 6
    val locksPerThread = 2
    val allChoices =
      Iterable.fill(n)(locks) reduceLeft { (a, b) =>
        for(a<-a;b<-b) yield a+b
      }

    // JPF explores all interleavings (combinations) of
    // the lock choices (2 locks each) of the 3 individual threads,
    // so they can be sorted for a canonical representation

    // remove isomorphic strings by attempting to match renamings
    val results = new HashSet[String]
    for (choice <- allChoices) {
      val iso =
	generateIso(choice).map(s => sortedLockIDs(s, locksPerThread)).distinct
      if (!found(iso, results)) {
	results += choice
      }
    }

    val bad = new HashSet[String]
    for (s <- results.toList) {
      val elements = s.grouped(locksPerThread).toList
      if (isCyclic(elements)) {
	bad += s
      }
    }

    for (s <- bad.toList.sorted) {
      Console.err.println(s)
    }

    for (choice <- (results diff bad).toList.sorted) {
      Console.out.println(choice)
    }
  }
}
