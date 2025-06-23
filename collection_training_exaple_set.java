import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class collection_training_exaple_set {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(7);
		nums.add(3);
		nums.add(4);
		nums.add(5);
		nums.add(1);
		nums.add(6);
		nums.add(8);
		nums.add(9);
		System.out.println(nums);
		System.out.println(nums.get(3));
		
		Set<Integer> nums1 = new HashSet<Integer>();
		nums1.add(37);
		nums1.add(3);
		nums1.add(4);
		nums1.add(5);
		nums1.add(111);
		nums1.add(61);
		nums1.add(38);
		nums1.add(93);
		System.out.println(nums1);
		for (int n:nums1) {
			System.out.print(n+" ");
		}
		// treeset extends sortedset so the values are by default sorted. 
		Set<Integer> nums2 = new TreeSet<Integer>();
		nums2.add(72);
		nums2.add(31);
		nums2.add(34);
		nums2.add(65);
		nums2.add(18);
		nums2.add(66);
		nums2.add(85);
		nums2.add(90);
		System.out.println(nums2);
		for (int n:nums2) {
			System.out.print(n+" ");
		

	}

}
}
//set does not have duplicate values, and it dosent have get method
