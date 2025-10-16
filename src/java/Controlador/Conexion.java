package Controlador;

import com.mysql.cj.jdbc.Driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    public static Connection  conectar(){
        Connection conn = null;
        
        try {
            Driver drv = new Driver(); 
            DriverManager.registerDriver(drv);
            
            String cad = "jdbc:mysql://localhost:3306/staywell?user=root&useSSL=false";
            conn = DriverManager.getConnection(cad);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return conn;
    }

    public static Conexion getConexion() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
