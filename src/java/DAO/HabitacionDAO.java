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

    public List<Habitacion> listar() throws SQLException {
        List<Habitacion> listaHabitaciones = new ArrayList<>();
        String sql = "SELECT "
                + "h.idHabitacion, h.numHabitacion, h.estado, h.fechaCreacion, h.fechaActualizacion, h.idTipoHabitacion, "
                + "th.idTipoHabitacion AS tipoId, th.nombre AS tipoNombre, th.descripcion AS tipoDescripcion, "
                + "th.capacidad AS tipoCapacidad, th.precio AS tipoPrecio "
                + "FROM habitacion h "
                + "INNER JOIN tipohabitacion th ON th.idTipoHabitacion = h.idTipoHabitacion "
                + "ORDER BY h.numHabitacion";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Habitacion h = new Habitacion();
                h.setIdHabitacion(rs.getInt("idHabitacion"));
                h.setNumHabitacion(rs.getInt("numHabitacion"));
                h.setNombreTipoHabitacion(rs.getString("tipoNombre"));
                h.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estado")));

                TipoHabitacion tipo = new TipoHabitacion();
                tipo.setIdTipoHabitacion(rs.getInt("tipoId"));
                tipo.setNombre(rs.getString("tipoNombre"));
                tipo.setDescripcion(rs.getString("tipoDescripcion"));
                tipo.setCapacidad(rs.getInt("tipoCapacidad"));
                tipo.setPrecio(rs.getFloat("tipoPrecio"));
                h.setTipoHabitacion(tipo);

                // Manejo seguro de timestamps que pueden ser NULL
                java.sql.Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                if (fechaCreacion != null) {
                    h.setFechaCreacion(fechaCreacion.toLocalDateTime());
                }

                java.sql.Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                if (fechaActualizacion != null) {
                    h.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                }

                listaHabitaciones.add(h);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar habitaciones: " + e.getMessage());
            throw e;
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
                    habitacion.setTipoHabitacion(tipoDAO.buscarPorId(idTipoHabitacion));
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
                    habitacion.setTipoHabitacion(tipoDAO.buscarPorId(rs.getInt("idTipoHabitacion")));
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
 
 
 public void actualizar(Habitacion h) throws SQLException {
     String sql = "UPDATE habitacion SET numHabitacion = ?, estado = ?, fechaActualizacion = ?, idTipoHabitacion = ? WHERE idHabitacion = ?";

      try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
          ps.setInt(1, h.getNumHabitacion());
          ps.setString(2, h.getEstado() != null ? h.getEstado().name() : null);
          ps.setTimestamp(3, Timestamp.valueOf(h.getFechaActualizacion()));
          ps.setInt(4, h.getTipoHabitacion().getIdTipoHabitacion());
          ps.setInt(5, h.getIdHabitacion());

          ps.executeUpdate();
      }
 }
 
 
    public void eliminar(Habitacion h) throws SQLException {
        String sql = "DELETE FROM habitacion WHERE idHabitacion = ?";

        try(PreparedStatement ps = Conexion.conectar().prepareStatement(sql)){
            ps.setInt(1, h.getIdHabitacion());
            ps.executeUpdate();
            
        }catch(SQLException e){
            System.out.println("Error al eliminar habitacion");
            throw e;
        }
    }
    
    public List<Habitacion> buscar(String filtro) throws SQLException {

    List<Habitacion> lista = new ArrayList<>();

    String sql = "SELECT h.*, t.nombreTipoHabitacion "
               + "FROM habitacion h "
               + "JOIN tipohabitacion t ON h.idTipoHabitacion = t.idTipoHabitacion "
               + "WHERE h.numHabitacion LIKE ? OR t.nombreTipoHabitacion LIKE ? "
               + "ORDER BY h.numHabitacion ASC";

     PreparedStatement ps = Conexion.conectar().prepareStatement(sql);

    ps.setString(1, "%" + filtro + "%");
    ps.setString(2, "%" + filtro + "%");

    ResultSet rs = ps.executeQuery();

    while (rs.next()) {

        Habitacion h = new Habitacion();
        h.setIdHabitacion(rs.getInt("idHabitacion"));
        h.setNumHabitacion(rs.getInt("numHabitacion"));
        h.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estadoHabitacion")));
        h.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
        h.setNombreTipoHabitacion(rs.getString("nombreTipoHabitacion"));

        lista.add(h);
    }

    rs.close();
    ps.close();
    

    return lista;
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