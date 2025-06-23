
public class arithmetic_operators {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int num1 =2;
		int num2= 4;
		System.out.println(num1 + num2);
		System.out.println(num1 - num2);
		System.out.println(num1 * num2);
		System.out.println(num1 / num2);
		System.out.println(num1 % num2);
		System.out.println(num1++);
		System.out.println(num1);
		System.out.println(++num1);
		System.out.println(num1*2);
		System.out.println(num1*=2);
		System.out.println(num1/=2);
		System.out.println(num1+=2);
		System.out.println(num1-=2);
		boolean result = num1<num2;
		System.out.println(result);
		result = num1>num2;
		System.out.println(result);
		result= num1==num2;
		System.out.println(result);
		num1=2;
		result = num1<num2;
		System.out.println(result);
		result = num1>num2;
		System.out.println(result);
		result= num1==num2;
		System.out.println(result);
		
		int x =7;
		int y =5;
		int a = 5;
		int b =9;
		result = x<y || a<b;
		System.out.println(!result);
	}

}
