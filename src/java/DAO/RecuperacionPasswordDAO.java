package DAO;

import Controlador.Conexion;
import Modelo.RecuperacionPassword;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RecuperacionPasswordDAO {

    private Connection obtenerConexion() throws SQLException {
        Connection con = Conexion.conectar();
        if (con == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos");
        }
        return con;
    }

    public void invalidarTokensActivos(int idUsuario) throws SQLException {
        String sql = "UPDATE recuperacion_password SET usado = 1 WHERE idUsuario = ? AND usado = 0";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void crearRegistro(RecuperacionPassword rec) throws SQLException {
        String sql = "INSERT INTO recuperacion_password (idUsuario, token, fechaExpiracion, usado) VALUES (?, ?, ?, ?)";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rec.getIdUsuario());
            ps.setString(2, rec.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(rec.getFechaExpiracion()));
            ps.setBoolean(4, rec.isUsado());
            ps.executeUpdate();
        }
    }

    public RecuperacionPassword obtenerTokenValido(String token, int idUsuario) throws SQLException {
        String sql = "SELECT * FROM recuperacion_password WHERE token = ? AND idUsuario = ? AND usado = 0 AND fechaExpiracion > ? ORDER BY idRecuperacion DESC LIMIT 1";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RecuperacionPassword rec = new RecuperacionPassword();
                    rec.setIdRecuperacion(rs.getInt("idRecuperacion"));
                    rec.setIdUsuario(rs.getInt("idUsuario"));
                    rec.setToken(rs.getString("token"));
                    Timestamp fecha = rs.getTimestamp("fechaExpiracion");
                    if (fecha != null) {
                        rec.setFechaExpiracion(fecha.toLocalDateTime());
                    }
                    rec.setUsado(rs.getBoolean("usado"));
                    return rec;
                }
            }
        }
        return null;
    }

    public void marcarComoUsado(int idRecuperacion) throws SQLException {
        String sql = "UPDATE recuperacion_password SET usado = 1 WHERE idRecuperacion = ?";
        try (Connection con = obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRecuperacion);
            ps.executeUpdate();
        }
    }
}
