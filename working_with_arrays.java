class Student{
	String name;
	int id;
}
public class working_with_arrays {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int nums[]= {3,7,4,2};
		System.out.println(nums[1]);
		for(int x=0;x<nums.length;x++) {
			System.out.print(nums[x]+" ");
		}
		System.out.println();
		for(int n : nums) {
			System.out.println(n);
		}
		Student s1 = new Student();
		Student s2 = new Student();
		Student s3 = new Student();
		s1.name="Alan";
		s1.id=1;
		s2.name="lan";
		s2.id=3;
		s3.name="Ala";
		s3.id=5;
		int nums2[]=new int[4];
		nums2[0]=1;
		nums2[1]=10;
		nums2[2]=19;
		nums2[3]=23;
		System.out.println();
		int num[][]= new int[3][4];
		for(int i =0; i<3;i++) {
			for(int j =0;j<4;j++) {
				num[i][j]= (int)(Math.random()*10);
			}
		}
		for(int i =0; i<3;i++) {
			for(int j =0;j<4;j++) {
				System.out.print(num[i][j]+ " ");
			}
			System.out.println();
		}
		Student students[] = new Student[3];
		students[0]=s1;
		students[1]=s2;
		students[2]=s3;
		for(Student n:students) {
			System.out.println(n.name+ " "+ n.id);
		}	
	}

}
