
package db;
import java.sql.Connection;
import java.sql.DriverManager;
public class Conexion {
    private static final String URL  = "jdbc:mysql://localhost:3306/atm_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root2025"; 
    public static Connection get() throws Exception { return DriverManager.getConnection(URL, USER, PASS); }
}
