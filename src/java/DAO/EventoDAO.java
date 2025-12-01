package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoEspacio;
import Modelo.Evento;
import Modelo.Espacio;
import Modelo.Usuario;
import Modelo.EnumEstadoEvento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;

public class EventoDAO {

    private static final String ESTADO_ACTIVO = EnumEstadoEvento.Activa.name();
    private static final String ESTADO_FINALIZADO = EnumEstadoEvento.Finalizado.name();
    private static final String SQL_FINALIZAR_EVENTOS = "UPDATE evento "
            + "SET estado = ?, fechaActualizacion = NOW() "
            + "WHERE estado = ? "
            + "AND fechaEvento IS NOT NULL "
            + "AND horaFin IS NOT NULL "
            + "AND TIMESTAMP(fechaEvento, horaFin) <= NOW()";

    public List<Evento> listar() throws SQLException {

        List<Evento> listaEventos = new ArrayList<>();

        String sql = "SELECT e.*, "
                + "es.nombre AS nombreEspacio, es.descripcion AS descripcionEspacio, es.capacidad AS capacidadEspacio, "
                + "es.costoHora AS costoHoraEspacio, es.estado AS estadoEspacio, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM evento e "
                + "LEFT JOIN espacio es ON e.idEspacio = es.idEspacio "
                + "LEFT JOIN usuario u ON e.idUsuario = u.idUsuario";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    listaEventos.add(mapearEvento(rs));
                }
            }
        }

        return listaEventos;
    }

    public List<Evento> listarActivos() throws SQLException {

        List<Evento> eventosActivos = new ArrayList<>();

        String sql = "SELECT e.*, "
                + "es.nombre AS nombreEspacio, es.descripcion AS descripcionEspacio, es.capacidad AS capacidadEspacio, "
                + "es.costoHora AS costoHoraEspacio, es.estado AS estadoEspacio, es.imagen AS imagenEspacio, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM evento e "
                + "LEFT JOIN espacio es ON e.idEspacio = es.idEspacio "
                + "LEFT JOIN usuario u ON e.idUsuario = u.idUsuario "
                + "WHERE e.estado = 'Activa' "
                + "ORDER BY e.fechaEvento ASC, e.horaInicio ASC";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventosActivos.add(mapearEvento(rs));
                }
            }
        }

        return eventosActivos;
    }

    public List<Evento> listarPorUsuario(int idUsuario) throws SQLException {

        List<Evento> eventos = new ArrayList<>();

        String sql = "SELECT e.*, "
                + "es.nombre AS nombreEspacio, es.tipo AS tipoEspacio, "
                + "es.descripcion AS descripcionEspacio, es.capacidad AS capacidadEspacio, "
                + "es.costoHora AS costoHoraEspacio, es.fechaActualizacion AS fechaActualizacionEspacio, "
                + "es.estado AS estadoEspacio, es.imagen AS imagenEspacio, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM evento e "
                + "LEFT JOIN espacio es ON e.idEspacio = es.idEspacio "
                + "LEFT JOIN usuario u ON e.idUsuario = u.idUsuario "
                + "WHERE e.idUsuario = ?";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Evento evento = new Evento();

                        evento.setIdEvento(rs.getInt("idEvento"));
                        evento.setNombreEvento(rs.getString("nombreEvento"));
                        evento.setDescripcion(rs.getString("descripcion"));

                        Date fechaEvento = rs.getDate("fechaEvento");
                        if (fechaEvento != null) {
                            evento.setFechaEvento(fechaEvento);
                        }

                        Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                        if (fechaCreacion != null) {
                            evento.setFechaCreacion(fechaCreacion.toLocalDateTime());
                        }

                        Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                        if (fechaActualizacion != null) {
                            evento.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                        }

                        // Horas
                        Time horaInicio = rs.getTime("horaInicio");
                        if (horaInicio != null) {
                            evento.setHoraInicio(horaInicio.toLocalTime());
                        }

                        Time horaFin = rs.getTime("horaFin");
                        if (horaFin != null) {
                            evento.setHoraFin(horaFin.toLocalTime());
                        }

                        // Cliente
                        evento.setNombreCliente(rs.getString("nombreCliente"));

                        // Estado del evento
                        String estado = rs.getString("estado");
                        if (estado != null) {
                            evento.setEstado(EnumEstadoEvento.valueOf(estado));
                        }

                        // Espacio relacionado
                        Espacio espacio = new Espacio();
                        espacio.setIdEspacio(rs.getInt("idEspacio"));
                        espacio.setNombre(rs.getString("nombreEspacio"));
                        espacio.setDescripcion(rs.getString("descripcionEspacio"));
                        espacio.setCapacidad(rs.getInt("capacidadEspacio"));
                        float costoHora = rs.getFloat("costoHoraEspacio");
                        if (!rs.wasNull()) {
                            espacio.setCostoHora(costoHora);
                        }
                        espacio.setImagen(rs.getString("imagenEspacio"));
                        String estadoEspacio = rs.getString("estadoEspacio");
                        if (estadoEspacio != null) {
                            espacio.setEstado(EnumEstadoEspacio.valueOf(estadoEspacio));
                        }
                        evento.setEspacio(espacio);

                        Usuario usuario = new Usuario();
                        usuario.setIdUsuario(rs.getInt("idUsuario"));
                        usuario.setNombre(rs.getString("nombreUsuario"));
                        usuario.setEmail(rs.getString("correoUsuario"));
                        usuario.setTelefono(rs.getString("telefonoUsuario"));
                        evento.setUsuario(usuario);

                        eventos.add(evento);
                    }
                }
            }
        }

        return eventos;
    }

    public Evento buscar(int idEvento) throws SQLException {

        Evento evento = null;

        String sql = "SELECT e.*, "
                + "es.nombre AS nombreEspacio, es.descripcion AS descripcionEspacio, es.capacidad AS capacidadEspacio, "
                + "es.costoHora AS costoHoraEspacio, es.imagen AS imagenEspacio, es.estado AS estadoEspacio, "
                + "u.nombre AS nombreUsuario, u.email AS correoUsuario, u.telefono AS telefonoUsuario "
                + "FROM evento e "
                + "LEFT JOIN espacio es ON e.idEspacio = es.idEspacio "
                + "LEFT JOIN usuario u ON e.idUsuario = u.idUsuario "
                + "WHERE e.idEvento = ?";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEvento);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        evento = mapearEvento(rs);
                    }
                }
            }
        }

        return evento;
    }

    public int agregarEvento(Evento evento) throws SQLException {
        String sql = "INSERT INTO evento(nombreEvento, descripcion, fechaEvento, fechaActualizacion, fechaCreacion, horaInicio, horaFin, nombreCliente, idEspacio, idUsuario, estado)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, evento.getNombreEvento());
                ps.setString(2, evento.getDescripcion());
                ps.setDate(3, evento.getFechaEvento() == null ? null : new java.sql.Date(evento.getFechaEvento().getTime()));
                ps.setTimestamp(4, evento.getFechaActualizacion() == null ? null : java.sql.Timestamp.valueOf(evento.getFechaActualizacion()));
                ps.setTimestamp(5, evento.getFechaCreacion() == null ? null : java.sql.Timestamp.valueOf(evento.getFechaCreacion()));
                ps.setTime(6, evento.getHoraInicio() == null ? null : Time.valueOf(evento.getHoraInicio()));
                ps.setTime(7, evento.getHoraFin() == null ? null : Time.valueOf(evento.getHoraFin()));
                ps.setString(8, evento.getNombreCliente());
                ps.setInt(9, evento.getEspacio().getIdEspacio());
                ps.setInt(10, evento.getUsuario().getIdUsuario());
                ps.setString(11, evento.getEstado() != null ? evento.getEstado().name() : null);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se insertó ningún registro para el evento.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    //Evento Huesped 
    public int eventoHuesped(Evento evento) throws SQLException {
        FacesContext context = FacesContext.getCurrentInstance();
        Usuario usuarioLogueado = (Usuario) context.getExternalContext()
                .getSessionMap().get("usuarioLogueado");

        if (usuarioLogueado == null) {
            throw new SQLException("No hay usuario logueado en la sesión.");
        }

        String sql = "INSERT INTO evento(nombreEvento, descripcion, fechaEvento, fechaActualizacion, fechaCreacion, horaInicio, horaFin, nombreCliente, idEspacio, idUsuario, estado)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, evento.getNombreEvento());
                ps.setString(2, evento.getDescripcion());
                ps.setDate(3, evento.getFechaEvento() == null ? null : new java.sql.Date(evento.getFechaEvento().getTime()));
                ps.setTimestamp(4, evento.getFechaActualizacion() == null ? null : java.sql.Timestamp.valueOf(evento.getFechaActualizacion()));
                ps.setTimestamp(5, evento.getFechaCreacion() == null ? null : java.sql.Timestamp.valueOf(evento.getFechaCreacion()));
                ps.setTime(6, evento.getHoraInicio() == null ? null : Time.valueOf(evento.getHoraInicio()));
                ps.setTime(7, evento.getHoraFin() == null ? null : Time.valueOf(evento.getHoraFin()));
                ps.setString(8, evento.getNombreCliente());
                ps.setInt(9, evento.getEspacio().getIdEspacio());
                ps.setInt(10, usuarioLogueado.getIdUsuario());
                ps.setString(11, "Activa");

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se insertó ningún registro para el evento.");
                }

                 try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public void actualizar(Evento evento) throws SQLException {
        String sql = "UPDATE evento SET nombreEvento = ?, descripcion = ?, fechaEvento = ?, fechaActualizacion = ?, "
                + "fechaCreacion = ?, horaInicio = ?, horaFin = ?, nombreCliente = ?, idEspacio = ?, idUsuario = ?, estado = ? "
                + "WHERE idEvento = ?";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Campos de texto
                ps.setString(1, evento.getNombreEvento());
                ps.setString(2, evento.getDescripcion());
                if (evento.getFechaEvento() != null) {
                    ps.setDate(3, new java.sql.Date(evento.getFechaEvento().getTime()));
                } else {
                    ps.setNull(3, java.sql.Types.DATE);
                }
                ps.setTimestamp(4, evento.getFechaActualizacion() != null
                        ? java.sql.Timestamp.valueOf(evento.getFechaActualizacion()) : null);
                ps.setTimestamp(5, evento.getFechaCreacion() != null
                        ? java.sql.Timestamp.valueOf(evento.getFechaCreacion()) : null);
                ps.setTime(6, evento.getHoraInicio() != null
                        ? java.sql.Time.valueOf(evento.getHoraInicio()) : null);
                ps.setTime(7, evento.getHoraFin() != null
                        ? java.sql.Time.valueOf(evento.getHoraFin()) : null);
                ps.setString(8, evento.getNombreCliente());
                ps.setInt(9, evento.getEspacio().getIdEspacio());
                ps.setInt(10, evento.getUsuario().getIdUsuario());
                ps.setString(11, evento.getEstado() != null ? evento.getEstado().name() : null);
                ps.setInt(12, evento.getIdEvento());

                ps.executeUpdate();
            }
        }
    }

    public void actualizarFecha(int idEvento, java.sql.Date fechaEvento) throws SQLException {
        String sql = "UPDATE evento SET fechaEvento = ? WHERE idEvento = ?";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, (fechaEvento != null) ? fechaEvento : null);
                ps.setInt(2, idEvento);
                ps.executeUpdate();
            }
        }
    }

    public boolean espacioDisponible(int espacioId, Date fechaEvento, Integer eventoExcluirId) throws SQLException {
        if (fechaEvento == null) {
            throw new IllegalArgumentException("La fecha del evento no puede ser nula");
        }

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM evento WHERE idEspacio = ? AND fechaEvento = ?"
        );

        if (eventoExcluirId != null) {
            sql.append(" AND idEvento != ?");
        }

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                Timestamp fechaEventoTs = new Timestamp(fechaEvento.getTime());

                ps.setInt(1, espacioId);
                ps.setTimestamp(2, fechaEventoTs);

                if (eventoExcluirId != null) {
                    ps.setInt(3, eventoExcluirId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {

                        return rs.getInt(1) == 0;
                    }
                }
            }
        }

        return false;
    }

    public List<Evento> listarOcupacionesEspacio(int espacioId, Integer eventoExcluirId) throws SQLException {
        List<Evento> ocupaciones = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT idEvento, fechaEvento FROM evento WHERE idEspacio = ? ");

        if (eventoExcluirId != null) {
            sql.append("AND idEvento <> ? ");
        }

        sql.append("ORDER BY fechaEvento");

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                ps.setInt(1, espacioId);

                if (eventoExcluirId != null) {
                    ps.setInt(2, eventoExcluirId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Evento evento = new Evento();
                        evento.setIdEvento(rs.getInt("idEvento"));

                        java.sql.Date fechaSQL = rs.getDate("fechaEvento");
                        if (fechaSQL != null) {
                            evento.setFechaEvento(new java.util.Date(fechaSQL.getTime()));
                        }

                        ocupaciones.add(evento);
                    }
                }
            }
        }

        return ocupaciones;
    }


    private Evento mapearEvento(ResultSet rs) throws SQLException {
        Evento evento = new Evento();

        evento.setIdEvento(rs.getInt("idEvento"));
        evento.setNombreEvento(rs.getString("nombreEvento"));
        evento.setDescripcion(rs.getString("descripcion"));

        // Fecha del evento (DATE)
        Date fechaEvento = rs.getDate("fechaEvento");
        if (fechaEvento != null) {
            evento.setFechaEvento(fechaEvento);
        }

        // Fechas de creación y actualización (TIMESTAMP)
        Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
        if (fechaCreacion != null) {
            evento.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
        if (fechaActualizacion != null) {
            evento.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        // Horas de inicio y fin
        Time horaInicio = rs.getTime("horaInicio");
        if (horaInicio != null) {
            evento.setHoraInicio(horaInicio.toLocalTime());
        }

        Time horaFin = rs.getTime("horaFin");
        if (horaFin != null) {
            evento.setHoraFin(horaFin.toLocalTime());
        }

        // Cliente
        evento.setNombreCliente(rs.getString("nombreCliente"));

        // Espacio relacionado
        Espacio espacio = new Espacio();
        espacio.setIdEspacio(rs.getInt("idEspacio"));
        espacio.setNombre(rs.getString("nombreEspacio"));
        espacio.setDescripcion(rs.getString("descripcionEspacio"));
        espacio.setCapacidad(rs.getInt("capacidadEspacio"));
        float costoHora = rs.getFloat("costoHoraEspacio");
        if (!rs.wasNull()) {
            espacio.setCostoHora(costoHora);
        }
        String estadoEspacio = rs.getString("estadoEspacio");
        if (estadoEspacio != null) {
            espacio.setEstado(EnumEstadoEspacio.valueOf(estadoEspacio));
        }
        evento.setEspacio(espacio);

        // Usuario relacionado
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idUsuario"));
        usuario.setNombre(rs.getString("nombreUsuario"));
        usuario.setEmail(rs.getString("correoUsuario"));
        usuario.setTelefono(rs.getString("telefonoUsuario"));
        evento.setUsuario(usuario);

        // Estado del evento
        String estado = rs.getString("estado");
        if (estado != null) {
            evento.setEstado(EnumEstadoEvento.valueOf(estado));
        }

        return evento;
    }

    public Map<String, Integer> obtenerEventosPorMes(LocalDate fechaInicio) throws SQLException {
        Map<String, Integer> eventosPorMes = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(fechaEvento, '%Y-%m') AS mes, COUNT(*) AS total "
                + "FROM evento "
                + "WHERE fechaEvento >= ? "
                + "GROUP BY YEAR(fechaEvento), MONTH(fechaEvento) "
                + "ORDER BY YEAR(fechaEvento), MONTH(fechaEvento)";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, java.sql.Date.valueOf(fechaInicio));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        eventosPorMes.put(rs.getString("mes"), rs.getInt("total"));
                    }
                }
            }
        }

        return eventosPorMes;
    }

    public int contarEventosActivos() throws SQLException {

        String sql = "SELECT COUNT(*) AS total FROM evento WHERE UPPER(TRIM(estado)) = 'ACTIVA'";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    public void eliminar(int idEvento) throws SQLException {
        String sql = "DELETE from evento WHERE idEvento = ?";

        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                throw new SQLException("No se pudo establecer conexión a la base de datos.");
            }

            finalizarEventosVencidos(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEvento);
                ps.executeUpdate();
            }
        }

    }

    /**
     * Marca como finalizados todos los eventos cuya fecha y hora de fin ya
     * pasaron. Esto garantiza que los listados y contadores reflejen el estado
     * real sin requerir acciones manuales desde el panel administrativo.
     *
     * @return cantidad de registros actualizados
     * @throws SQLException si ocurre un error al actualizar los estados
     */
    public int finalizarEventosVencidos() throws SQLException {
        try (Connection conn = Conexion.conectar()) {
            if (conn == null) {
                return 0;
            }

            return finalizarEventosVencidos(conn);
        }
    }

    private int finalizarEventosVencidos(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FINALIZAR_EVENTOS)) {
            ps.setString(1, ESTADO_FINALIZADO);
            ps.setString(2, ESTADO_ACTIVO);
            return ps.executeUpdate();
        }
    }
}