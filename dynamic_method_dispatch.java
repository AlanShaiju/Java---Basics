package access_modifiers;

class a{
	public void show() {
		System.out.println("In  a show");
	}
}

class b extends a{
	public void show() {
		System.out.println("In  b show");
	}
	
}

class c extends a{
	public void show() {
		System.out.println("In  c show");
	}
}



public class dynamic_method_dispatch {
	public static void main(String ars[]) {
		a obj = new a();
		obj.show();
		obj = new b();
		obj.show();
		a obj1 = new b();
		obj1.show();
		obj = new c();
		obj.show();
		
	}
}
