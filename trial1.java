package exception_handling_trial;

class MyException extends Exception{
	public MyException(String str) {
		super(str);
	}
	
}

class A{
	public void show() throws MyException, ClassNotFoundException{
		System.out.println("HAHAHAHA");
		Class.forName("Demo");
		Class.forName("lol");
	}
}
public class trial1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int i = 2;
		int j = 0;
		try {
			j = 18 / i;
			if(j==0) {
				throw new MyException("Dont Print Me Please");
			}
		} catch (MyException e) {
			j=18/1;
			System.out.println("Thats the default expression");
			System.out.println(e);
			//System.out.println("Somethng went wrong:"+e.getMessage());
		}
		System.out.println(j);
		
		A obj = new A();
		try {
			obj.show();
		}
		catch(Exception e) {
			System.out.println("ayayay  "+e);
		}

	}

}
