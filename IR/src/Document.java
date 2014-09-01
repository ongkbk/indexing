public class Document {
   // ancillary doc id
   protected int    id;
   // document's unique indexing label
   protected String no;
   // doc's headline text
   protected String headline;
   // doc's text body
   protected String text;

   // line of doc location
   protected long    docLine;
   // for retrieval only
   protected double avgWeight;

   /**
    * Temporary holder for each individual document
    */
   public Document() {
      id = -1;
      no = "";
   }

   @Override
   public String toString() {
      //@formatter:off
      return String.format(
           "DOCID    : %d\n" + 
           "DOCNO    : %s\n" + 
           "HEADLINE : %s\n" + 
           "TEXT     : %s\n\n", 
           id, no, headline, text);
      //@formatter:on
   }

   public int getByteSize() {
      return searchText().length();
   }

   /**
    * create the text string for indexing. This includes both headline and text
    * body.
    * 
    * @return string for indexing.
    */
   public String searchText() {
      return headline + " " + text;
   }
}
