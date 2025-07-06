package jdbcdemo;

import java.sql.*;
public class DeleteDataFromADataBase {

	public static void main(String[] args) {
		try {
			//Get connection
			Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store","root","root");
			
			//Create a statement
			Statement myStmt = myConn.createStatement();
			
			//Execute a sql query
			String sql = "delete from customers where CustomerID=9";
			int rowsAffected = myStmt.executeUpdate(sql);
			System.out.println("Data has been deleted"+" | "+"Rows Affected:"+rowsAffected);
			
			ResultSet myRs = myStmt.executeQuery("select * from customers");
			
			//Process the result set
			while(myRs.next()) {
				System.out.println(myRs.getString("CustomerID")+", "+myRs.getString("Name"));
				
				
			}
		
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
