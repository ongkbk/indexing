import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

public class index {
   /**
    * 
    * @throws IOException
    */
   public static boolean              verbose       = false;
   public static Map<Integer, Double> docWeightList = new LinkedHashMap<Integer, Double>();
   public static Map<Integer, Long>   docLineList   = new LinkedHashMap<Integer, Long>();
   public static Map<Integer, String> docNoList     = new LinkedHashMap<Integer, String>();
   public static Map<String, Lexicon> lexicon       = new TreeMap<String, Lexicon>();

   public static void main(String[] args) {
      try {
         // get stoplist file from flag, defaults to local stoplist
         String stopfile = getFileName(args, "-s", "");
         // read stoplist to hashset
         Set<String> stopSet = readStopList(stopfile);
         // get data file from flag
         String datafile = null;
         // let it throw IndexOutOfBoundException
         datafile = args[args.length - 1];
         datafile = getFileName(args, "-p", datafile);
         for (String arg : args)
            if (arg.contains("-p"))
               // print to stdout if verbose
               verbose = true;

         if (verbose)
            System.out.println("Indexing lexicon...");
         double avgDocLength = parseXML(stopSet, datafile);

         List<Lexicon> lexiconsList = new ArrayList<Lexicon>();
         lexiconsList.addAll(lexicon.values());

         if (verbose)
            System.out.println("Populating postings...");

         // if you want to read content
         generateInvList(lexiconsList, "invlists.txt");
         // compressing list
         LinkedList<Integer> pointers = generateInvListCompressed(lexiconsList,
               "invlists");

         PrintWriter pw;
         Iterator<Entry<Integer, String>> docEntry = docNoList.entrySet()
               .iterator();
         pw = new PrintWriter(new BufferedWriter(new FileWriter("map")));
         while (docEntry.hasNext()) {
            Map.Entry<Integer, String> pair = docEntry.next();
            pw.printf("%d:%s:%f:%d\n", pair.getKey(), pair.getValue(),
                  docWeightList.get(pair.getKey()) / avgDocLength,
                  docLineList.get(pair.getKey()));
         }
         pw.close();

         pw = new PrintWriter(new BufferedWriter(new FileWriter("lexicon")));
         for (Lexicon lexi : lexiconsList) {
            Integer i = pointers.poll();
            pw.println(lexi.name + ":" + i.intValue());
         }
         pw.close();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   // generate the compressed inverted list and return the pointers to each line
   private static LinkedList<Integer> generateInvListCompressed(
         List<Lexicon> lexiconsList, String filename) throws IOException {
      DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(
            new FileOutputStream(filename)));
      LinkedList<Integer> pointers = new LinkedList<Integer>();

      for (Lexicon lex : lexiconsList) {
         int count = lex.getDocumentFrequency();
         LinkedList<Integer> values = new LinkedList<Integer>();
         values.add(count);

         for (InvertedList row : lex.invlist.values()) {
            values.add(row.docID);
            values.add(row.count);
         }

         List<Byte> encodedBytes = VariableByte.encodeAll(values);
         pointers.add(encodedBytes.size());
         for (Byte encoded : encodedBytes) {
            int i = encoded.toInt();
            writer.write((byte) i);
         }
      }
      writer.close();
      return pointers;
   }

   // create inverted list from the lexicon list
   private static void generateInvList(List<Lexicon> lexiconsList,
         String filename) throws IOException {
      PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(
            filename)));
      for (Lexicon lex : lexiconsList) {
         int count = lex.getDocumentFrequency();
         pw.print(count);

         for (InvertedList row : lex.invlist.values()) {
            pw.print(" " + row.docID + " " + row.count);
         }

         pw.println();
      }
      pw.close();

   }

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

   /**
    * Takes the stopword list and the data collection, generate a lexicon with
    * document id, frequency of times it occurs in each document, adding new
    * words to the lexicon as it parse each document
    * 
    * reference: http://woodstox.codehaus.org/
    * 
    * @param stopfile
    *           filename of the stopword list
    * @param datafile
    *           filename of the data file
    * @return
    * @throws FileNotFoundException
    *            if either stopfile or datafile is not found
    * @throws XMLStreamException
    *            if datafile is not properly formatted
    */
   private static double parseXML(Set<String> stopfile, String datafile)
         throws IOException {
      int totDocLength = 0;

      Document currDoc = null;

      SGMLParser reader = new SGMLParser(new FileReader(datafile));
      while ((currDoc = reader.getNextDocument()) != null) {
         generateLexicon(currDoc, stopfile, lexicon);
         docNoList.put(currDoc.id, currDoc.no);
         docWeightList.put(currDoc.id, currDoc.searchText().length() * 1.0);
         docLineList.put(currDoc.id, currDoc.docLine);
         totDocLength += currDoc.getByteSize();
      }
      return totDocLength * 1.0 / docNoList.size();
   }

   /**
    * Populate the lexicon with words from current document
    * 
    * @param currDoc
    * @param stoplist
    * @param lexicon
    * @param postingList
    */
   private static void generateLexicon(Document currDoc, Set<String> stoplist,
         Map<String, Lexicon> lexicon) {

      String searchText = sanitiseText(currDoc.searchText());
      String[] bits = searchText.split(" ");
      int docID = currDoc.id;

      for (String str : bits) {
         if (!stoplist.contains(str) && str.length() > 0) {
            if (!lexicon.containsKey(str)) {
               lexicon.put(str, new Lexicon(lexicon.size() + 1, str));
               if (verbose && str.length() > 1)
                  System.out.print(str + " ");
            }
            Lexicon _lexicon = lexicon.get(str);
            _lexicon.add(docID);
         }
      }
      if (verbose)
         System.out.println();
   }

   /**
    * Removes punctuations, extra spaces and case folding
    * 
    * @param text
    * @return
    */
   private static String sanitiseText(String text) {
      String regex = "-";
      text = text.replaceAll(regex, "");

      regex = "[0-9]*[,][0-9]{3}";
      text = text.replaceAll(regex, "[0-9]*[0-9]{3}");

      regex = "\\p{Punct}+";
      text = text.replaceAll(regex, " ");

      regex = "\\s+";
      text = text.replaceAll(regex, " ");
      text = text.toLowerCase();

      return text;
   }

   /**
    * Reads the stoplist into memory
    * 
    * @param filename
    *           name of the file
    * @return Set of stopwords
    * @throws FileNotFoundException
    */
   private static Set<String> readStopList(String filename)
         throws FileNotFoundException {
      Set<String> stoplist = new HashSet<String>();
      if (filename.length() != 0) {
         Scanner sc = new Scanner(new FileReader(new File(filename)));

         while (sc.hasNext()) {
            stoplist.add(sc.nextLine());
         }
         sc.close();
      }
      return stoplist;
   }
}
