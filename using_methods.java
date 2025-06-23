class Computer{
	public void playMe() {
		System.out.println("Playing Music");
	}
	public String GetMeAPen() {
		return "Pen";
	}
}


public class using_methods {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Computer obj = new Computer();
		obj.playMe();
		String result;
		result = obj.GetMeAPen();
		System.out.println(result);
	}

}
