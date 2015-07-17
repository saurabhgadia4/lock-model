import scala.collection.mutable.HashSet

/* Create only necessary permutations by first generating all
   string permutations, then mapping each character to a unique
   small integer in order.

   For example, aaabcc and aaacbb can both be mapped to 000122.

   Finally we exploit the fact that the two-character subsequences
   represent lock choices of each thread, whose order does not matter. */

object GeneratePermutations {
  def main(args: Array[String]) {
    val locks = List("a", "b", "c")
    val n = 6
    val allChoices =
      Iterable.fill(n)(locks) reduceLeft { (a, b) =>
        for(a<-a;b<-b) yield a+b
      }

    val canonChoices0 =
      (allChoices map(choice => {
	val firstChar = choice.charAt(0)
	new String(choice.toCharArray.map(ch =>
          if (ch == firstChar) { '0' } else { ch }))
      }))

    val canonChoices1 =
      (canonChoices0 map(choice => {
	val secondChar = choice.toCharArray.find(_ != '0')
	secondChar match {
	  case Some(ch2: Char) =>
	    new String(choice.toCharArray.map(ch =>
              if (ch == ch2) { '1' } else { ch }))
	  case None => choice // do not change string
	}
      }))

    val canonChoices2 =
      (canonChoices1 map(choice => {
	val thirdChar = choice.toCharArray.find(c => (c != '0') & (c != '1'))
	thirdChar match {
	  case Some(ch3: Char) =>
	    new String(choice.toCharArray.map(ch =>
              if (ch == ch3) { '2' } else { ch }))
	  case None => choice // do not change string
	}
      }))

    val canonChoices = canonChoices2.map(ch =>
      new String(List(ch.substring(0, 2), ch.substring(2, 4), ch.substring(4, 6)).sorted.flatten.toArray)).distinct
    // JPF explores all interleavings (combinations) of
    // the lock choices (2 locks each) of the 3 individual threads,
    // so they can be sorted for a canonical representation

    for (choice <- canonChoices) {
      Console.out.println(choice)
    }
  }
}
