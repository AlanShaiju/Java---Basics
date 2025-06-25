class AICD{
	public void show() {
	System.out.println("In show in AICD");
	}
}

class AICD2 extends AICD{
	public void show() {
		System.out.println("In show in AICD2");
	}
}
public class anonymous_inner_class_demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AICD obj = new AICD();
		obj.show();
		AICD2 obj1 = new AICD2();
		obj1.show();
		
// this new way of making a different implementation AICD for obj2
// is called anonymous inner class
		
		AICD obj2 = new AICD() {
			public void show() {
				System.out.println("Hallo in AICD new implementation:);");
			}
		};
		obj2.show();
	}

}
