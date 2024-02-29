/*
 * Run this file to reset the tables int columns to zeros(0)
 */
import java.sql.*;

public class ResetTable {
    public static void main(String[] args) {
        String driver = "com.mysql.cj.jdbc.Driver";
        String db_url = "jdbc:mysql://localhost:3306/leaguetable";
        String username = "root";
        String password = "";

        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            int k = stmt.executeUpdate("update epl set p=0,w=0,d=0,l=0,gf=0,ga=0,gd=0,points=0");    //set count value to zero(0) for all rows after a class session
            if(k!=1) {
                System.out.println("reset done!!!!");
            } else {
                System.out.println("reset not done!!!!");
            }

            stmt.close();
			conn.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
