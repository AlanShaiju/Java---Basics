package workingwith_interfaces;

interface Computer{
	public void code();
}

class Laptop implements Computer{
	public void code() {
		System.out.println("Coding in Laptop");
	}
}

class Desktop implements Computer{
	public void code() {
		System.out.println("Coding in Desktop");
	}
}

class Developer{
	public void devApp(Computer lap) {
		lap.code();
	}
}

public class Demo2 {
	public static void main(String args[]) {
		Computer lap = new Laptop();
		Computer desk = new Desktop();
		Developer dev = new Developer();
		dev.devApp(lap);
		dev.devApp(desk);
	}
}
