import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class is implemented as a min-max heap. The root node stores the highest
 * value when the heap is heapified. Retrieve of node starts from the root node
 * and insertion starts at the bottom of the nodes. During retrieval, the top
 * node is removed and the last node replaced the top node and a siftdown().
 * Description of siftdown() is in the javadocs.
 * 
 * During insertion, the value is inserted as a last node. A siftup() function
 * is called. Description of the siftup() function is description in the
 * function
 * 
 * The values of the heap is implemented as an arraylist, and the traversal of
 * the nodes are in the form node_index -> 2*node_index+1 and 2node_index+2 for
 * the left and right leaves.
 * 
 * Adapted from: http://www.youtube.com/watch?v=W81Qzuz4qH0
 */
public class MinHeap {
   private List<Accumulator> items;

   /**
    * Create a new Heap with an empty arraylist. The
    */
   public MinHeap() {
      items = new ArrayList<Accumulator>();
   }

   /*
    * This helper function is called during insertion. It surfaces the highest
    * value to the top of the heap.
    */
   private void siftUp() {
      int currNode = items.size() - 1;
      while (currNode > 0) {
         int p = (currNode - 1) / 2;
         Accumulator item = items.get(currNode);
         Accumulator parent = items.get(p);

         if (item.compareTo(parent) > 0) {
            Accumulator _temp = item;
            // swap
            items.set(currNode, items.get(p));
            items.set(p, _temp);

            currNode = p;
         } else
            break;
      }
   }

   /*
    * This helper function is called during removal. It sinks a lower value to
    * the bottom of the heap. This function is called after the root node is
    * removed and the last node is added to the top of the heap.
    */
   private void siftDown() {
      int currNode = 0;
      int left = 2 * currNode + 1;

      while (left < items.size()) {
         int max = left, right = left + 1;
         if (right < items.size()) {
            // there is a right child
            if (items.get(right).compareTo(items.get(max)) > 0) {
               max++;
            }
         }
         if (items.get(currNode).compareTo(items.get(max)) < 0) {
            Accumulator _temp = items.get(currNode);
            items.set(currNode, items.get(max));
            items.set(max, _temp);
            currNode = max;
            left = 2 * currNode + 1;
         } else
            break;
      }
   }

   /**
    * Insert new item to the heap.
    * 
    * @param item
    */
   public void insert(Accumulator item) {
      items.add(item);
      siftUp();
   }

   /**
    * Delete the root node from the heap and return the value of the root node.
    * 
    * @return root node
    * @throws NoSuchElementException
    *            when no item is left on the heap.
    */
   public Accumulator delete() throws NoSuchElementException {

      if (items.size() == 0)
         throw new NoSuchElementException();
      if (items.size() == 1)
         return items.remove(0);
      Accumulator hold = items.get(0);
      items.set(0, items.remove(items.size() - 1));
      siftDown();
      return hold;
   }

   /**
    * Return the size of the heap
    * 
    * @return size of heap
    */
   public int size() {
      return items.size();
   }

   /**
    * Check if the heap is empty
    * 
    * @return true or false for empty heap
    */
   public boolean isEmpty() {
      return items.isEmpty();
   }

   /**
    * Auxillary function to get arrays of items in a string
    */
   public String toString() {
      return Arrays.toString(items.toArray());
   }

}