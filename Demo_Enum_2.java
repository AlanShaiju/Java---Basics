package workingwith_interfaces;


enum Laptops{
	MacBook(2500),XPS(2200),Surface(1900),ThinkPad(2190);
	private int price;

	private Laptops(int price) {
		this.setPrice(price);
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}


public class Demo_Enum_2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Laptops lap = Laptops.MacBook;
		System.out.println(lap);
		System.out.println(lap.getPrice());
		for(Laptops l : Laptops.values()) {
			System.out.println(l+" | "+l.getPrice());
		}
	}

}
