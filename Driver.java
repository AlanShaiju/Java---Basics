package jdbcdemo;

import java.sql.*;

public class Driver {

	public static void main(String[] args) {
		try {
			// Get connection
			Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "root", "root");

			// Create a statement
			Statement myStmt = myConn.createStatement();

			// Execute a sql query
			ResultSet myRs = myStmt.executeQuery("select * from customers");
			while (myRs.next()) {
				System.out.println(myRs.getString("CustomerID") + ", " + myRs.getString("Name"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
