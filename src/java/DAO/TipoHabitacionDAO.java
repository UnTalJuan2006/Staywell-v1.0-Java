
package DAO;

import Controlador.Conexion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import Modelo.TipoHabitacion;


public class TipoHabitacionDAO {
    PreparedStatement ps;
    ResultSet rs;
    
    public List<TipoHabitacion> listar()throws SQLException {
        List<TipoHabitacion> listaTipoHabitaciones = new ArrayList<>();
        
        try{
            String sql = "Select * from tipohabitacion";
            ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();
            
            while(rs.next()){
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                listaTipoHabitaciones.add(t);
                
                
            }
        }catch(SQLException e){
            System.out.println("Error al listar Tipos" + e.getMessage());
        }
        return listaTipoHabitaciones;
    }
    
    public void agregar(TipoHabitacion t) throws SQLException {
        try{
            String sql = "INSERT INTO tipohabitacion(nombre, descripcion, capacidad, precio, imagen )" +
                    "VALUES(?,?,?,?,?)";
            
            ps= Conexion.conectar().prepareStatement(sql);
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getDescripcion());
            ps.setInt(3, t.getCapacidad());
            ps.setFloat(4, t.getPrecio());
            ps.setString(5, t.getImagen());
            
            ps.executeUpdate();
            
        }catch(SQLException e){
            System.out.println("Error al registrar tipo" + e.getMessage());
        }
    }
    public TipoHabitacion buscar(int idTipoHabitacion) throws SQLException {
    TipoHabitacion t = null;
    try {
        String sql = "SELECT * FROM tipohabitacion WHERE idTipoHabitacion = ?";
        ps = Conexion.conectar().prepareStatement(sql);
        ps.setInt(1, idTipoHabitacion);
        rs = ps.executeQuery();

        if (rs.next()) {
            t = new TipoHabitacion();
            t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
            t.setNombre(rs.getString("nombre"));
            t.setDescripcion(rs.getString("descripcion"));
            t.setCapacidad(rs.getInt("capacidad"));
            t.setPrecio(rs.getFloat("precio"));
            t.setImagen(rs.getString("imagen"));
        }
    } catch (SQLException e) {
        System.out.println("Error al buscar TipoHabitacion: " + e.getMessage());
    }
    return t;
}
    
}
