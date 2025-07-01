package workingwith_interfaces;


class ABC{
	public void showTheDataWhichBelongsToThisClass() {
		System.out.println("Failure IN ABC");
	}
}
class BC extends ABC{
	@Override  
	public void showTheDataWhichBelongsToThisClass() {
		System.out.println("Failure in BC");
	}
}

public class Demo3 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BC obj = new BC();
		obj.showTheDataWhichBelongsToThisClass();
	}

}
