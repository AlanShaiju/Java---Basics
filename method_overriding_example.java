
public class method_overriding_example {
	public static void main(String args[]) {
		example2 obj = new example2();
		obj.show();
	}

}

class example2 extends example1{
	public void show() {
		System.out.println("In show method 2");
	}
}


class example1{
	public void show() {
		System.out.println("In show method 1");
	}
}
