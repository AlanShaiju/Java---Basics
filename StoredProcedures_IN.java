package jdbcdemo;

import java.sql.*;

public class StoredProcedures_IN {
    public static void main(String[] args) {
        Connection myConn = null;
        CallableStatement myStmt = null;
        ResultSet myRs = null;

        try {
            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "root");

            myStmt = myConn.prepareCall("{call get_film_by_rating(?)}");

            // set the IN parameter
            myStmt.setString(1, "PG");

            // execute
            System.out.println("Calling procedure: get_film_by_rating");
            myRs = myStmt.executeQuery();

            // process the results
            while (myRs.next()) {
                System.out.println(myRs.getInt("film_id") + ", " + myRs.getString("title") + ", " + myRs.getString("rating"));
            }

            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (myRs != null) myRs.close();
                if (myStmt != null) myStmt.close();
                if (myConn != null) myConn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
