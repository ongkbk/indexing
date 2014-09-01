public class Byte {
   protected int[] abyte = new int[8];

   /**
    * read int and parse to variable bytes
    * 
    * @param n
    */
   public void readInt(int n) {
      String bin = Integer.toBinaryString(n);
      for (int i = 0; i < (8 - bin.length()); i++) {
         abyte[i] = 0;
      }
      for (int i = 0; i < bin.length(); i++) {
         abyte[i + (8 - bin.length())] = bin.charAt(i) - 48; // ASCII code
                                                             // for '0' is 48
      }
   }

   /**
    * Switch the first bit to represent continue
    */
   public void switchFirst() {
      abyte[0] = 1;
   }

   /**
    * get the byte representation
    * 
    * @return integer representation of this byte
    */
   public int toInt() {
      int res = 0;
      for (int i = 0; i < 8; i++) {
         res += abyte[i] * Math.pow(2, (7 - i));
      }
      // System.out.println(" Value ***** " + res);
      return res;
   }

   /**
    * Override the default toString method to get binary representation of this
    * byte
    */
   @Override
   public String toString() {
      String res = "";
      for (int i = 0; i < 8; i++) {
         res += abyte[i];
      }
      return res;
   }
}