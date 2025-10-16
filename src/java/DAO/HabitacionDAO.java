package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoHabitacion;
import Modelo.EnumTipoHabitacion;
import Modelo.Habitacion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDAO {
    PreparedStatement ps;
    ResultSet rs;

    
    public List<Habitacion> listar() throws SQLException {
        List<Habitacion> listaHabitaciones = new ArrayList<>();

        try {
            String sql = "SELECT * FROM habitacion";
            ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Habitacion h = new Habitacion();
                h.setIdHabitacion(rs.getInt("idHabitacion"));
                h.setNumHabitacion(rs.getInt("numHabitacion"));
                h.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));
                h.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                h.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                h.setTipoHabitacion(EnumTipoHabitacion.valueOf(rs.getString("tipoHabitacion")));

                listaHabitaciones.add(h);
            }
        } catch (SQLException e) {
            System.out.println(" Error al listar habitaciones: " + e.getMessage());
        }
        return listaHabitaciones;
    }


    public void agregar(Habitacion h) throws SQLException {
        try {
            String sql = "INSERT INTO habitacion (numHabitacion, estado, fechaCreacion, fechaActualizacion, tipoHabitacion) " +
                         "VALUES (?, ?, ?, ?, ?)";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, h.getNumHabitacion());
            ps.setString(2, h.getEstado().name());
            ps.setTimestamp(3, Timestamp.valueOf(h.getFechaCreacion()));
            ps.setTimestamp(4, Timestamp.valueOf(h.getFechaActualizacion()));
            ps.setString(5, h.getTipoHabitacion().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println(" Error al registrar habitación: " + e.getMessage());
        }
    }

  
    public void actualizar(Habitacion h) {
        try {
            String sql = "UPDATE habitacion SET numHabitacion=?, estado=?, fechaActualizacion=?, tipoHabitacion=? WHERE idHabitacion=?";
            ps = Conexion.conectar().prepareStatement(sql);

            ps.setInt(1, h.getNumHabitacion());
            ps.setString(2, h.getEstado().name());
            ps.setTimestamp(3, Timestamp.valueOf(h.getFechaActualizacion()));
            ps.setString(4, h.getTipoHabitacion().name());
            ps.setInt(5, h.getIdHabitacion());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Error al actualizar habitación: " + e.getMessage());
        }
    }

    
    public void eliminar(Habitacion h) {
        try {
            String sql = "DELETE FROM habitacion WHERE idHabitacion = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, h.getIdHabitacion());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar habitación: " + e.getMessage());
        }
    }


    public Habitacion buscar(int id) {
        Habitacion h = null;
        try {
            String sql = "SELECT * FROM habitacion WHERE idHabitacion = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                h = new Habitacion();
                h.setIdHabitacion(rs.getInt("idHabitacion"));
                h.setNumHabitacion(rs.getInt("numHabitacion"));
                h.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));
                h.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                h.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                h.setTipoHabitacion(EnumTipoHabitacion.valueOf(rs.getString("tipoHabitacion")));
            }

        } catch (SQLException e) {
            System.out.println(" Error al buscar habitación: " + e.getMessage());
        }
        return h;
    }
}
