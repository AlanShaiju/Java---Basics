package jdbcdemo;

import java.sql.*;

public class JDBC_MetaData_2 {

	public static void main(String[] args) {
		try (Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "root", "root")) {
			// Get connection

			// Get meta data
			DatabaseMetaData databaseMetaData = myConn.getMetaData();

			// Display metadata
			System.out.println("Product name: " + databaseMetaData.getDatabaseProductName());
			System.out.println("Product version: " + databaseMetaData.getDatabaseProductVersion());
			System.out.println();
			// Display driver details
			System.out.println("JDBC Driver name: " + databaseMetaData.getDriverName());
			System.out.println("JDBC Driver version: " + databaseMetaData.getDriverVersion());
			System.out.println();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
