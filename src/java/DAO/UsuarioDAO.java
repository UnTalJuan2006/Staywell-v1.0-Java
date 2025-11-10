package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoUsuario;
import Modelo.EnumRoles;
import Modelo.Usuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> listar() throws SQLException {
        List<Usuario> listaUsuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE LOWER(rol) = 'huesped'";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));

                // Manejo seguro de timestamps
                java.sql.Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                if (fechaCreacion != null) {
                    u.setFechaCreacion(fechaCreacion.toLocalDateTime());
                }

                java.sql.Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                if (fechaActualizacion != null) {
                    u.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                }

                u.setRol(EnumRoles.HUESPED);
                u.setEstado(EnumEstadoUsuario.valueOf(rs.getString("estado")));
                u.setDireccion(rs.getString("direccion"));
                u.setTelefono(rs.getString("telefono"));

                listaUsuarios.add(u);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar usuarios: " + e.getMessage());
            throw e;
        }

        return listaUsuarios;
    }

    public void agregar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, fechaCreacion, fechaActualizacion, rol, password, estado, direccion, telefono) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setTimestamp(3, Timestamp.valueOf(u.getFechaCreacion()));
            ps.setTimestamp(4, Timestamp.valueOf(u.getFechaActualizacion()));
            ps.setString(5, u.getRol().name().toLowerCase());
            ps.setString(6, u.getPassword());
            ps.setString(7, u.getEstado().name());
            ps.setString(8, u.getDireccion());
            ps.setString(9, u.getTelefono());

            ps.executeUpdate();
            System.out.println("Usuario agregado con éxito: " + u.getEmail());
        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            throw e;
        }
    }

    public void eliminar(Usuario u) throws SQLException {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, u.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            throw e;
        }
    }
    
    
    
    public Usuario buscar(int id) throws SQLException {
        Usuario u = null;
        String sql = "SELECT * FROM usuario WHERE idUsuario = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if ("huesped".equalsIgnoreCase(rs.getString("rol"))) {
                        u = new Usuario();
                        u.setIdUsuario(rs.getInt("idUsuario"));
                        u.setNombre(rs.getString("nombre"));
                        u.setEmail(rs.getString("email"));

                        // Manejo seguro de timestamps
                        java.sql.Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                        if (fechaCreacion != null) {
                            u.setFechaCreacion(fechaCreacion.toLocalDateTime());
                        }

                        java.sql.Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                        if (fechaActualizacion != null) {
                            u.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                        }

                        u.setRol(EnumRoles.HUESPED);
                        u.setEstado(EnumEstadoUsuario.valueOf(rs.getString("estado")));
                        u.setDireccion(rs.getString("direccion"));
                        u.setTelefono(rs.getString("telefono"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar usuario: " + e.getMessage());
            throw e;
        }

        return u;
    }
    
    
    public void cambiarEstado(int idUsuario, EnumEstadoUsuario nuevoEstado) throws SQLException {
        String sql = "UPDATE usuario SET estado = ?, fechaActualizacion = ? WHERE idUsuario = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(3, idUsuario);

            ps.executeUpdate();
            System.out.println("Estado cambiado a " + nuevoEstado + " para usuario con ID: " + idUsuario);
        } catch (SQLException e) {
            System.out.println("Error al cambiar estado del usuario: " + e.getMessage());
            throw e;
        }
    }


}
