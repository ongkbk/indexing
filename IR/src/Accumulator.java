/**
 * This class collects all the partial weight for the document
 */
public class Accumulator implements Comparable<Accumulator> {
   protected Document doc;
   protected int      docID;
   protected double   value;

   /**
    * Stores the document's partial weight
    * @param doc
    */
   public Accumulator(Document doc) {
      this.doc = doc;
      this.value = 0;
   }

   @Override
   public int compareTo(Accumulator that) {
      double result = this.value - that.value;
      if (result > 0)
         return 1;
      else if (result == 0)
         return 0;
      else
         return -1;
   }

   public String toString() {
      return doc.no + " " + value;
   }
}
