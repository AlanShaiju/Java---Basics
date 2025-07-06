package jdbcdemo;

import java.sql.*;

public class StoredProcedures_INOUT {
    public static void main(String[] args) {
        Connection myConn = null;
        CallableStatement myStmt = null;

        try {
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "root");

            myStmt = myConn.prepareCall("{call adjust_rental_rate(?)}");

            // set the IN value
            myStmt.setInt(1, 5);  // initial film id

            // register the same parameter as INOUT
            myStmt.registerOutParameter(1, java.sql.Types.DECIMAL);

            // execute
            System.out.println("Calling procedure: adjust_rental_rate");
            myStmt.execute();

            // get the returned OUT value
            double newRate = myStmt.getDouble(1);
            System.out.println("New rental rate: $" + newRate);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (myStmt != null) myStmt.close();
                if (myConn != null) myConn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
