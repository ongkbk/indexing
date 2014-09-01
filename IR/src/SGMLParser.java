import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Helper class to extract Documents from the SGML tagged corpus
 */
public class SGMLParser {
   private Scanner         reader;
   private String          tagName          = "";
   private String          text             = "";
   public static final int START_ELEMENT    = 1;
   public static final int CHARACTERS       = 4;
   public static final int END_ELEMENT      = 2;
   private long            currLine;
   private int             currDocNo;

   public static String    end_node         = "^</.*>$";
   public static String    start_node       = "^<.*>$";
   public static String    start_node_start = "^<.*";
   public static String    start_node_end   = "^.*>$";

   /**
    * Create a new XML-like parser to read documents.
    * 
    * @param fileReader
    * @throws IOException
    */
   public SGMLParser(FileReader fileReader) throws IOException {
      reader = new Scanner(new BufferedReader(fileReader));
      currLine = 1;
      currDocNo = 1;
   }

   /**
    * Skip lines to reach the relevant document.
    * 
    * @param fileReader
    * @param blockindex
    * @throws IOException
    */
   public SGMLParser(FileReader fileReader, long blockindex) throws IOException {
      this(fileReader);
      while (currLine++ < blockindex) {
         reader.nextLine();
      }
   }

   /**
    * Check if the parser has next token.
    * 
    * @return true if parser has next token, false otherwise.
    */
   public boolean hasNext() {
      return reader.hasNext();
   }

   /**
    * Check if parser has next line
    * 
    * @return true if parser has next line, false otherwise.
    */
   public boolean hasNextLine() {
      return reader.hasNextLine();
   }

   /**
    * Get the next line from the document and return the tag ID of the returned
    * text.
    * 
    * @return tagID matched by START_ELEMENT, CHARACTERS or END_ELEMENT
    */
   public int nextLine() {
      String _temp = reader.nextLine();
      currLine++;

      if (_temp.matches(end_node)) {
         if (_temp.length() > 3)
            tagName = _temp.substring(2, _temp.length() - 1);
         else
            tagName = "";
         return END_ELEMENT;
      } else if (_temp.matches(start_node_start) && !_temp.matches(start_node)) {
         tagName = _temp.substring(1, _temp.length() - 1);
         while (true) {
            _temp = reader.next();
            if (_temp.matches(start_node_end))
               break;
         }
      } else if (_temp.matches(start_node)) {
         if (_temp.length() > 2)
            tagName = _temp.substring(1, _temp.length() - 1);
         else
            tagName = "";
         return START_ELEMENT;
      }
      tagName = "";
      text = _temp;
      return CHARACTERS;

   }

   /**
    * Get the next token and return the tag ID
    * 
    * @return tagID matched by START_ELEMENT, CHARACTERS or END_ELEMENT
    */
   public int next() {
      String _temp = reader.next();

      if (_temp.matches(end_node)) {
         if (_temp.length() > 3)
            tagName = _temp.substring(2, _temp.length() - 1);
         else
            tagName = "";
         return END_ELEMENT;
      } else if (_temp.matches(start_node_start) && !_temp.matches(start_node)) {
         tagName = _temp.substring(1, _temp.length() - 1);
         while (true) {
            _temp = reader.next();
            if (_temp.matches(start_node_end))
               break;
         }
      } else if (_temp.matches(start_node)) {
         if (_temp.length() > 2)
            tagName = _temp.substring(1, _temp.length() - 1);
         else
            tagName = "";
         return START_ELEMENT;
      }
      tagName = "";
      text = _temp;
      return CHARACTERS;

   }

   public String getLocalName() {
      return tagName;
   }

   public String getText() {
      return text;
   }

   public void close() {
      reader.close();
   }

   /**
    * Construct a document header filling in preliminary information.
    * 
    * @return a Document object containing the sample info
    */
   public Document getDocumentHeader() {
      Document currDoc = null;
      String tagContent = "";
      while (this.hasNext()) {
         int event = this.next();
         switch (event) {
         case SGMLParser.START_ELEMENT:
            String _name = this.getLocalName();
            if (_name.equals("DOC")) {
               currDoc = new Document();
               currDoc.docLine = currLine;
               currLine += 2;
            }
            if (_name.equals("DOCNO") || _name.equals("DOCID"))
               tagContent = "";
            else
               continue;
         break;
         // if parser at text, collect them
         case SGMLParser.CHARACTERS:
            String text = this.getText().trim() + " ";
            tagContent += text;
         break;
         case SGMLParser.END_ELEMENT:
            switch (this.getLocalName()) {
            case "DOCID":
               currDoc.id = currDocNo++;
               tagContent = "";
               return currDoc;
            case "DOCNO":
               currDoc.no = tagContent.trim();
               tagContent = "";
            break;
            }
         }
      }
      return currDoc;
   }

   /**
    * Get the entire document from the parser
    * 
    * @return complete info for the document
    */
   public Document getNextDocument() {
      Document currDoc = getDocumentHeader();
      String tagContent = "";
      boolean flag = true;

      while (this.hasNextLine() && flag) {
         int event = this.nextLine();

         switch (event) {
         // if parser at start of tag, clean up the string
         case SGMLParser.START_ELEMENT:
            String _name = this.getLocalName();
            if (_name.equals("DOC")) {
               currDoc = new Document();
            }
            if (_name.equals("HEADLINE") || _name.equals("TEXT")
                  || _name.equals("DOCNO") || _name.equals("DOCID"))
               tagContent = "";
            else
               continue;
         break;
         // if parser at text, collect them
         case SGMLParser.CHARACTERS:
            String text = this.getText().trim() + " ";
            tagContent += text;
         break;
         // if parser at end tag, collect data in tag
         case SGMLParser.END_ELEMENT:
            switch (this.getLocalName()) {
            case "DOC":
               // currDoc.docLine = currLine;
               flag = false;
            break;
            case "DOCID":
               currDoc.id = currDocNo++;
               tagContent = "";
            break;
            case "DOCNO":
               currDoc.no = tagContent.trim();
               tagContent = "";
            break;
            case "HEADLINE":
               currDoc.headline = tagContent.trim();
               tagContent = "";
            break;
            case "TEXT":
               tagContent.trim();
               currDoc.text = tagContent.replaceAll("\\n\\n\\n", "\\n\\n");
               tagContent = "";
            break;
            }
         }
      }
      return currDoc;
   }
}
