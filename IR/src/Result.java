
import java.util.Map;
import java.util.TreeMap;

/**
 * Wraper class to hold document frequency for docNo
 * 
 */
public class Result {
   protected String                term;
   protected Map<Integer, Integer> docFrequency = new TreeMap<Integer, Integer>();

   public String toString() {
      return term;
   }
}
