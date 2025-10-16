package DAO;

import Controlador.Conexion;
import Modelo.Evento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EventoDAO {
    PreparedStatement ps;
    ResultSet rs;

    public List<Evento> listar() throws SQLException {
        List<Evento> listaEventos = new ArrayList<>();

        try {
            String sql = "SELECT * FROM evento";
             ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Evento v = new Evento();
                v.setIdEvento(rs.getInt("idEvento"));
                v.setNombreEvento(rs.getString("nombreEvento"));
                v.setDescripcion(rs.getString("descripcion"));
                v.setFechaEvento(rs.getDate("fechaEvento"));
                v.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                v.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                v.setHoraInicio(rs.getTime("horaInicio"));
                v.setHoraFin(rs.getTime("horaFin"));

                v.setNombreCliente(rs.getString("nombreCliente"));

                listaEventos.add(v);
            }
        } catch (SQLException e) {
            System.out.println(" Error al listar eventos: " + e.getMessage());
        }
        return listaEventos;
    }

    public void agregar(Evento v) throws SQLException {
        String sql = "INSERT INTO evento(nombreEvento, fechaEvento, descripcion, fechaCreacion, fechaActualizacion, horaInicio, horaFin, nombreCliente) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            ps = Conexion.conectar().prepareStatement(sql);

            ps.setString(1, v.getNombreEvento());

           
            java.util.Date fecha = v.getFechaEvento();
            if (fecha != null) {
                ps.setDate(2, new java.sql.Date(fecha.getTime()));
            } else {
                ps.setDate(2, null);
            }

            ps.setString(3, v.getDescripcion());
            ps.setTimestamp(4, Timestamp.valueOf(v.getFechaCreacion()));
            ps.setTimestamp(5, Timestamp.valueOf(v.getFechaActualizacion()));

            
            if (v.getHoraInicio() != null) {
                ps.setTime(6, new Time(v.getHoraInicio().getTime()));
            } else {
                ps.setTime(6, null);
            }

            if (v.getHoraFin() != null) {
                ps.setTime(7, new Time(v.getHoraFin().getTime()));
            } else {
                ps.setTime(7, null);
            }

            ps.setString(8, v.getNombreCliente());

            ps.executeUpdate();
            System.out.println(" Evento registrado correctamente");
        } catch (SQLException e) {
            System.out.println(" Error al registrar evento: " + e.getMessage());
            throw e;
        }
    }

    public void actualizar(Evento v) {
        try {
            String sql = "UPDATE evento SET nombreEvento = ?, fechaEvento = ?, descripcion = ?, "
                       + "fechaActualizacion = ?, horaInicio = ?, horaFin = ?, nombreCliente = ? "
                       + "WHERE idEvento = ?";

            ps = Conexion.conectar().prepareStatement(sql);

            ps.setString(1, v.getNombreEvento());

           
            java.util.Date fecha = v.getFechaEvento();
            if (fecha != null) {
                ps.setDate(2, new java.sql.Date(fecha.getTime()));
            } else {
                ps.setDate(2, null);
            }

            ps.setString(3, v.getDescripcion());
            ps.setTimestamp(4, Timestamp.valueOf(v.getFechaActualizacion()));

           
            if (v.getHoraInicio() != null) {
                ps.setTime(5, new Time(v.getHoraInicio().getTime()));
            } else {
                ps.setTime(5, null);
            }

            if (v.getHoraFin() != null) {
                ps.setTime(6, new Time(v.getHoraFin().getTime()));
            } else {
                ps.setTime(6, null);
            }

            ps.setString(7, v.getNombreCliente());
            ps.setInt(8, v.getIdEvento());

            ps.executeUpdate();
            System.out.println(" Evento actualizado correctamente");
        } catch (SQLException e) {
            System.out.println(" Error al actualizar evento: " + e.getMessage());
        }
    }

    public void eliminar(Evento v) {
        try {
            String sql = "DELETE FROM evento WHERE idEvento = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, v.getIdEvento());
            ps.executeUpdate();
            System.out.println("Evento eliminado correctamente");
        } catch (SQLException e) {
            System.out.println(" Error al eliminar evento: " + e.getMessage());
        }
    }

    public Evento buscar(int id) {
        Evento v = null;
        try {
            String sql = "SELECT * FROM evento WHERE idEvento = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                v = new Evento();
                v.setIdEvento(rs.getInt("idEvento"));
                v.setNombreEvento(rs.getString("nombreEvento"));
                v.setDescripcion(rs.getString("descripcion"));
                v.setFechaEvento(rs.getDate("fechaEvento")); 
                v.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                v.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                v.setHoraInicio(rs.getTime("horaInicio"));
                v.setHoraFin(rs.getTime("horaFin"));

                v.setNombreCliente(rs.getString("nombreCliente"));
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar evento: " + e.getMessage());
        }
        return v;
    }
}
