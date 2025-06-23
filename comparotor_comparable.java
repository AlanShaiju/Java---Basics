import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Students{
	int age;
	String name;
	
	
	public Students(int age,String name) {
		// TODO Auto-generated constructor stub
		this.age=age;
		this.name=name;
		
	}


	@Override
	public String toString() {
		return "Student [age="+age+",name="+name+"]";
	}
}



public class comparotor_comparable {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Comparator<Students> com= new Comparator<Students>() {
			public int compare(Students i , Students j) {
				
				if(i.age>j.age) {
					return 1;
				}else
					return -1;
			}
		};
		
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(71);
		nums.add(33);
		nums.add(54);
		nums.add(75);
		nums.add(19);
		nums.add(68);
		nums.add(88);
		nums.add(93);
		System.out.println(nums);
//		Collections.sort(nums,com);
		System.out.println(nums);
		List<Students> studs = new ArrayList<Students>();
		studs.add(new Students (1,"Alan"));
		studs.add(new Students (2,"lan"));
		studs.add(new Students (3,"an"));
		studs.add(new Students (4,"Aln"));
		studs.add(new Students (51,"Ala"));
		studs.add(new Students (8,"ana"));
		System.out.println(studs);
		for(Students s:studs) {
			System.out.println(s);
		}
		Collections.sort(studs,com);
		System.out.println(studs);
	}

}
