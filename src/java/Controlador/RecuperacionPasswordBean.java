package Controlador;

import DAO.RecuperacionPasswordDAO;
import DAO.UsuarioDAO;
import Modelo.CifradoAES;
import Modelo.RecuperacionPassword;
import Modelo.Usuario;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class RecuperacionPasswordBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String codigoVerificacion;
    private String nuevaPassword;
    private String confirmarPassword;
    private boolean codigoEnviado;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RecuperacionPasswordDAO recuperacionDAO = new RecuperacionPasswordDAO();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }

    public boolean isCodigoEnviado() {
        return codigoEnviado;
    }

    public void enviarCodigo() {
        try {
            if (email == null || email.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Debe ingresar un correo electrónico"));
                return;
            }

            email = email.trim().toLowerCase(Locale.ROOT);
            Usuario usuario = usuarioDAO.buscarPorEmail(email);
            if (usuario == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo no está registrado"));
                return;
            }

            recuperacionDAO.invalidarTokensActivos(usuario.getIdUsuario());

            String token = generarCodigo();
            LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15);

            RecuperacionPassword rec = new RecuperacionPassword();
            rec.setIdUsuario(usuario.getIdUsuario());
            rec.setToken(token);
            rec.setFechaExpiracion(expiracion);
            rec.setUsado(false);

            recuperacionDAO.crearRegistro(rec);
            CorreoBean.enviarCodigoRecuperacion(email.trim(), token);

            codigoEnviado = true;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Código enviado", "Revisa tu bandeja de correo"));
        } catch (SQLException e) {
            codigoEnviado = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo generar el código: " + e.getMessage()));
        } catch (Exception e) {
            codigoEnviado = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al enviar el código"));
        }
    }

    public void validarYRestablecer() {
        try {
            if (!codigoEnviado) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Primero solicita un código de verificación"));
                return;
            }

            if (codigoVerificacion == null || codigoVerificacion.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Debe ingresar el código enviado"));
                return;
            }

            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()
                    || confirmarPassword == null || confirmarPassword.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Debe ingresar y confirmar la nueva contraseña"));
                return;
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Las contraseñas no coinciden"));
                return;
            }

            email = email.trim().toLowerCase(Locale.ROOT);
            Usuario usuario = usuarioDAO.buscarPorEmail(email);
            if (usuario == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró el usuario"));
                return;
            }

            RecuperacionPassword rec = recuperacionDAO.obtenerTokenValido(codigoVerificacion.trim(), usuario.getIdUsuario());
            if (rec == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El código es inválido o ha expirado"));
                return;
            }

            String passwordEncriptado = CifradoAES.encriptar(nuevaPassword);
            usuarioDAO.actualizarPassword(usuario.getIdUsuario(), passwordEncriptado);
            recuperacionDAO.marcarComoUsado(rec.getIdRecuperacion());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Contraseña actualizada", "Ahora puedes iniciar sesión"));

            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (SQLException | IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo restablecer la contraseña: " + e.getMessage()));
        }
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int numero = random.nextInt(900000) + 100000;
        return String.valueOf(numero);
    }
}
