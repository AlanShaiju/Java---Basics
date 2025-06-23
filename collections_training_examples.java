//collections, collection api, collection helps make the storing data in datastructures easier
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class collections_training_examples {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Collection<Integer> nums = new ArrayList<Integer>();
		nums.add(7);
		nums.add(3);
		nums.add(4);
		nums.add(5);
		nums.add(1);
		nums.add(6);
		nums.add(8);
		nums.add(9);
		
		System.out.println(nums);
		System.out.println();
		for(int n:nums) {
			System.out.print(n);
		}
		List<Integer> nums1 = new ArrayList<Integer>();
		nums1.add(7);
		nums1.add(3);
		nums1.add(4);
		nums1.add(5);
		nums1.add(1);
		nums1.add(6);
		nums1.add(8);
		nums1.add(9);
		System.out.println(nums1);
		System.out.println(nums1.get(3));

	}

}

//collection is basically an interface, it has more implementations like list,queue,set, etc
//list has arraylist and linkedlist
//queue has queue and dequeue
//set has hashedset anf linked hashed set

