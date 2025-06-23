
class calc{
	
	public int add(int n1, int n2) {
		return n1+n2;
	}
	public int sub(int n1, int n2) {
		return n1-n2;
	}
}

public class Demo2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AdvCalc obj1= new AdvCalc();
		int d1 = obj1.div(50,2);
		int d2 = obj1.multi(23, 78);
		int d3 = obj1.add(1,9);
		int d4 = obj1.add(9, 10);
		System.out.println(d3+ " "+ d4);
		System.out.println(d1+ " "+ d2);
		
		calc obj = new calc();
		int r1 = obj.add(1,9);
		int r2 = obj.add(9, 10);
		System.out.println(r1+ " "+ r2);
		r1 = obj.sub(1,9);
		r2 = obj.sub(9, 10);
		System.out.println(r1+ " "+ r2);
		
		VeryAdvCalc obj3= new VeryAdvCalc();
		int v1 = obj3.div(50,2);
		int v2 = obj3.multi(23, 78);
		int v3 = obj3.add(1,9);
		int v4 = obj3.add(9, 10);
		double v5 = obj3.power(9, 3);
		System.out.println(v3+ " "+ v4);
		System.out.println(v1+ " "+ v2);
		System.out.println(v5);

	}

}
