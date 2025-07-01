package exception_handling_trial;

public class Demo_working_of_try_resources {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int i =0;
		int j = 0;
		try {
			j=j/i;
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
		finally {
			System.out.println("yoyoyuo");
		}
		System.out.println("haha");
	}

}
