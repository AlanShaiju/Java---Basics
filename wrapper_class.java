//Integer, Boolean, Character, Float, Long, Short, Byte, Double
//are called wrapper classes as they wrap around or encapsulate the primitive
//datatype.
public class wrapper_class {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int num =7;
		Integer num1 = new Integer(4);//boxing
		
//boxing - taking a primitive value and storing it into a object

		Integer num2 = num;//Auto-boxing - converting automatically
		int num3 = num2.intValue();//unboxing
		
//unboxing - taking out the primitive value from the object
		int num4 = num2;
		System.out.println(num);
		System.out.println(num1);
		System.out.println(num2);
		System.out.println(num1.getClass());
		System.out.println(num3);
		System.out.println(num4 );
		String str = "5";
		System.out.println(Integer.parseInt(str));
	}

}

//int -> Integer
//char ->Character
//double ->Double


