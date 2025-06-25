class InnerFirst{
	int age;
	public void show() {
		System.out.println("Class InnerFirst");
	}
	class InnerSecond{
		public void config() {
			System.out.println("Config of InnerSecond");
		}
	}
	static class InnerSecond2{
		public void config() {
			System.out.println("Config of InnerSecond2");
		}
	}
}
public class innerclass_demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InnerFirst obj = new InnerFirst();
		obj.show();
		//InnerSecond obj1 = new InnerSecond();
		InnerFirst.InnerSecond obj1 = obj.new InnerSecond();
		obj1.config();
		InnerFirst.InnerSecond2 obj2 = new InnerFirst.InnerSecond2();
		obj2.config();
		
		
		
	}

}
