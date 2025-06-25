//cannot create objects of abstract class
//abstract methods need to be implemented in abstract class
//abstract classes need not have an abstract method
//all abstract needs to be defined in inherited subclass
//abstract classes need not define the abstract method of a parent class

abstract class MyCar {
	public abstract void drive();
	public abstract void fly();
	public void playMusic() {
		System.out.println("Playing Music");
	}
}

//concrete class is a class where all the abstract methods are defined. 
//in this example advancedCar is a Concrete class.

class advancedCar extends MyCar{
	public void drive() {
		System.out.println("Driving");
	}
	public void testing() {
		System.out.println("Testing");
	}
	public void fly() {
		System.out.println("Flying");
	}
}
public class understanding_abstract {

	public static void main(String[] args) {
		advancedCar car = new advancedCar();
		car.playMusic();
		car.drive();
		car.testing();
	}

}
