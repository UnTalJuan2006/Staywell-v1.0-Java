package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoHabitacion;
import Modelo.Habitacion;
import Modelo.TipoHabitacion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


public class HabitacionDAO {
    PreparedStatement ps;
    ResultSet rs;

    
    public List<Habitacion> listar() throws SQLException {
    List<Habitacion> listaHabitaciones = new ArrayList<>();
        TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();

        try {
            String sql = "SELECT * FROM habitacion";
            ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Habitacion h = new Habitacion();
                h.setIdHabitacion(rs.getInt("idHabitacion"));
                h.setNumHabitacion(rs.getInt("numHabitacion"));
                h.setTipoHabitacion(tipoDAO.buscar(rs.getInt("idTipoHabitacion")));
                h.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));
                h.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                h.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                listaHabitaciones.add(h);
            }
        } catch (SQLException e) {
            System.out.println(" Error al listar habitaciones: " + e.getMessage());
        }
        return listaHabitaciones;
    }

    public List<Habitacion> listarPorTipo(int idTipoHabitacion) throws SQLException {
        List<Habitacion> listaHabitaciones = new ArrayList<>();
        TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();

        String sql = "SELECT * FROM habitacion WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idTipoHabitacion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Habitacion habitacion = new Habitacion();
                    habitacion.setIdHabitacion(rs.getInt("idHabitacion"));
                    habitacion.setNumHabitacion(rs.getInt("numHabitacion"));
                    habitacion.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));
                    habitacion.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                    habitacion.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                    habitacion.setTipoHabitacion(tipoDAO.buscar(idTipoHabitacion));
                    listaHabitaciones.add(habitacion);
                }
            }
        }

        return listaHabitaciones;
    }

    public Habitacion buscarPorId(int idHabitacion) throws SQLException {
        Habitacion habitacion = null;
        String sql = "SELECT * FROM habitacion WHERE idHabitacion = ?";
        TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idHabitacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    habitacion = new Habitacion();
                    habitacion.setIdHabitacion(rs.getInt("idHabitacion"));
                    habitacion.setNumHabitacion(rs.getInt("numHabitacion"));
                    habitacion.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));
                    habitacion.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                    habitacion.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                    habitacion.setTipoHabitacion(tipoDAO.buscar(rs.getInt("idTipoHabitacion")));
                }
            }
        }

        return habitacion;
    }

 public int agregar(Habitacion h) throws SQLException {
    String sql = "INSERT INTO habitacion (numHabitacion, estado, fechaCreacion, fechaActualizacion, idTipoHabitacion) "
               + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
        ps.setInt(1, h.getNumHabitacion());
        ps.setString(2, "Disponible");
        ps.setTimestamp(3, Timestamp.valueOf(h.getFechaCreacion()));
        ps.setTimestamp(4, Timestamp.valueOf(h.getFechaActualizacion()));
        ps.setInt(5, h.getTipoHabitacion().getIdTipoHabitacion());
        ps.executeUpdate();
    
       try(ResultSet generatedKeys = ps.getGeneratedKeys()){
           if(generatedKeys.next()){
               return generatedKeys.getInt(1);
           }
       }
    }
    return -1;
}
 
 
 public void actualizar (Habitacion h) throws SQLException {
     String sql = "UPDATE habitacion SET numHabitacion = ? , estado= ?, fechaActualizacion = ?, idTipoHabitacion = ?, where idHabitacion = ?";
     
      try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
          ps.setInt(1, h.getIdHabitacion());
          ps.setString(2, h.getEstado() != null ? h.getEstado().name() : null);
          ps.setTimestamp(3, Timestamp.valueOf(h.getFechaActualizacion()));
          ps.setInt(4 , h.getTipoHabitacion().getIdTipoHabitacion());
          ps.setInt(5, h.getIdHabitacion());
          
          ps.executeUpdate();    
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
    
    
    
        public int contarPorTipo(int idTipoHabitacion) throws SQLException {
        int total = 0;
        String sql = "SELECT COUNT(*) AS total FROM habitacion WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idTipoHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al contar habitaciones tipo " + idTipoHabitacion + ": " + e.getMessage());
            throw e;
        }
        return total;
    }
}