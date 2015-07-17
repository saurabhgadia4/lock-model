import scala.collection.mutable.HashSet

object GeneratePermutations {
  def main(args: Array[String]) {
    val locks = List("0", "1", "2")
    val n = 6
    val allChoices =
      Iterable.fill(n)(locks) reduceLeft { (a, b) =>
        for(a<-a;b<-b) yield a+b
      }
    val canonChoices0 =
      (allChoices map(choice =>
	new String(choice.toCharArray.map(ch =>
	  (ch - choice.toCharArray.min + 48).asInstanceOf[Char])))).distinct // subtract minimal string value from each entry to map min char. to 0

    val canonChoices1 = "000000" :: (canonChoices0 filter(_.contains('1')))
    // elements without a "1" are subsumed by strings containing 1,
    // but we have to include 000000 again

    val canonChoices2 = canonChoices1.map(ch =>
      new String(List(ch.substring(0, 2), ch.substring(2, 4), ch.substring(4, 6)).sorted.flatten.toArray)).distinct
    // JPF explores all interleavings (combinations) of
    // the lock choices (2 locks each) of the 3 individual threads,
    // so they can be sorted for a canonical representation
    for (choice <- canonChoices2) {
      Console.out.println(choice)
    }
  }
}
