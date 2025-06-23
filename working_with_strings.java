//strings are stored in string constant pool.
//string buffer and string builders provide a way to create immutable strings
//string buffer is thread safe and string builder is not
//string buffer is mutable while string is immutable

public class working_with_strings {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer("Hello World");
		System.out.println(sb.capacity());
		System.out.println(sb.length());
		sb.append(" Hello");
		System.out.println(sb);
		String str = sb.toString();
		System.out.println(str);
		sb.deleteCharAt(3);
		System.out.println(sb);
	}

}
