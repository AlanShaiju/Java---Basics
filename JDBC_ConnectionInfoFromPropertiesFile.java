package jdbcdemo;

import java.util.Properties;
import java.sql.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class JDBC_ConnectionInfoFromPropertiesFile {

	public static void main(String[] args) {
		Statement myStmt = null;
		ResultSet myRs = null;
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("demo.properties"));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String user = props.getProperty("user");
		String password = props.getProperty("password");
		String dburl = props.getProperty("dburl");
		System.out.println("Connecting to database");
		System.out.println("Database URL: " + dburl);
		System.out.println("User: " + user);

		try (Connection myConn = DriverManager.getConnection(dburl, user, password)) {
			System.out.println("Connecting to database");
			System.out.println();
			myStmt = myConn.createStatement();

			// Execute a sql query
			myRs = myStmt.executeQuery("select * from customers");

			// Process the result set
			while (myRs.next()) {
				System.out.println(myRs.getString("CustomerID") + ", " + myRs.getString("Name"));

			}

		} catch (Exception e) {

		}
	}

}
