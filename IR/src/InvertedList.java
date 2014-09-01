/**
 * This class contains a lexicon's index, as well as the frequency of the
 * lexicon appearing in a document
 * 
 * @author kevin
 */
public class InvertedList implements Comparable<InvertedList> {
   protected int docID;
   protected int count;

   /**
    * add a new frequency of this lexicon in the current document no.
    * 
    * @param docNo
    */
   public InvertedList(int docID) {
      this.docID = docID;
      count = 0;
   }

   public void add() {
      this.count++;
   }

   @Override
   public int compareTo(InvertedList that) {
      return docID - that.docID;
   }

}
