
class A{
	public A() {
		System.out.println("In A");
	}
	public A(int n) {
		this();
		System.out.println("In A int");
	}
}
class B extends A{
	public B() {
		super(7);
		System.out.println("In B");
	}
	public B(int n) {
		this();
		System.out.println("In B int");
	}
}

public class this_and_super_method {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		B obj2 = new B(23);
	}

}
