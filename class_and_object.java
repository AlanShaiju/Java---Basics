//object - properties and behavior
//class - acts as the blueprint for the object
//class -> byte code -> JVM -> object

class Calculator{
	public int add(int a, int b) {
		System.out.println("in add "+1);
		return a+b;
	}
	public int add( int a, int b, int c) {
		return a+b+c;
	}
	public int add( int a, int b, int c,int d) {
		return a+b+c+d;
	}
}

public class class_and_object {

	public static void main(String[] args) {
		
		int x= 4;
		int y=5;
		int result=0;
		
		Calculator calc = new Calculator();
		result=calc.add(x,y);
		System.out.println(result);
		result=calc.add(3,2);
		System.out.println(result);
		result=calc.add(3,2,1);
		System.out.println(result);
		result=calc.add(3,2,1,4);
		System.out.println(result);
	}

}
