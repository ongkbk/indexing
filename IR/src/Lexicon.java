import java.util.SortedMap;
import java.util.TreeMap;

public class Lexicon implements Comparable<Lexicon> {
   protected int                              index;
   protected String                           name;
   protected SortedMap<Integer, InvertedList> invlist;
   protected int                              pointer;
   protected int                              byteSize;

   /**
    * Create a lexicon based on the term string.
    * 
    * @param index
    *           nth term to be inserted to the lexicon list
    * @param name
    *           the term string
    */
   public Lexicon(int index, String name) {
      this.index = index;
      this.name = name;
      this.invlist = new TreeMap<Integer, InvertedList>();
   }

   /**
    * Add a term frequency for the current document id
    * 
    * @param docID
    */
   public void add(int docID) {
      if (!invlist.containsKey(docID))
         invlist.put(docID, new InvertedList(docID));
      InvertedList list = invlist.get(docID);
      list.add();
   }

   /**
    * Get the number of times this term appears in each document across a
    * collection
    * 
    * @return the total number of times this term appears in each doc in the
    *         collection
    */
   public int getDocumentFrequency() {
      return invlist.size();
   }

   @Override
   public int compareTo(Lexicon that) {
      return name.compareTo(that.name);
   }

   /*
    * making sure the object is matched accordingly
    */
   public boolean equals(Object o) {
      return ((Lexicon) o).toString().equals(this.toString());
   }

   /*
    * Minimizing hash collision (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode() {
      String hashed = index + ":" + name;
      return hashed.hashCode();
   }

   public String toString() {
      return name;
   }

}
