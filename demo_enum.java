package workingwith_interfaces;


enum Status{
	Running,Failed,Pending,Success;
}
public class demo_enum {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Status s = Status.Failed;
		System.out.println(s);
		if(s==Status.Running) {
			System.out.println("Running");
		}else if(s==Status.Pending) {
			System.out.println("Pending");
		}else if(s==Status.Success) {
			System.out.println("Success");
		}else
			System.out.println("Failure");
		
		switch(s) {
		case Running: 
			System.out.println("Running");
			break;
		case Pending:
			System.out.println("Pending");
			break;
		case Success:
			System.out.println("Success");
			break;
		case Failed:
			System.out.println("Failure");
			break;
		default:
			System.out.println("dEFAUKT");
			break;
		}
		
		System.out.println(s.getClass().getSuperclass());
		
	}

}
