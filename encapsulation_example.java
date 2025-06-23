class Human{
	 private int age;
	 private String name;
	 private String email;
	 public Human(){
		 System.out.println("In Constructor");
		 age=12;
		 name="Default";
	 }
	 public Human(int age , String name) {
		 this.age=age;
		 this.name=name; 
	 }
	 public int getAge() {
		return age;
	}
	 public void setAge(int age) {
		 this.age = age;
	 }
	 public String getName() {
		 return name;
	 }
	 public void setName(String name) {
		 this.name = name;
	 }
	 public String getEmail() {
		return email;
	}
	 public void setEmail(String email) {
		 this.email = email;
	 }
	
}

// "this" keyword is used to specify the variable within the class itself
//
public class encapsulation_example {

	public static void main(String[] args) {
		Human obj =new Human();
		System.out.println(obj.getName());
		System.out.println(obj.getAge());
		obj.setAge(23);
		obj.setName("AHAHA");
		System.out.println(obj.getAge());
		System.out.println(obj.getName());
		Human obj2 = new Human(12,"Alan");
		System.out.println(obj2.getAge());
		System.out.println(obj2.getName());
	}

}
