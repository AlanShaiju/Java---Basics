package jdbcdemo;

import java.sql.*;

public class PreparedStatements {

	public static void main(String[] args) {
		Connection myConn = null;
		PreparedStatement myStmt = null;
		ResultSet myRs = null;
		try {
			// Get connection
			myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "root", "root");

			// Prepare the statement
			myStmt = myConn.prepareStatement("select * from orders where CustomerID>? and TotalAmount>?");
			// Set the parameters
			myStmt.setDouble(1, 3);
			myStmt.setDouble(2, 4000);

			// Execute a sql query
			myRs = myStmt.executeQuery();

			// Process the result set
			display(myRs);

			// Reuse section
			System.out.println("Resuse Section:");
			// Set the parameters
			myStmt.setDouble(1, 2);
			myStmt.setDouble(2, 400);

			// Execute a sql query
			myRs = myStmt.executeQuery();

			// Process the result set
			display(myRs);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void display(ResultSet myRs) throws SQLException {
		while (myRs.next()) {
			System.out.println(myRs.getString("CustomerID") + ", " + myRs.getString("TotalAmount"));
		}

	}

}
