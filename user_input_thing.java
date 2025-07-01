package exception_handling_trial;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class user_input_thing {
	public static void main(String args[]) throws Exception {
		System.out.println("Hahah");
		
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(in);
		int n = Integer.parseInt(br.readLine());
		System.out.println(n);
		br.close();
	}
}
