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
    for (e <- elements) {
      if (e.charAt(0) != e.charAt(1) && elements.contains(e.reverse)) {
	return true
      }
    }

    // cycle of length 3
    val s0 = elements(0)
    if (s0.charAt(0) != s0.charAt(1)) {
      for (s1 <- elements) {
	if (s1.charAt(0) != s1.charAt(1) && s1.charAt(0) == s0.charAt(1)) {
	  for (s2 <- elements) {
	    if (s2.charAt(0) != s2.charAt(1) && s2.charAt(0) == s1.charAt(1)
		&& s0.charAt(0) == s2.charAt(1)) {
	      return true
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
      val iso = generateIso(choice).map(s => sortedLockIDs(s, 2)).distinct
      if (!found(iso, results)) {
	results += choice
      }
    }

    val bad = new HashSet[String]
    for (s <- results.toList) {
      val elements = List(s.substring(0, 2), s.substring(2, 4), s.substring( 4,6))
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
