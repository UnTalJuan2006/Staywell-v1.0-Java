
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

    public List<TipoHabitacion> listar() throws SQLException {
        List<TipoHabitacion> listaTipoHabitaciones = new ArrayList<>();
        String sql = "SELECT * FROM tipohabitacion";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                listaTipoHabitaciones.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar Tipos: " + e.getMessage());
            throw e;
        }

        return listaTipoHabitaciones;
    }
    
public void agregar(TipoHabitacion t) throws SQLException {
    String sql = "INSERT INTO tipohabitacion(nombre, descripcion, capacidad, precio, imagen) VALUES(?,?,?,?,?)";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql) ){

        ps.setString(1, t.getNombre());
        ps.setString(2, t.getDescripcion());
        ps.setInt(3, t.getCapacidad());
        ps.setFloat(4, t.getPrecio());
        ps.setString(5, t.getImagen());

        int filas = ps.executeUpdate();
        if(filas == 0){
            throw new SQLException("No se pudo insertar el tipo de habitación");
        }

    } catch (SQLException e) {
        System.out.println("Error al registrar tipo: " + e.getMessage());
        throw e; 
    }
}
    
    public TipoHabitacion buscar(int idTipoHabitacion) throws SQLException {
        TipoHabitacion t = null;
        String sql = "SELECT * FROM tipohabitacion WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idTipoHabitacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = new TipoHabitacion();
                    t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                    t.setNombre(rs.getString("nombre"));
                    t.setDescripcion(rs.getString("descripcion"));
                    t.setCapacidad(rs.getInt("capacidad"));
                    t.setPrecio(rs.getFloat("precio"));
                    t.setImagen(rs.getString("imagen"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar TipoHabitacion: " + e.getMessage());
            throw e;
        }

        return t;
    }
    
}
