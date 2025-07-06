package jdbcdemo;

import java.sql.*;

public class StoredProcedures_OUT {
    public static void main(String[] args) {
        Connection myConn = null;
        CallableStatement myStmt = null;

        try {
            // connect
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "root");

            // prepare the stored procedure call
            myStmt = myConn.prepareCall("{call film_in_stock(?, ?, ?)}");

            // set IN parameters
            myStmt.setInt(1, 7); // film id
            myStmt.setInt(2, 1); // store id

            // register the OUT parameter
            myStmt.registerOutParameter(3, java.sql.Types.INTEGER);

            // execute
            System.out.println("Calling procedure: film_in_stock");
            myStmt.execute();

            // get the OUT parameter value
            int filmCount = myStmt.getInt(3);
            System.out.println("Number of copies in stock: " + filmCount);

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
