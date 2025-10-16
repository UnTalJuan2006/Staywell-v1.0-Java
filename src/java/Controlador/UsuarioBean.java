package Controlador;

import DAO.UsuarioDAO;
import Modelo.CifradoAES;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import Modelo.Usuario;
import java.time.LocalDateTime;
import Modelo.EnumRoles;
import Modelo.EnumEstadoUsuario;
import java.util.List;
import java.util.Locale;
import javax.faces.bean.ApplicationScoped;

@ApplicationScoped
@ManagedBean
public class UsuarioBean {
    

    Usuario usuario = new Usuario();
    
     
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
 
        
        public List<Usuario> getListaUsuarios(){
            try {
                return usuarioDAO.listar();
            }catch  (SQLException e)  {
             System.out.println(" Error al listar habitaciones");
            return null;
            }
            
        }

    public void autenticar() throws SQLException, IOException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = Conexion.conectar();

            String sql = "SELECT * FROM usuario WHERE email = ? AND password = ? AND estado = 'Activo'";
            ps = con.prepareStatement(sql);
            ps.setString(1, usuario.getEmail());

            String passwordEncriptada = CifradoAES.encriptar(usuario.getPassword());
            ps.setString(2, passwordEncriptada);

            rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("idUsuario"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setDireccion(rs.getString("direccion"));
                u.setTelefono(rs.getString("telefono"));
                //u.setPassword(rs.getString("password")); // puede omitirse por seguridad
                //u.setEstado(EnumEstadoUsuario.valueOf(rs.getString("estado").trim().toUpperCase(Locale.ROOT)));

                String rolDb = rs.getString("rol").trim().toUpperCase(Locale.ROOT);
                EnumRoles rol = EnumRoles.valueOf(rolDb);
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

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", "Error en la conexión o autenticación"));
        } finally {
            // 🔹 Cerramos recursos para evitar fugas
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
            if (con != null) try { con.close(); } catch (SQLException ignored) {}
        }
    }


    public void cerrarSesion() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().invalidateSession(); // ✅ invalida la sesión completa
            facesContext.getExternalContext().redirect("index.html"); // redirige al inicio/login
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void verifSesion() {
        String nom = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

        if (nom == null) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("noacceso.xhtml");
            } catch (IOException ex) {
            }

        }
    }

    public void agregar() {
        try {
            usuario.setFechaCreacion(LocalDateTime.now());
            usuario.setFechaActualizacion(LocalDateTime.now());

            // Encriptar la contraseña
            String passEncriptada = CifradoAES.encriptar(usuario.getPassword());
            usuario.setPassword(passEncriptada);

            // Asignar valores por defecto
            usuario.setRol(EnumRoles.HUESPED);
            usuario.setEstado(EnumEstadoUsuario.Activo);
            usuarioDAO.agregar(usuario);

            // Mensaje de éxito
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Usuario registrado correctamente."));

            // Limpiar formulario
            usuario = new Usuario();
            FacesContext.getCurrentInstance().getExternalContext()
                .redirect("login.xhtml");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo registrar el usuario: " + e.getMessage()));
        }
       
    }
    public int totalUsuarios() throws SQLException {
    int total = 0;
    String sql = "SELECT COUNT(*) AS total FROM usuario";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            total = rs.getInt("total");
        }

    } catch (SQLException e) {
        System.out.println("Error al obtener total de usuarios: " + e.getMessage());
        throw e;
    }

    return total;
}
    
    public int totalUsuariosHuesped() throws SQLException{
        int totalHuespedes = 0;
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE rol = 'Huesped'";
        
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
         ResultSet rs = ps.executeQuery())  {
            if(rs.next()){
                totalHuespedes = rs.getInt("total");
                
            }
        }catch (SQLException e){
            System.out.println("Error al obtener total de usuarios: " + e.getMessage());
             throw e;
        }
       return totalHuespedes;
    }
    
    public int totalActivos() throws SQLException{
        int totalActivos = 0;
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE estado = 'Activo' AND rol='Huesped'";
        
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
         ResultSet rs = ps.executeQuery())  {
            if(rs.next()){
                totalActivos = rs.getInt("total");
                
            }
        }catch (SQLException e){
            System.out.println("Error al obtener total de usuarios: " + e.getMessage());
             throw e;
        }
        return totalActivos;
    }
    
    public int totalInactivos() throws SQLException{
        int totalInactivos = 0;
        String sql= "SELECT COUNT(*) AS total FROM  usuario WHERE estado = 'Inactivo' AND rol = 'Huesped'";
        
          try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
         ResultSet rs = ps.executeQuery())  {
            if(rs.next()){
                totalInactivos = rs.getInt("total");
                
            }
        }catch (SQLException e){
            System.out.println("Error al obtener total de usuarios: " + e.getMessage());
             throw e;
        }
          return totalInactivos;
    }
    
    

    
    public String eliminar(Usuario u){
        try{
            usuarioDAO.eliminar(u);
             FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Error al eliminar usuario"));
        }
        return "Usuarioss?faces-redirect=true";
    }
    
    //Para dejar activo e inactivo el usuario
    public void toggleEstado(Usuario u) {
    try {
        EnumEstadoUsuario nuevoEstado = 
            (u.getEstado() == EnumEstadoUsuario.Activo) 
                ? EnumEstadoUsuario.Inactivo 
                : EnumEstadoUsuario.Activo;

        usuarioDAO.cambiarEstado(u.getIdUsuario(), nuevoEstado);

        u.setEstado(nuevoEstado);

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Éxito", "El usuario fue " + (nuevoEstado == EnumEstadoUsuario.Activo ? "activado" : "inactivado")));
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Error", "No se pudo cambiar el estado: " + e.getMessage()));
    }
}

}