
package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoReserva;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.Usuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    PreparedStatement ps;
    ResultSet rs;

    public List<Reserva> listar() throws SQLException {
        List<Reserva> listaReservas = new ArrayList<>();
        String sql = "SELECT r.*, h.numHabitacion AS numeroHabitacion, h.idTipoHabitacion AS habitacionTipoId, "
                + "th.nombre AS nombreTipoHabitacion, th.descripcion AS descripcionTipoHabitacion, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN habitacion h ON r.idHabitacion = h.idHabitacion "
                + "LEFT JOIN tipohabitacion th ON h.idTipoHabitacion = th.idTipoHabitacion "
                + "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaReservas.add(mapearReserva(rs));
            }
        }

        return listaReservas;
    }

    public Reserva buscar(int idReserva) throws SQLException {
        Reserva reserva = null;
        String sql = "SELECT r.*, h.numHabitacion AS numeroHabitacion, h.idTipoHabitacion AS habitacionTipoId, "
                + "th.nombre AS nombreTipoHabitacion, th.descripcion AS descripcionTipoHabitacion, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN habitacion h ON r.idHabitacion = h.idHabitacion "
                + "LEFT JOIN tipohabitacion th ON h.idTipoHabitacion = th.idTipoHabitacion "
                + "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario "
                + "WHERE r.idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reserva = mapearReserva(rs);
                }
            }
        }

        return reserva;
    }

    public int agregarReserva(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (checkin, checkout, fechaReserva, estado, nombreCliente, email, telefono, observaciones, idHabitacion, idUsuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, reserva.getCheckin() != null ? Timestamp.valueOf(reserva.getCheckin()) : null);
            ps.setTimestamp(2, reserva.getCehckout() != null ? Timestamp.valueOf(reserva.getCehckout()) : null);
            ps.setTimestamp(3, reserva.getFechaReserva() != null ? Timestamp.valueOf(reserva.getFechaReserva()) : null);
            ps.setString(4, reserva.getEstado() != null ? reserva.getEstado().name() : null);
            ps.setString(5, reserva.getNombreCliente());
            ps.setString(6, reserva.getEmail());
            ps.setString(7, reserva.getTelefono());
            ps.setString(8, reserva.getObservaciones());
            ps.setInt(9, reserva.getHabitacion().getIdHabitacion());
            ps.setInt(10, reserva.getUsuario().getIdUsuario());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        return -1;
    }

    public void actualizar(Reserva reserva) throws SQLException {
        String sql = "UPDATE reserva SET checkin = ?, checkout = ?, fechaReserva = ?, estado = ?, nombreCliente = ?, email = ?, telefono = ?, observaciones = ?, idHabitacion = ?, idUsuario = ? WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setTimestamp(1, reserva.getCheckin() != null ? Timestamp.valueOf(reserva.getCheckin()) : null);
            ps.setTimestamp(2, reserva.getCehckout() != null ? Timestamp.valueOf(reserva.getCehckout()) : null);
            ps.setTimestamp(3, reserva.getFechaReserva() != null ? Timestamp.valueOf(reserva.getFechaReserva()) : null);
            ps.setString(4, reserva.getEstado() != null ? reserva.getEstado().name() : null);
            ps.setString(5, reserva.getNombreCliente());
            ps.setString(6, reserva.getEmail());
            ps.setString(7, reserva.getTelefono());
            ps.setString(8, reserva.getObservaciones());
            ps.setInt(9, reserva.getHabitacion().getIdHabitacion());
            ps.setInt(10, reserva.getUsuario().getIdUsuario());
            ps.setInt(11, reserva.getIdReserva());

            ps.executeUpdate();
        }
    }

    public void actualizarFechas(int idReserva, java.time.LocalDateTime checkin, java.time.LocalDateTime checkout) throws SQLException {
        String sql = "UPDATE reserva SET checkin = ?, checkout = ? WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setTimestamp(1, checkin != null ? Timestamp.valueOf(checkin) : null);
            ps.setTimestamp(2, checkout != null ? Timestamp.valueOf(checkout) : null);
            ps.setInt(3, idReserva);
            ps.executeUpdate();
        }
    }

    public boolean habitacionDisponible(int habitacionId, LocalDateTime checkin, LocalDateTime checkout, Integer reservaExcluirId) throws SQLException {
        if (checkin == null || checkout == null) {
            throw new IllegalArgumentException("Las fechas de verificación no pueden ser nulas");
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM reserva WHERE idHabitacion = ? "
                + "AND (? < COALESCE(checkout, ?)) "
                + "AND (? > COALESCE(checkin, ?))");

        if (reservaExcluirId != null) {
            sql.append(" AND idReserva <> ?");
        }

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql.toString())) {
            Timestamp checkinTs = Timestamp.valueOf(checkin);
            Timestamp checkoutTs = Timestamp.valueOf(checkout);

            ps.setInt(1, habitacionId);
            ps.setTimestamp(2, checkinTs);
            ps.setTimestamp(3, checkoutTs);
            ps.setTimestamp(4, checkoutTs);
            ps.setTimestamp(5, checkinTs);

            if (reservaExcluirId != null) {
                ps.setInt(6, reservaExcluirId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }

        return true;
    }

    public void eliminar(int idReserva) throws SQLException {
        String sql = "DELETE FROM reserva WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();

        reserva.setIdReserva(rs.getInt("idReserva"));

        Timestamp checkin = rs.getTimestamp("checkin");
        if (checkin != null) {
            reserva.setCheckin(checkin.toLocalDateTime());
        }

        Timestamp checkout = rs.getTimestamp("checkout");
        if (checkout != null) {
            reserva.setCehckout(checkout.toLocalDateTime());
        }

        Timestamp fechaReserva = rs.getTimestamp("fechaReserva");
        if (fechaReserva != null) {
            reserva.setFechaReserva(fechaReserva.toLocalDateTime());
        }

        String estado = rs.getString("estado");
        if (estado != null) {
            reserva.setEstado(EnumEstadoReserva.valueOf(estado));
        }

        reserva.setNombreCliente(rs.getString("nombreCliente"));
        reserva.setEmail(rs.getString("email"));
        reserva.setTelefono(rs.getString("telefono"));
        reserva.setObservaciones(rs.getString("observaciones"));

        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(rs.getInt("idHabitacion"));
        habitacion.setNumHabitacion(rs.getInt("numeroHabitacion"));

        if (rs.getObject("habitacionTipoId") != null) {
            Modelo.TipoHabitacion tipoHabitacion = new Modelo.TipoHabitacion();
            tipoHabitacion.setIdTipoHabitacion(rs.getInt("habitacionTipoId"));
            tipoHabitacion.setNombre(rs.getString("nombreTipoHabitacion"));
            tipoHabitacion.setDescripcion(rs.getString("descripcionTipoHabitacion"));
            habitacion.setTipoHabitacion(tipoHabitacion);
        }
        reserva.setHabitacion(habitacion);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idUsuario"));
        usuario.setNombre(rs.getString("nombreUsuario"));
        usuario.setEmail(rs.getString("correoUsuario"));
        usuario.setTelefono(rs.getString("telefonoUsuario"));
        reserva.setUsuario(usuario);

        return reserva;
    }
}
