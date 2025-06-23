
public class conditional_statements {

	public static void main(String[] args) {
		int x = 9;
		int y = 18;
		int z = 10;
		if (x>y) {
			System.out.println("IF LOOP");
		}
		else {
			System.out.println("Else Loop");
		}
		
		if(x>y && x>z) {
			System.out.println(x);
		}
		else if(y>z) {
			System.out.println(y);
		}
		else {
			System.out.println(z);
		}
		int n =5;
		int result = 0;
		result =n%2==0?10:20;
		System.out.println(result);
		n=1;
		switch(5){
		case 1:
			System.out.println("Case 1");
			break;
		case 2:
			System.out.println("Case 2");
			break;
		case 3:
			System.out.println("Case 3");
			break;
		case 4:
			System.out.println("Case 4");
			break;
		default:
			System.out.println("Case Default");
			break;
		}
		

	}

}
