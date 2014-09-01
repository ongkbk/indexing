import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;

public class search {
   public static final double k1 = 1.2;
   public static final double b  = 0.75;

   /*
    * modified main method for search for assignment 2
    */
   public static void main(String[] args) {
      if (!isAssignment2(args))
         assignment1(args);
      else
      // put example as:
      // 0 -BM25
      // 1,2 -q <query-label> {no usable function for search, only for
      // output}
      // 3,4 -n <num-results>
      // 5 lexicon
      // 6 invlists
      // 7 map
      // (8, 9)? -s stoplist
      // (8 or 10)+ query1... queryN
      {
         long then = (new Date()).getTime();
         assignment2(args);
         long now = (new Date()).getTime();

         System.out.printf("Running time: %d ms\n", now - then);
      }
   }

   /*
    * Get the filename from a range of args flags and falls back to
    * defaultFileName if none is found.
    */
   private static String getFileName(String[] args, String flag,
         String defaultFileName) {
      String filename = defaultFileName;
      try {
         for (int i = 0; i < args.length; i++)
            if (args[i].equals(flag))
               return args[i + 1];
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return filename;
   }

   private static void assignment2(String[] args) {
      String[] queries;
      // Create lexicon map from the file
      Map<String, Lexicon> lexicon = new LinkedHashMap<String, Lexicon>();
      /*
       * Creates docMap from file. document text is not read from the
       * corpus/collection. only the docID, docNo, avgDocWeight and index
       * position (for binary inverted list) is read
       */
      Map<Integer, Document> map = new LinkedHashMap<Integer, Document>();
      // stores the collection of partial document weights to be "accumulated"
      // later
      Map<String, Accumulator> accumulatorMap = new HashMap<String, Accumulator>();
      int index = 0;
      int summary_index = 0;
      String sgml_file = "data.sgml";
      if (hasSummary(args)) {
         summary_index += 2;
         sgml_file = getFileName(args, "-SUM", "data.sgml");
      }

      try {
         readLexiconFromFile(lexicon, args[5 + summary_index]);
         readMapFromFile(map, args[7 + summary_index]);
         if (hasStoplist(args))
            index += 2;

         queries = new String[args.length - 8 - index - summary_index];
         for (int j = 0, i = 8 + index + summary_index; i < args.length; i++, j++)
            queries[j] = args[i];

         List<Result> results = getResults(lexicon, map,
               args[6 + summary_index], queries);
         if (results.size() == 0) {
            System.out.println("No document match found. Try again");
            System.exit(0);
         }

         for (Result r : results) {

            Iterator<Entry<Integer, Integer>> iter = r.docFrequency.entrySet()
                  .iterator();
            while (iter.hasNext()) {
               Entry<Integer, Integer> pair = iter.next();
               Document d = map.get(pair.getKey());

               // create bm25 here
               // N is number of doc in collection
               int n = map.size();
               // number of docs containing term t, ft
               int termInCorpus = r.docFrequency.size();
               // document weight (Ld/AL)
               double docWeight = d.avgWeight;

               double bm25 = computeBM25(n, pair.getValue(), termInCorpus,
                     docWeight, k1, b);
               if (!accumulatorMap.containsKey(d.no))
                  accumulatorMap.put(d.no, new Accumulator(d));
               Accumulator accumulate = accumulatorMap.get(d.no);
               accumulate.value += bm25;
            }
         }
         MinHeap heap = new MinHeap();

         for (Accumulator acc : accumulatorMap.values()) {
            heap.insert(acc);
         }

         int numResults = Integer.parseInt(args[4 + summary_index]);
         String label = args[2 + summary_index];
         int count = 1;
         while (!heap.isEmpty() && count <= numResults) {
            Accumulator curr = heap.delete();
            System.out.printf("%s %s %02d %7.3f\n", label, curr.doc.no,
                  count++, curr.value);

            if (hasSummary(args)) {
               String summary1 = generateSummary1FromDoc(curr.doc, sgml_file);
               System.out.println("SUMMARY 1:\n" + summary1);
               String summary2 = generateSummary2FromDoc(curr.doc, sgml_file,
                     queries);
               System.out.println("SUMMARY 2:\n" + summary2);
               System.out.println();
            }
         }

      } catch (Exception e) {
         System.out.println("Whoa! what you did there was not correct");
      }

   }

   private static boolean hasSummary(String[] args) {
      for (String s : args)
         if (s.equals("-SUM"))
            return true;
      return false;
   }

   /**
    * Generate a summary for document. The summary is based on the headline of
    * the document and up to last 320 characters (depending on the document text
    * length)
    * 
    * @param doc
    * @param dataFileName
    * @return
    * @throws IOException
    */
   private static String generateSummary1FromDoc(Document doc,
         String dataFileName) throws IOException {
      SGMLParser reader = new SGMLParser(new FileReader(dataFileName),
            doc.docLine - 1);
      Document _temp = reader.getNextDocument();
      reader.close();
      String output = _temp.headline + "...";

      char[] ch = _temp.text.toCharArray();
      for (int i = ch.length - 320 > 0 ? ch.length - 320 : 0; i < ch.length; i++) {
         output += ch[i];
      }
      ch = output.trim().toCharArray();
      output = "";
      for (int i = 0; i < ch.length; i++) {
         if (i % 80 == 0 && i != 0) {
            output += "\n";
            if (ch[i] == ' ')
               i++;
         }
         output += ch[i];
      }

      return output;
   }

   /**
    * This summary is based on the query-focus. It will extract the head + any
    * sentences that matches any of the query terms.
    * 
    * @param doc
    * @param dataFileName
    * @param searchText
    * @return
    * @throws IOException
    */
   private static String generateSummary2FromDoc(Document doc,
         String dataFileName, String[] searchText) throws IOException {
      SGMLParser reader = new SGMLParser(new FileReader(dataFileName),
            doc.docLine - 1);
      Document _temp = reader.getNextDocument();
      reader.close();

      String output = _temp.headline + "...";
      String[] sentences = _temp.text.split("\\.");
      String output2 = "";
      for (String str : sentences) {
         for (String s : searchText)
            if (str.toLowerCase().contains(s.toLowerCase()))
               output2 += str + "...";
      }
      char[] ch = output2.trim().toCharArray();
      for (int i = ch.length - 320 > 0 ? ch.length - 320 : 0; i < ch.length; i++) {
         output += ch[i];
      }

      ch = output.trim().toCharArray();
      output = "";
      for (int i = 0; i < ch.length; i++) {
         if (i % 80 == 0 && i != 0) {
            output += "\n";
            if (ch[i] == ' ')
               i++;
         }
         output += ch[i];
      }

      return output;
   }

   /**
    * get partial bm25 for term (to be combined with other query terms)
    * 
    * @param n
    *           number of documents in collection
    * @param fdt
    *           in document frequency of term
    * @param ft
    *           number of documents containing term
    * @param docWeight
    *           document length divide by avg document length
    * @param k1
    *           arbitrary parameter
    * @param b
    *           another arbitrary parameter
    * @return calculated bm25 value
    */
   private static double computeBM25(int n, Integer ft, int fdt,
         double docWeight, double k1, double b) {
      double k = k1 * ((1 - b) + b * docWeight);

      return Math.log((n - ft + 0.5) / (ft + 0.5))
            * (((k1 + 1) * fdt) / (k + fdt));
   }

   private static boolean hasStoplist(String[] args) {
      for (String arg : args)
         if (arg.equals("-s"))
            return true;
      return false;
   }

   private static boolean isAssignment2(String[] args) {
      for (String arg : args)
         if (arg.equals("-BM25"))
            return true;
      return false;
   }

   // @Unused
   private static void assignment1(String[] args) {
      if (args.length > 3) {
         try {
            // extract queries from arguments
            String[] queries = new String[args.length - 3];
            Map<String, Lexicon> lexicon = new LinkedHashMap<String, Lexicon>();
            Map<Integer, Document> map = new LinkedHashMap<Integer, Document>();
            readLexiconFromFile(lexicon, args[0]);
            readMapFromFile(map, args[2]);

            for (int i = 3; i < args.length; i++)
               queries[i - 3] = args[i];
            // extract results from queries
            List<Result> results = new ArrayList<Result>();
            results.addAll(getResults(lexicon, map, args[1], queries));
            if (results.size() > 0) {
               for (Result r : results) {
                  System.out.println(r.term);
                  System.out.println(r.docFrequency.size());
                  Iterator<Entry<Integer, Integer>> iterator = r.docFrequency
                        .entrySet().iterator();
                  while (iterator.hasNext()) {
                     Entry<Integer, Integer> pair = iterator.next();
                     System.out.printf("%s %d\n", pair.getKey(),
                           pair.getValue());
                  }
                  System.out.println();
               }
            } else
               System.out.println("No results found");

         } catch (FileNotFoundException e) {
            System.out.println("File:  not found");
            e.printStackTrace();
         } catch (IOException e) {
            System.out.println("Exceptional exception");
         }

      } else {
         System.out
               .println("You have did not specify location of either the lexicon, invlist or map file");
      }

   }

   /*
    * Get results from the lexicon, based on the mapping specified for the
    * documents in the collection
    */
   private static List<Result> getResults(Map<String, Lexicon> lexicon,
         Map<Integer, Document> map, String invlistFile, String[] queries)
         throws IOException {
      List<Result> results = new LinkedList<Result>();
      for (String query : queries) {

         String[] sanitised_query = { query };

         // allowing the program to fail beautifully if query length is 0
         if (query.length() > 1) {
            sanitised_query = sanitiseText(query).split(" ");
         }
         // basic search or compressed search
         // results
         // .addAll(searchBasic(sanitised_query, lexicon, map, invlistFile));

         // compressed search
         results.addAll(searchCompressed(sanitised_query, lexicon, map,
               invlistFile));
      }
      return results;

   }

   /*
    * Working format for dealing with compressed data
    */
   private static List<Result> searchCompressed(String[] sanitised_query,
         Map<String, Lexicon> lexicon, Map<Integer, Document> map,
         String invlistFile) throws IOException, NullPointerException {

      List<Result> results = new LinkedList<Result>();
      DataInputStream file = new DataInputStream(new BufferedInputStream(
            new FileInputStream(invlistFile)));

      for (String q : sanitised_query) {
         int bytesSkipped = 0;
         int bytesToBeRead = 0;

         if (lexicon.containsKey(q)) {
            Lexicon term = lexicon.get(q);
            bytesToBeRead = term.byteSize;
            bytesSkipped = term.pointer;
         } else
            continue;

         file.skip(bytesSkipped);
         byte[] bytes = new byte[bytesToBeRead];
         file.read(bytes);
         LinkedList<Byte> linkedBytes = new LinkedList<Byte>();

         for (byte b : bytes) {
            Byte aByte = new Byte();
            // funky stuff. bit operator to force integer values into bytes
            aByte.readInt(0xFF & b);
            linkedBytes.add(aByte);
         }
         List<Integer> decode = VariableByte.decode(linkedBytes);

         if (decode.size() > 2) {
            int frequency = decode.get(0).intValue();
            Result result = new Result();
            result.term = q;
            for (int i = 1; i < frequency * 2; i += 2) {
               Document doc = map.get(decode.get(i).intValue());
               if (doc != null) {
                  result.docFrequency.put(doc.id, decode.get(i + 1));
               }
            }
            results.add(result);
         }
      }

      file.close();
      return results;
   }

   @SuppressWarnings("unused")
   //@formatter:off
   private static List<Result> searchBasic(
         String[]                   sanitised_query,
         SortedMap<String, Integer> lexicon, 
         Map<Integer, Document>       map,
         String                     invlistFile
         ) throws FileNotFoundException {
   //@formatter:on   
      List<Result> results = new ArrayList<Result>();
      for (String q : sanitised_query) {
         Scanner file = new Scanner(new BufferedReader(new FileReader(
               invlistFile)));

         int index = lexicon.get(q);
         int count = 0;
         String output = null;
         while (file.hasNext()) {
            while (count++ < index) {
               file.nextLine();
            }
            output = file.nextLine();
            break;
         }
         file.close();
         String[] docValues = output.split(" ");
         if (docValues.length > 2) {
            int frequency = Integer.parseInt(docValues[0]);
            Result result = new Result();
            result.term = q;
            for (int i = 1; i < frequency * 2; i += 2) {
               Integer docValue = Integer.parseInt(docValues[i]);
               Document doc = map.get(docValue);
               result.docFrequency.put(doc.id,
                     Integer.parseInt(docValues[i + 1]));
            }
            results.add(result);
         }
      }
      return results;
   }

   /**
    * Creates docMap from file. document text is not read from the
    * corpus/collection. only the docID, docNo, avgDocWeight and index position
    * (for binary inverted list) is read. Due to the increased number of
    * parameters, this will no longer work for assignment 1's parameters.
    */
   private static void readMapFromFile(Map<Integer, Document> map,
         String filename) throws FileNotFoundException {
      Scanner sc = new Scanner(new File(filename));
      while (sc.hasNextLine()) {
         String[] lineParts = sc.nextLine().split(":");
         if (lineParts.length >= 2) {
            Document d = new Document();
            d.id = Integer.parseInt(lineParts[0]);
            d.no = lineParts[1];
            d.avgWeight = Double.parseDouble(lineParts[2]);
            d.docLine = Long.parseLong(lineParts[3]);
            map.put(Integer.parseInt(lineParts[0]), d);
         }
      }
      sc.close();
   }

   /**
    * Load the lexicon from the specified argument
    */
   private static void readLexiconFromFile(Map<String, Lexicon> lexicon,
         String filename) throws FileNotFoundException {
      Scanner sc = new Scanner(new FileReader(filename));
      int pointer = 0;
      while (sc.hasNext()) {
         String[] line = sc.nextLine().split(":");
         if (line.length == 2) {
            Lexicon lexi = new Lexicon(-1, line[0]);
            lexi.pointer = pointer;
            lexi.byteSize = Integer.parseInt(line[1]);
            pointer += Integer.parseInt(line[1]);
            lexicon.put(line[0], lexi);
         }
      }
      sc.close();
   }

   /**
    * case fold the queries, compressing hyphenated words, removing other
    * punctuations
    */
   private static String sanitiseText(String text) {
      String regex = "-";
      text = text.replaceAll(regex, "");

      regex = "\\p{Punct}+";
      text = text.replaceAll(regex, " ");

      regex = "\\s+";
      text = text.replaceAll(regex, " ");
      text = text.toLowerCase();

      return text;
   }

}
