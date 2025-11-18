package Controlador;

import DAO.UsuarioDAO;
import Modelo.CifradoAES;
import Modelo.Usuario;
import Modelo.EnumRoles;
import Modelo.EnumEstadoUsuario;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ApplicationScoped
@ManagedBean
public class UsuarioBean {

    private Usuario usuario = new Usuario();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ========================== Getters y Setters ==========================
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // ========================== Listado de usuarios ==========================
    public List<Usuario> getListaUsuarios() {
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
            return null;
        }
    }

    // ========================== Autenticación ==========================
    public void autenticar() throws SQLException, IOException {
        try (Connection con = Conexion.conectar()) {
            String sql = "SELECT * FROM usuario WHERE email = ? AND password = ? AND estado = 'Activo'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, usuario.getEmail());
                ps.setString(2, CifradoAES.encriptar(usuario.getPassword()));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Usuario u = new Usuario();
                        u.setIdUsuario(rs.getInt("idUsuario"));
                        u.setNombre(rs.getString("nombre"));
                        u.setEmail(rs.getString("email"));
                        u.setDireccion(rs.getString("direccion"));
                        u.setTelefono(rs.getString("telefono"));

                        EnumRoles rol = EnumRoles.valueOf(rs.getString("rol").trim().toUpperCase(Locale.ROOT));
                        u.setRol(rol);

                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioLogueado", u);

                        if (rol == EnumRoles.ADMIN) {
                            FacesContext.getCurrentInstance().getExternalContext().redirect("HomeAdmin.xhtml");
                        } else {
                            FacesContext.getCurrentInstance().getExternalContext().redirect("HomeHuesped.xhtml");
                        }
                    } else {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Correo o contraseña inválidos"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", "Error en la conexión o autenticación"));
        }
    }

    // ========================== Cerrar sesión ==========================
    public void cerrarSesion() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().invalidateSession();
            facesContext.getExternalContext().redirect("index.html");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ========================== Verificar sesión ==========================
    public void verifSesion() {
        Object usuarioLog = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuarioLogueado");
        if (usuarioLog == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("noacceso.xhtml");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ========================== Registro de usuario ==========================
    public void agregar() {
        try {
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setFechaActualizacion(LocalDateTime.now());

            usuario.setPassword(CifradoAES.encriptar(usuario.getPassword()));
            usuario.setRol(EnumRoles.HUESPED);
            usuario.setEstado(EnumEstadoUsuario.Activo);

            usuarioDAO.agregar(usuario);

            // 🎯 ENVÍO DE CORREO - MÉTODO ESTÁTICO
            CorreoBean.enviarCorreoBienvenida(usuario.getEmail(), usuario.getNombre());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Usuario registrado. Correo de bienvenida enviado."));

            usuario = new Usuario();
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al registrar usuario: " + e.getMessage()));
        }
    }

    // ========================== Estadísticas de usuarios ==========================
    public int totalUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public int totalUsuariosHuesped() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE rol = 'HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public int totalActivos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE estado = 'Activo' AND rol='HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    public int totalInactivos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE estado = 'Inactivo' AND rol='HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    // ========================== Eliminar usuario ==========================
    public String eliminar(Usuario u) {
        try {
            usuarioDAO.eliminar(u);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Error al eliminar usuario"));
        }
        return "Usuarioss?faces-redirect=true";
    }

    // ========================== Activar/Inactivar usuario ==========================
    public void toggleEstado(Usuario u) {
        try {
            EnumEstadoUsuario nuevoEstado =
                    (u.getEstado() == EnumEstadoUsuario.Activo)
                            ? EnumEstadoUsuario.Inactivo
                            : EnumEstadoUsuario.Activo;

            usuarioDAO.cambiarEstado(u.getIdUsuario(), nuevoEstado);
            u.setEstado(nuevoEstado);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "El usuario fue " + (nuevoEstado == EnumEstadoUsuario.Activo ? "activado" : "inactivado")));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo cambiar el estado: " + e.getMessage()));
        }
    }
}
