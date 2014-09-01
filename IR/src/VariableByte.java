import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapted from http://www.sfs.uni-tuebingen.de/~parmenti/code/VariableByte.java
 */
public class VariableByte {
   // for testing purposes only
   public static void main(String[] args) throws IOException {
      DataInputStream fis = new DataInputStream(new BufferedInputStream(
            new FileInputStream("invlists.bin")));
      fis.skip(0);
      byte[] bytes = new byte[36];
      fis.read(bytes);
      System.out.println(fis.available() + ": " + Arrays.toString(bytes));
      System.out.println();

      LinkedList<Byte> linkedBytes = new LinkedList<Byte>();
      for (byte b : bytes) {
         Byte aByte = new Byte();
         aByte.readInt(0xFF & b);
         linkedBytes.add(aByte);
      }
      List<Integer> decode = decode(linkedBytes);
      for (Integer i : decode)
         System.out.print(" " + i.toString());
      fis.close();

   }

   /**
    * Encode a list of integers into variable byte code
    * 
    * @param numbers
    * @return
    */
   public static LinkedList<Byte> encodeAll(List<Integer> numbers) {
      numbers = addGap(numbers);

      LinkedList<Byte> code = new LinkedList<Byte>();
      for (Integer i : numbers) {
         code.addAll(encodeNumber(i));
      }
      return code;
   }

   /**
    * Encode a number into variable byte code
    * 
    * @param n
    * @return list of variable bytes to represent this integer
    */
   public static List<Byte> encodeNumber(int n) {
      LinkedList<Byte> bytestream = new LinkedList<Byte>();

      while (true) {
         Byte b = new Byte();
         b.readInt(n % 128);
         bytestream.addFirst(b);
         if (n < 128) {
            break;
         }
         // right-shift of length 7 (128 = 2^7)
         n /= 128;
      }
      // retrieving the last byte
      Byte aByte = bytestream.get(bytestream.size() - 1);

      // setting the continuation bit to 1
      aByte.switchFirst();
      return bytestream;
   }

   /**
    * Decode the variable byte codes into integer string
    * 
    * @param bytes
    *           list of bytes to decode into list of integers
    * @return LinkedList of the integers to decode
    */
   public static LinkedList<Integer> decode(LinkedList<Byte> bytes) {
      LinkedList<Integer> numbers = new LinkedList<Integer>();
      int n = 0;
      while (!(bytes.isEmpty())) {
         // read leading byte
         Byte b = bytes.poll();

         // decimal value of this byte
         int bi = b.toInt();

         // continuation bit is set to 0
         if (bi < 128) {
            n = 128 * n + bi;
         } else {
            // continuation bit is set to 1
            n = 128 * n + (bi - 128);
            // number is stored
            numbers.add(n);
            // reset
            n = 0;
         }
      }
      if (numbers.size() > 3)
         numbers = removeGap(numbers);
      return numbers;
   }

   /**
    * <p>
    * use gap for document number, which is every alternate number in the lists
    * from 1,3,5... so on.
    * </p>
    * <p>
    * So if an int array has 16, 1000, 5, 1001, 3 8 it will convert to 16, 1000,
    * 5, 1, 3
    * </p>
    * 
    * @param integers
    *           reduce the space needed for increment of documents
    * @return linked list of the gap between documents
    */

   public static LinkedList<Integer> addGap(List<Integer> integers) {
      int previousNumber = 0;
      LinkedList<Integer> newArr = new LinkedList<Integer>();
      newArr.add(integers.get(0).intValue());

      // discount 0 at the end of the stream
      for (int i = 1; i < integers.size(); i += 2) {
         int _temp = integers.get(i).intValue();
         newArr.add(integers.get(i).intValue() - previousNumber);
         previousNumber = _temp;

         int out = i + 1;
         newArr.add(integers.get(out).intValue());
      }

      return newArr;
   }

   /**
    * <p>
    * Remove the gaps between documents so that the value represent the actual
    * documentNo.
    * </p>
    * <p>
    * The format of the position is as such: \\<freq\\> \\<docNo\\>
    * \\<freqInDoc\\> ...
    * </p>
    * 
    * @param integers
    *           of a string of values
    * @return
    */

   public static LinkedList<Integer> removeGap(LinkedList<Integer> integers) {
      LinkedList<Integer> newArr = new LinkedList<Integer>();
      newArr.add(integers.get(0).intValue());
      int previousValue = 0;
      for (int i = 1; i < integers.size() - 1; i += 2) {
         newArr.add(integers.get(i).intValue() + previousValue);
         previousValue = newArr.get(i);
         int out = i + 1;
         newArr.add(integers.get(out).intValue());
      }

      return newArr;
   }

}