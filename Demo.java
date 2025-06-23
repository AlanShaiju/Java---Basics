class ABA{
	
	int marks = 4;
	public ABA() {
		System.out.println("Inside Constructor");
	}
	public void show() {
		System.out.println("In A Show");
	}
}
public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ABA obj = new ABA();
		obj.show();

	}

}
