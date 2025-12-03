package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoNotificacion;
import Modelo.EnumTipoNotificacion;
import Modelo.Notificacion;
import Modelo.Usuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public List<Notificacion> listar() throws SQLException {
        List<Notificacion> lista = new ArrayList<>();

        String sql
                = "SELECT n.idNotificacion, n.titulo, n.mensaje, n.fechaEnvio, n.estado, n.tipo, "
                + "u.idUsuario, u.nombre, u.email "
                + "FROM notificacion n "
                + "LEFT JOIN usuario u ON n.idUsuario = u.idUsuario "
                + "ORDER BY n.fechaEnvio DESC";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearNotificacion(rs));
            }
        }

        return lista;
    }

    public void actualizar(Notificacion notificacion) throws SQLException {
        String sql = "UPDATE notificacion SET titulo = ?, mensaje = ? WHERE idNotificacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, notificacion.getTitulo());
            ps.setString(2, notificacion.getMensaje());
            ps.setInt(3, notificacion.getIdNotificacion());

            ps.executeUpdate();
        }
    }

    public void eliminar(int idNotificacion) throws SQLException {
        String sql = "DELETE FROM notificacion WHERE idNotificacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.executeUpdate();
        }
    }

    public List<Notificacion> listarGeneralesYUsuario(int idUsuario) throws SQLException {
        List<Notificacion> lista = new ArrayList<>();

        String sql = "SELECT n.idNotificacion, n.titulo, n.mensaje, n.fechaEnvio, n.estado, n.tipo, "
                + "u.idUsuario, u.nombre, u.email "
                + "FROM notificacion n "
                + "LEFT JOIN usuario u ON n.idUsuario = u.idUsuario "
                + "WHERE n.idUsuario IS NULL OR n.idUsuario = ? "
                + "ORDER BY n.fechaEnvio DESC";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearNotificacion(rs));
                }
            }
        }

        return lista;
    }

    public List<Notificacion> listarNuevasReservasParaAdmin(int idUsuario) throws SQLException {
        List<Notificacion> lista = new ArrayList<>();

        String sql = "SELECT n.idNotificacion, n.titulo, n.mensaje, n.fechaEnvio, n.estado, n.tipo, "
                + "u.idUsuario, u.nombre, u.email "
                + "FROM notificacion n "
                + "LEFT JOIN usuario u ON n.idUsuario = u.idUsuario "
                + "WHERE n.tipo = ? AND (n.idUsuario IS NULL OR n.idUsuario = ?) "
                + "ORDER BY n.fechaEnvio DESC";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, EnumTipoNotificacion.NUEVARESERVA.name());
            ps.setInt(2, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearNotificacion(rs));
                }
            }
        }

        return lista;
    }

    public void actualizarEstado(int idNotificacion, EnumEstadoNotificacion estado) throws SQLException {
        String sql = "UPDATE notificacion SET estado = ? WHERE idNotificacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idNotificacion);
            ps.executeUpdate();
        }
    }

    public List<Notificacion> listarPorUsuario(int idUsuario) throws SQLException {
        List<Notificacion> lista = new ArrayList<>();

        String sql = "SELECT n.idNotificacion, n.titulo, n.mensaje, n.fechaEnvio, n.estado, n.tipo, "
                + "u.idUsuario, u.nombre, u.email "
                + "FROM notificacion n "
                + "LEFT JOIN usuario u ON n.idUsuario = u.idUsuario "
                + "WHERE n.idUsuario = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                    lista.add(mapearNotificacion(rs));
                }
            }
        }

        return lista;
    }
    
    public void enviarPorUsuario(Notificacion notificacion, int idUsuario) throws SQLException {

        String sql = "INSERT INTO notificacion (titulo, mensaje, fechaEnvio, estado, tipo, idUsuario) "
                + "VALUES (?, ?, NOW(), ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

            ps.setString(1, notificacion.getTitulo());
            ps.setString(2, notificacion.getMensaje());
            ps.setString(3, notificacion.getEstado().name());
            ps.setString(4, notificacion.getTipo().name());
            ps.setInt(5, idUsuario);

            ps.executeUpdate();
        }
    }

    public void enviarNuevasReservasParaAdmins(Notificacion notificacion, List<Usuario> administradores) throws SQLException {
        if (administradores == null || administradores.isEmpty()) {
            enviarGeneral(notificacion);
            return;
        }

        for (Usuario admin : administradores) {
            enviarPorUsuario(notificacion, admin.getIdUsuario());
        }
    }
    
    public void enviarGeneral(Notificacion notificacion) throws SQLException {

        String sql = "INSERT INTO notificacion (titulo, mensaje, fechaEnvio, estado, tipo, idUsuario) "
                + "VALUES (?, ?, NOW(), ?, ?, NULL)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

            ps.setString(1, notificacion.getTitulo());
            ps.setString(2, notificacion.getMensaje());
            ps.setString(3, notificacion.getEstado().name());
            ps.setString(4, notificacion.getTipo().name());

            ps.executeUpdate();
        }
    }

    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {
        Notificacion notificacion = new Notificacion();

        notificacion.setIdNotificacion(rs.getInt("idNotificacion"));
        notificacion.setTitulo(rs.getString("titulo"));
        notificacion.setMensaje(rs.getString("mensaje"));
        notificacion.setFechaEnvio(rs.getTimestamp("fechaEnvio").toLocalDateTime());
        notificacion.setEstado(EnumEstadoNotificacion.valueOf(rs.getString("estado").toUpperCase()));
        notificacion.setTipo(EnumTipoNotificacion.valueOf(rs.getString("tipo").toUpperCase()));

        int idUsuario = rs.getInt("idUsuario");
        if (!rs.wasNull()) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idUsuario);
            usuario.setNombre(rs.getString("nombre"));
            usuario.setEmail(rs.getString("email"));

            notificacion.setUsuario(usuario);
        }

        return notificacion;
    }
}
