import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

/* Create only necessary permutations of strings representing lock id
   choices.

   We exploit the fact that the two-character subsequences
   represent lock choices of each thread, whose order does not matter. */

object GeneratePermutations {
  val Charset = "012"
  val Perms = Charset.toCharArray.toList.permutations.toList

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
    }
    results.toList
  }

  def sortedLockIDs(in: String) =
    new String(List(in.substring(0, 2), in.substring(2, 4), in.substring(4, 6)).sorted.flatten.toArray)

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
      val iso = generateIso(choice).map(s => sortedLockIDs(s)).distinct
      if (!found(iso, results)) {
	results += choice
      }
    }

    for (choice <- results.toList.sorted) {
      Console.out.println(choice)
    }
  }
}
