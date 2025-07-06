package jdbcdemo;

import java.sql.*;

public class InsertingDataIntoADataBase {
	public static void main(String[] args) {
		try {
			//Get connection
			Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store","root","root");
			
			//Create a statement
			Statement myStmt = myConn.createStatement();
			
			//Execute a sql query
			String sql = "insert into customers values (9,'Customer9','cusnine@order.com','Kochi','2024-06-02')";
			myStmt.executeUpdate(sql);
			System.out.println("Data has been inserted");
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
