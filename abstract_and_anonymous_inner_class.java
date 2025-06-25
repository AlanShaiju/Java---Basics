abstract class AAAIC{
	public abstract void show();
}
class AAAIC2 extends AAAIC{
	public void show() {
		System.out.println("IN AAAIC2 SHOWa");
	}
}
public class abstract_and_anonymous_inner_class {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AAAIC2 obj = new AAAIC2();
		obj.show();
		AAAIC obj1 = new AAAIC() {
			public void show() {
				System.out.println("IN AAAIC SHOWa");
			}
		};
		obj1.show();
	}

}
