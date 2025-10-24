
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
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    PreparedStatement ps;
    ResultSet rs;

    public List<Reserva> listar() throws SQLException {
        List<Reserva> listaReservas = new ArrayList<>();
        String sql = "SELECT r.*, h.numHabitacion AS numeroHabitacion, u.nombre AS nombreUsuario, u.email AS correoUsuario, "
                + "u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN habitacion h ON r.idHabitacion = h.idHabitacion "
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
        String sql = "SELECT r.*, h.numHabitacion AS numeroHabitacion, u.nombre AS nombreUsuario, u.email AS correoUsuario, "
                + "u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN habitacion h ON r.idHabitacion = h.idHabitacion "
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

    public void agregarReserva(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (checkin, checkout, fechaReserva, estado, nombreCliente, email, telefono, observaciones, idHabitacion, idUsuario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

            ps.executeUpdate();
        }
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
