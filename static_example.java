class mobile{
	String brand;
	int price;
	String network;
	static String name;
	static {
		name="Phone";
		System.out.println("In static block");
	}
	public mobile(){
		brand="";
		price=200;
		System.out.println("Inside constructor");
	}
	public void show() {
		System.out.println(brand + ":"+price+":"+name);
	}
	
	public static void show1(mobile obj) {
		System.out.println("In static method"+obj.brand);
		
	}
}
public class static_example {

		public static void main(String args[]) throws ClassNotFoundException {
			
			Class.forName("mobile");
			
			
//			
//			mobile obj1 = new mobile();
//			obj1.brand="Apple";
//			obj1.price=1500;
//			mobile obj2 = new mobile();
//			obj2.brand="Samsung";
//			obj2.price=1700;
//			
//			obj1.show();
//			obj2.show();
//			mobile.show1(obj1);
//			mobile obj3 = new mobile();
//			obj3.show();
		}
}
//static makes the variable a class member
//static variables should be accessed via class name, and they make a common value for all 
//objects of that class
//only static variables can be used inside a static method
