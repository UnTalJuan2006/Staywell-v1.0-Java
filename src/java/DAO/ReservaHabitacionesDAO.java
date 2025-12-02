package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoHabitacion;
import Modelo.EnumEstadoReservaHabitacion;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.ReservaHabitaciones;
import Modelo.TipoHabitacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaHabitacionesDAO {

    public void registrarRelacion(int idReserva, int idHabitacion, EnumEstadoReservaHabitacion estado) throws SQLException {
        String sql = "INSERT INTO reservahabitaciones (idReserva, idHabitacion, estado, fechaCreacion, fechaActualizacion) "
                + "VALUES (?, ?, ?, ?, ?)";

        LocalDateTime ahora = LocalDateTime.now();

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.setInt(2, idHabitacion);
            ps.setString(3, estado != null ? estado.name() : EnumEstadoReservaHabitacion.Activa.name());
            ps.setTimestamp(4, Timestamp.valueOf(ahora));
            ps.setTimestamp(5, Timestamp.valueOf(ahora));
            ps.executeUpdate();
        }
    }

    public List<ReservaHabitaciones> obtenerHabitacionesPorReserva(int idReserva) throws SQLException {
        List<ReservaHabitaciones> relaciones = new ArrayList<>();

        String sql = "SELECT rh.id, rh.estado, rh.observaciones, rh.fechaCreacion, rh.fechaActualizacion, "
                + "h.idHabitacion, h.numHabitacion, h.estado AS estadoHabitacion, "
                + "th.idTipoHabitacion, th.nombre AS nombreTipoHabitacion, th.descripcion AS descripcionTipoHabitacion, th.precio "
                + "FROM reservahabitaciones rh "
                + "INNER JOIN habitacion h ON h.idHabitacion = rh.idHabitacion "
                + "INNER JOIN tipohabitacion th ON th.idTipoHabitacion = h.idTipoHabitacion "
                + "WHERE rh.idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReservaHabitaciones relacion = new ReservaHabitaciones();
                    relacion.setId(rs.getInt("id"));
                    relacion.setEstado(EnumEstadoReservaHabitacion.valueOf(rs.getString("estado")));
                    relacion.setObservaciones(rs.getString("observaciones"));

                    Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                    if (fechaCreacion != null) {
                        relacion.setFechaCreacion(fechaCreacion.toLocalDateTime());
                    }

                    Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                    if (fechaActualizacion != null) {
                        relacion.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                    }

                    Habitacion habitacion = new Habitacion();
                    habitacion.setIdHabitacion(rs.getInt("idHabitacion"));
                    habitacion.setNumHabitacion(rs.getInt("numHabitacion"));
                    habitacion.setEstado(EnumEstadoHabitacion.valueOf(rs.getString("estadoHabitacion")));

                    TipoHabitacion tipo = new TipoHabitacion();
                    tipo.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                    tipo.setNombre(rs.getString("nombreTipoHabitacion"));
                    tipo.setDescripcion(rs.getString("descripcionTipoHabitacion"));
                    tipo.setPrecio(rs.getFloat("precio"));
                    habitacion.setTipoHabitacion(tipo);

                    relacion.setHabitacion(habitacion);
                    relaciones.add(relacion);
                }
            }
        }

        return relaciones;
    }

    public void finalizarPorReserva(int idReserva) throws SQLException {
        String sql = "UPDATE reservahabitaciones SET estado = ?, fechaActualizacion = ? WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, EnumEstadoReservaHabitacion.Finalizada.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idReserva);
            ps.executeUpdate();
        }
    }

    public void eliminarPorReserva(int idReserva) throws SQLException {
        String sql = "DELETE FROM reservahabitaciones WHERE idReserva = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    public boolean habitacionDisponible(int habitacionId, LocalDateTime checkin, LocalDateTime checkout, Integer reservaExcluirId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM reservahabitaciones rh "
                        + "INNER JOIN reserva r ON r.idReserva = rh.idReserva "
                        + "WHERE rh.idHabitacion = ? "
                        + "AND rh.estado = ? "
                        + "AND (? < COALESCE(r.checkout, ?)) "
                        + "AND (? > COALESCE(r.checkin, ?))");

        if (reservaExcluirId != null) {
            sql.append(" AND r.idReserva <> ?");
        }

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql.toString())) {
            Timestamp checkinTs = Timestamp.valueOf(checkin);
            Timestamp checkoutTs = Timestamp.valueOf(checkout);

            ps.setInt(1, habitacionId);
            ps.setString(2, EnumEstadoReservaHabitacion.Activa.name());
            ps.setTimestamp(3, checkinTs);
            ps.setTimestamp(4, checkoutTs);
            ps.setTimestamp(5, checkoutTs);
            ps.setTimestamp(6, checkinTs);

            if (reservaExcluirId != null) {
                ps.setInt(7, reservaExcluirId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }

        return true;
    }
}
