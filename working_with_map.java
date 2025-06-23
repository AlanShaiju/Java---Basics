import java.util.HashMap;
import java.util.Map;
import java.util.Hashtable;

public class working_with_map {
	public static void main(String args[]) {
		
		Map<String, Integer> students = new HashMap<String, Integer>();
		students.put("Allan", 85);
		students.put("Harsh", 95);
		students.put("Sushil", 99);
		students.put("Mark", 77);
		students.put("Tennyson", 55);
		students.put("Harsh", 84);
		
		System.out.println(students);
		System.out.println(students.get("Allan"));
		System.out.println(students.keySet());
		System.out.println(students.values());
		for(String name:students.keySet()) {
			System.out.println(name+" : "+students.get(name));
			
		}
		
	}

}
