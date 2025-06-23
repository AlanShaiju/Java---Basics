package access_modifiers;


class d{
	public void show() {
		System.out.println("Haii");
			
	}
	public void meh() {
		System.out.println("Jolly good day");
	}
}
public class final_keyword_example {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final int num = 8;
//		num=0;
		System.out.println(num);
		d sad = new d();
		sad.show();
		sad.meh();

	}

}
// final keywords makes the value of a variable constant
//final ensures that the variable remains the same throughout the execution
// "final" helps stop the inheritance of a class, essentially if a class is final then it cannot be inherited
//"final" keyword in method, ensures it is the final method and it cannot be overriden
