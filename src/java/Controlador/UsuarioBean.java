package Controlador;

import DAO.UsuarioDAO;
import Modelo.CifradoAES;
import Modelo.Usuario;
import Modelo.EnumRoles;
import Modelo.EnumEstadoUsuario;
import Modelo.Habitacion;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import util.ExcelUtil;
import util.PdfUtil;

@ViewScoped
@ManagedBean
public class UsuarioBean implements Serializable {

    private Usuario usuario = new Usuario();
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    private List<Usuario> usuariosFiltrados;
    private String filtro;
    private List<Usuario> usuarios;

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public List<Usuario> getUsuariosFiltrados() {
        return usuariosFiltrados;
    }

    public void cargarPerfilSesion() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.isPostback()) {
            return;
        }

        try {
            Usuario logueado = (Usuario) context.getExternalContext()
                    .getSessionMap().get("usuarioLogueado");

            if (logueado != null) {
                Usuario desdeDb = usuarioDAO.obtenerPorId(logueado.getIdUsuario());
                usuario = desdeDb != null ? desdeDb : logueado;
            }

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo cargar el perfil del usuario."));
        }
    }

    @PostConstruct
    public void init() {
        try {
            usuarios = usuarioDAO.listar(); 
            if (usuarios == null) {
                usuarios = new ArrayList<>();
            }
            usuariosFiltrados = new ArrayList<>(usuarios);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void buscarUsuarios() {
        if (usuarios == null) {
            return;
        }

        if (filtro == null || filtro.trim().isEmpty()) {
            usuariosFiltrados = new ArrayList<>(usuarios);
            return;
        }

        String txt = filtro.trim().toLowerCase();
        List<Usuario> resultados = new ArrayList<>();

        for (Usuario u : usuarios) {
            boolean coincideNombre = u.getNombre() != null && u.getNombre().toLowerCase().contains(txt);
            boolean coincideEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(txt);
            boolean coincideRol = u.getRol() != null && u.getRol().name().toLowerCase().contains(txt);
            boolean coincideEstado = u.getEstado() != null && u.getEstado().name().toLowerCase().contains(txt);

            if (coincideNombre || coincideEmail || coincideRol || coincideEstado) {
                resultados.add(u);
            }
        }

        usuariosFiltrados = resultados;
    }

    public List<Usuario> getListaUsuarios() {
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
            return null;
        }
    }

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

    public void cerrarSesion() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().invalidateSession();
            facesContext.getExternalContext().redirect("index.html");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

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

    public int totalUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int totalUsuariosHuesped() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE rol = 'HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int totalActivos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE estado = 'Activo' AND rol='HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int totalInactivos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE estado = 'Inactivo' AND rol='HUESPED'";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }


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

    public void toggleEstado(Usuario u) {
        try {
            EnumEstadoUsuario nuevoEstado
                    = (u.getEstado() == EnumEstadoUsuario.Activo)
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

    public void exportarExcelUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.listar();
            String[] headers = {
                "Id", "nombre", "email", "fechaCreacion", "fechaActualizacion", "estado"
            };

            List<Object[]> datos = lista.stream()
                    .map(u -> new Object[]{
                u.getIdUsuario(),
                u.getNombre(),
                u.getEmail(),
                u.getFechaCreacion(),
                u.getFechaActualizacion(),
                u.getEstado().name()

            })
                    .collect(java.util.stream.Collectors.toList());
            
            ExcelUtil.generarExcel("usuarios", "Usuarios", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportarPdfUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.listar();
            String[] headers = {
                "Id", "nombre", "email", "fechaCreacion", "fechaActualizacion", "estado"
            };
            List<Object[]> datos = lista.stream()
                    .map(u -> new Object[]{
                u.getIdUsuario(),
                u.getNombre(),
                u.getEmail(),
                u.getFechaCreacion(),
                u.getFechaActualizacion(),
                u.getEstado().name()

            })
                    .collect(java.util.stream.Collectors.toList()); // <-- CORRECCIÓN

            PdfUtil.generarPdf( "Usuarios", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String actualizar() {
    try {
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioDAO.actualizar(usuario);

        // 🔥 Actualizar los datos del usuario en sesión
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("usuarioLogueado", usuario);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Usuario actualizado correctamente."));

        return "perfil?faces-redirect=true";

    } catch (SQLException e) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "No se pudo actualizar el usuario: " + e.getMessage()));
        return null;
    }
}

    public void actualizarPerfilHuesped() {
        try {
            Usuario enSesion = (Usuario) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get("usuarioLogueado");

            if (enSesion != null) {
                usuario.setIdUsuario(enSesion.getIdUsuario());
            }

            usuario.setFechaActualizacion(LocalDateTime.now());
            usuarioDAO.actualizar(usuario);

            Usuario refrescado = usuarioDAO.obtenerPorId(usuario.getIdUsuario());
            if (refrescado != null) {
                usuario = refrescado;
            }

            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("usuarioLogueado", usuario);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Perfil actualizado",
                            "Tus datos se guardaron correctamente."));

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "No se pudo actualizar el perfil: " + e.getMessage()));
        }
    }


    public void cargarUsuarioPorId() {
    try {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam == null || idParam.isEmpty()) {
            System.out.println("⚠ No llegó el parámetro ID");
            return;
        }

        int id = Integer.parseInt(idParam);
        Usuario u = usuarioDAO.obtenerPorId(id); // <-- NECESITAS ESTE MÉTODO EN EL DAO

        if (u != null) {
            this.usuario = u;   // <-- ASIGNARLO ES CLAVE
        } else {
            System.out.println("⚠ No se encontró el usuario con ID: " + id);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
