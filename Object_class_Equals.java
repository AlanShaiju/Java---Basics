package access_modifiers;

class Laptop{
	String model;
	int price;
	public String toString() {
		return model+" : "+price;
	}
	public boolean equals(Laptop that) {
		return this.model==that.model && this.price==that.price;
	}
}

public class Object_class_Equals {
	public static void main(String args[]) {
		
		Laptop obj = new Laptop();
		obj.price=1000;
		obj.model="Lenovo";
		Laptop obj2 = new Laptop();
		obj2.price=1000;
		obj2.model="Lenovo";
		
		System.out.println(obj.price+":"+obj.model);
		System.out.println(obj);
		System.out.println(obj2);
		boolean result = obj.equals(obj2);
		System.out.println(result);
		
	}
	
}
