package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoReserva;
import Modelo.EnumEstadoReservaHabitacion;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.ReservaHabitaciones;
import Modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;

public class ReservaDAO {

    private final ReservaHabitacionesDAO reservaHabitacionesDAO = new ReservaHabitacionesDAO();

    public List<Reserva> listar() throws SQLException {
        finalizarReservasVencidas();

        List<Reserva> listaReservas = new ArrayList<>();
        String sql = "SELECT r.*, u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reserva reserva = mapearReserva(rs);
                adjuntarHabitaciones(reserva);
                listaReservas.add(reserva);
            }
        }

        return listaReservas;
    }

    public List<Reserva> listarPorUsuario(int idUsuario) throws SQLException {
        finalizarReservasVencidas();

        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.*, u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario "
                + "WHERE r.idUsuario = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = mapearReserva(rs);
                    adjuntarHabitaciones(reserva);
                    reservas.add(reserva);
                }
            }
        }
        return reservas;
    }

    public int finalizarReservasVencidas() throws SQLException {
        String sql = "UPDATE reserva SET estado = ? WHERE estado = ? AND checkout IS NOT NULL AND checkout <= ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, EnumEstadoReserva.FINALIZADA.name());
            ps.setString(2, EnumEstadoReserva.ACTIVA.name());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate();
        }
    }



    public Reserva buscar(int idReserva) throws SQLException {
        Reserva reserva = null;
        String sql = "SELECT r.*, u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM reserva r "
                + "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario "
                + "WHERE r.idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reserva = mapearReserva(rs);
                    adjuntarHabitaciones(reserva);
                }
            }
        }

        return reserva;
    }

    public int agregarReserva(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (checkin, checkout, fechaReserva, estado, nombreCliente, email, telefono, observaciones, idUsuario) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, reserva.getCheckin() != null ? Timestamp.valueOf(reserva.getCheckin()) : null);
            ps.setTimestamp(2, reserva.getCheckout() != null ? Timestamp.valueOf(reserva.getCheckout()) : null);
            ps.setTimestamp(3, reserva.getFechaReserva() != null ? Timestamp.valueOf(reserva.getFechaReserva()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, reserva.getEstado() != null ? reserva.getEstado().name() : null);
            ps.setString(5, reserva.getNombreCliente());
            ps.setString(6, reserva.getEmail());
            ps.setString(7, reserva.getTelefono());
            ps.setString(8, reserva.getObservaciones());
            ps.setInt(9, reserva.getUsuario().getIdUsuario());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        return -1;
    }

    public int reservaHuespd(Reserva reserva) throws SQLException {
        FacesContext context = FacesContext.getCurrentInstance();
        Usuario usuarioLogueado = (Usuario) context.getExternalContext()
                .getSessionMap().get("usuarioLogueado");

        if (usuarioLogueado == null) {
            throw new SQLException("No hay usuario logueado en la sesión.");
        }

        String sql = "INSERT INTO reserva (checkin, checkout, fechaReserva, estado, nombreCliente, email, telefono, observaciones, idUsuario) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            LocalDateTime fechaReserva = reserva.getFechaReserva() != null ? reserva.getFechaReserva() : LocalDateTime.now();

            ps.setTimestamp(1, reserva.getCheckin() != null ? Timestamp.valueOf(reserva.getCheckin()) : null);
            ps.setTimestamp(2, reserva.getCheckout() != null ? Timestamp.valueOf(reserva.getCheckout()) : null);
            ps.setTimestamp(3, Timestamp.valueOf(fechaReserva));
            ps.setString(4, "ACTIVA");
            ps.setString(5, reserva.getNombreCliente());
            ps.setString(6, reserva.getEmail());
            ps.setString(7, reserva.getTelefono());
            ps.setString(8, reserva.getObservaciones());
            ps.setInt(9, usuarioLogueado.getIdUsuario());

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
        String sql = "UPDATE reserva SET checkin = ?, checkout = ?, fechaReserva = ?, estado = ?, nombreCliente = ?, email = ?, telefono = ?, observaciones = ?, idUsuario = ? WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setTimestamp(1, reserva.getCheckin() != null ? Timestamp.valueOf(reserva.getCheckin()) : null);
            ps.setTimestamp(2, reserva.getCheckout() != null ? Timestamp.valueOf(reserva.getCheckout()) : null);
            ps.setTimestamp(3, reserva.getFechaReserva() != null ? Timestamp.valueOf(reserva.getFechaReserva()) : null);
            ps.setString(4, reserva.getEstado() != null ? reserva.getEstado().name() : null);
            ps.setString(5, reserva.getNombreCliente());
            ps.setString(6, reserva.getEmail());
            ps.setString(7, reserva.getTelefono());
            ps.setString(8, reserva.getObservaciones());
            ps.setInt(9, reserva.getUsuario().getIdUsuario());
            ps.setInt(10, reserva.getIdReserva());

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

        return reservaHabitacionesDAO.habitacionDisponible(habitacionId, checkin, checkout, reservaExcluirId);
    }

    public List<Reserva> listarOcupacionesHabitacion(int habitacionId, Integer reservaExcluirId) throws SQLException {
        List<Reserva> ocupaciones = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT r.idReserva, r.checkin, r.checkout FROM reservahabitaciones rh "
                + "INNER JOIN reserva r ON r.idReserva = rh.idReserva "
                + "WHERE rh.idHabitacion = ? AND rh.estado = ?");

        if (reservaExcluirId != null) {
            sql.append(" AND r.idReserva <> ?");
        }

        sql.append(" ORDER BY r.checkin");

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql.toString())) {
            ps.setInt(1, habitacionId);
            ps.setString(2, EnumEstadoReservaHabitacion.Activa.name());

            if (reservaExcluirId != null) {
                ps.setInt(3, reservaExcluirId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = new Reserva();
                    reserva.setIdReserva(rs.getInt("idReserva"));

                    Timestamp checkin = rs.getTimestamp("checkin");
                    if (checkin != null) {
                        reserva.setCheckin(checkin.toLocalDateTime());
                    }

                    Timestamp checkout = rs.getTimestamp("checkout");
                    if (checkout != null) {
                        reserva.setCheckout(checkout.toLocalDateTime());
                    }

                    ocupaciones.add(reserva);
                }
            }
        }

        return ocupaciones;
    }

    public void eliminar(int idReserva) throws SQLException {
        reservaHabitacionesDAO.eliminarPorReserva(idReserva);
        String sql = "DELETE FROM reserva WHERE idReserva = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    public int contarReservasActivas() throws SQLException {
        finalizarReservasVencidas();

        String sql = "SELECT COUNT(*) AS total FROM reserva WHERE estado = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, EnumEstadoReserva.ACTIVA.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
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
            reserva.setCheckout(checkout.toLocalDateTime());
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

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idUsuario"));
        usuario.setNombre(rs.getString("nombreUsuario"));
        usuario.setEmail(rs.getString("correoUsuario"));
        usuario.setTelefono(rs.getString("telefonoUsuario"));
        reserva.setUsuario(usuario);

        return reserva;
    }

    private void adjuntarHabitaciones(Reserva reserva) throws SQLException {
        if (reserva == null || reserva.getIdReserva() <= 0) {
            return;
        }

        List<ReservaHabitaciones> relaciones = reservaHabitacionesDAO.obtenerHabitacionesPorReserva(reserva.getIdReserva());
        List<Habitacion> habitaciones = new ArrayList<>();

        for (ReservaHabitaciones relacion : relaciones) {
            habitaciones.add(relacion.getHabitacion());
        }

        reserva.setHabitaciones(habitaciones);
    }

  public Map<String, Integer> obtenerReservasPorMes(LocalDate fechaInicio) throws SQLException {
    Map<String, Integer> reservasPorMes = new LinkedHashMap<>();

    String sql = 
        "SELECT DATE_FORMAT(checkin, '%Y-%m') AS mes, COUNT(*) AS total " +
        "FROM reserva " +
        "WHERE checkin IS NOT NULL " +
        "AND checkin >= ? " +
        "GROUP BY YEAR(checkin), MONTH(checkin) " +
        "ORDER BY YEAR(checkin), MONTH(checkin)";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
        ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservasPorMes.put(rs.getString("mes"), rs.getInt("total"));
            }
        }
    }

    return reservasPorMes;
}


    public Map<String, Integer> obtenerOcupacionPorMes(LocalDate fechaInicio) throws SQLException {
        Map<String, Integer> ocupacionMensual = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(checkin, '%Y-%m') AS mes, COUNT(*) AS total "
                + "FROM reserva "
                + "WHERE checkin IS NOT NULL AND checkin >= ? "
                + "GROUP BY YEAR(checkin), MONTH(checkin) "
                + "ORDER BY YEAR(checkin), MONTH(checkin)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ocupacionMensual.put(rs.getString("mes"), rs.getInt("total"));
                }
            }
        }

        return ocupacionMensual;
    }


}
