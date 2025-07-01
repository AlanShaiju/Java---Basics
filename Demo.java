package workingwith_interfaces;

//Instead of creating an abstract class that deals with only abstract methods we can deal with

//an interface.
//Multiple interfaces can be inherited together
//Every method in the interface is public abstract, so even if we don't 
//mention it by-default that is the case.
//we can't create objects of interfaces.
//all variables initialized inside the interface is final and static

@FunctionalInterface
interface R {
	void show();
}

@FunctionalInterface
interface S{
	int add(int i, int j);
}

interface A {
	void show();

	void config();
}

interface X {
	void run();
}

interface Y extends X {

}

class B implements A, Y {
	public void show() {
		System.out.println("In show");
	}

	public void config() {
		System.out.println("In config");
	}

	public void run() {
		System.out.println("In run");
	}
}

public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		B obj = new B();
		obj.show();
		obj.config();
		obj.run();

		R obj1 = new R() {
			public void show() {
				System.out.println("Hao halo hao");
			}
		};
		obj1.show();
		R obj2 = () -> System.out.println("Hao halo hao");
		obj2.show();
		S obj3 = (i,j)-> i+j;
		
		System.out.println(obj3.add(2,3));
	}
}
