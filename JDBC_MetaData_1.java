package jdbcdemo;

import java.sql.*;

public class JDBC_MetaData_1 {

	public static void main(String[] args) {
		try (Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/store", "root", "root");) {
			// Get connection
			Statement myStmt = myConn.createStatement();

			// Execute a sql query
			ResultSet myRs = myStmt.executeQuery("select * from customers");

			ResultSetMetaData rsMetaData = myRs.getMetaData();
			System.out.println("Column count:" +rsMetaData.getColumnCount());
			System.out.println();
			for(int column =1;column<=rsMetaData.getColumnCount();column++) {
				System.out.println("******+++++++******+++++++*******");
				System.out.println("Column name: "+rsMetaData.getColumnName(column));
				System.out.println("Column type name: "+rsMetaData.getColumnTypeName(column));
				System.out.println("Is nullable: "+rsMetaData.isNullable(column));
				System.out.println("Is autoincrement: "+rsMetaData.isAutoIncrement(column));
				System.out.println();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
