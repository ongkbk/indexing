package searchmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * args[0] contains the lexicon, args[1] contains the inverted list, args[2] is
 * the map of the document numbers args[3] to args[n] are the terms to be
 * searched for
 * 
 * @throws Exception
 */

public class search {

   public static void main(String[] args) {

      List<String> query = new ArrayList<String>();

      // Scanner inread = null;

      System.out.println("Search program");

      try {

         System.out.println("Confirm number of arguments");
         System.out.println(" No. args = " + args.length);

         // File the lexicon
         File vocab = new File(args[0]);

         // File the invlist
         File invert = new File(args[1]);

         // File the map
         File atlas = new File(args[2]);

         // Put the query terms into a list
         for (int i = 3; i < args.length; i++) {
            String word = args[i].toLowerCase().trim();
            query.add(word);
         }

         List<String> vocablist = new ArrayList<String>();

         vocablist = fillAList(vocab);

         System.out.println("Lexicon filled");

         List<String> invlist = new ArrayList<String>();

         invlist = fillAList(invert);

         System.out.println("Inverted list filled");

         // System.out.println("Query terms");

         int dex = 0;

         for (String term : query) {

            System.out.println(" ******* Search term: " + term);
            dex = 0;

            dex = inLexicon(vocablist, term);
            System.out.println("Searched for the query and got " + dex);

            if (dex == 0) {
               System.out.println("Your query for " + term
                     + " returned 0 entries");
               // System.exit(0);
               continue;
            }

            String result = invlistlook(invlist, dex);

            // System.out.println("Fossicked for the listing from the inverted list");

            Result solute = resultSplice(result);

            // System.out.println("Sorted and tokened up the listing from the inverted list");

            List<DocResult> listings = retrieveDocNo(atlas, solute.series);

            /**
             * Print out the results
             */

            // System.out.println("*******  Search Results  *******");
            System.out.println("Frequency of the word for your query: "
                  + solute.termfreq);
            System.out.println("Document No. and the number of times " + term
                  + " appears in the document:");

            System.out.println();

            for (DocResult element : listings) {
               System.out.println(element.docNum + "," + element.freq);
            }

            System.out.println();
         }

      } catch (Exception e) {
         // System.out.println(e.getMessage());
         System.out.println("Something went wrong!");
         System.out
               .println("Check that you included all documents are try again");
      }

      System.out.println("End search");
      System.exit(0);
   }

   /**
    * Fill the lexicon arraylist and the inverted list arraylist from their
    * files and into memory
    * 
    * @throws IOException
    */
   public static List<String> fillAList(File lefile) throws IOException {

      List<String> lexlist = new ArrayList<String>();
      Scanner inread = new Scanner(new FileInputStream(lefile));

      String line = null;
      int counter = 1;
      while (inread.hasNext()) {
         line = inread.nextLine();
         lexlist.add(line);

         counter++;
      }

      inread.close();
      // System.out.println(lexlist.get(0) + " " + lexlist.get(275) + " " +
      // lexlist.get(12576));
      System.out.println(counter + " terms added to the arraylist");

      return lexlist;
   }

   /**
    * Find the search term in the vocab list (lexicon)
    */
   public static int inLexicon(List<String> vocablist, String word) {

      List<String> vblist = vocablist;
      // Search Lexicon
      int counter = 1;
      int dex = 0;
      String line = null;
      String qterm = word;

      // System.out.println("size of vblist " + vblist.size());

      while (counter <= vblist.size()) {

         line = vblist.get(counter - 1);
         if (line.equals(qterm)) {
            dex = counter;
         }

         counter++;
      }

      // System.out.println("And dex = " + dex);

      return dex;

   }

   /**
    * Find where the locations for the search term are in the map
    */
   public static String invlistlook(List<String> raylist, int index) {

      String result = null;

      List<String> indexlist = raylist;

      result = indexlist.get(index);

      return result;
   }

   /**
    * Get the document numbers and term frequencies from the map
    * 
    * @throws FileNotFoundException
    */

   public static List<DocResult> retrieveDocNo(File map, List<DocResult> series)
         throws FileNotFoundException {

      Scanner inread = new Scanner(new BufferedInputStream(new FileInputStream(
            map)));

      List<DocResult> batch = series;

      String match = null;

      while (inread.hasNext()) {

         match = inread.nextLine();

         for (int i = 0; i < batch.size(); i++) {
            if (match.startsWith(batch.get(i).getdocNo())) {
               batch.get(i).setdocNum(match);
               // System.out.println(batch.get(i).docNum);
            }

         }

      }

      inread.close();

      return batch;

   }

   /**
    * Fill result with the document numbers and the term frequencies
    */
   public static Result resultSplice(String answer) {

      Result report = new Result();

      int freq = 0;
      StringTokenizer strtok = new StringTokenizer(answer, " ", false);

      List<DocResult> ids = new ArrayList<DocResult>();

      // System.out.println(strtok.countTokens());

      freq = Integer.parseInt(strtok.nextToken());

      while (strtok.hasMoreTokens()) {
         // System.out.println(strtok.nextToken());

         String docID = strtok.nextToken();
         int docfreq = Integer.parseInt(strtok.nextToken());

         DocResult docre = new DocResult(docID, docfreq);

         ids.add(docre);
         // System.out.println(docre.toString());
      }

      System.out.println("Size of IDs list " + ids.size());

      /*
       * for(DocResult element : ids) { System.out.println(element.docNo + " " +
       * element.freq); }
       */
      report.setTermFreq(freq);
      report.setSeries(ids);

      return report;

   }

}
