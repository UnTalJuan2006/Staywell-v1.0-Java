package Controlador;

import DAO.NotificacionDAO;
import DAO.UsuarioDAO;
import Modelo.EnumEstadoNotificacion;
import Modelo.EnumTipoNotificacion;
import Modelo.Notificacion;
import Modelo.Usuario;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class NotificacionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Notificacion notificacionGeneral;
    private Notificacion notificacionPersonal;
    private Integer usuarioSeleccionado;

    private List<Usuario> listaUsuarios;
    private List<Notificacion> historialNotificaciones;
    private List<Notificacion> notificacionesUsuario;
    private Notificacion notificacionEdicion;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private transient NotificacionDAO notificacionDAO;
    private transient UsuarioDAO usuarioDAO;

    @PostConstruct
    public void init() {
        notificacionGeneral = crearBorrador(EnumTipoNotificacion.GENERAL);
        notificacionPersonal = crearBorrador(EnumTipoNotificacion.PERSONAL);

        cargarUsuarios();
        cargarHistorial();
        cargarNotificacionesSesion();
    }

    public Notificacion getNotificacionGeneral() {
        return notificacionGeneral;
    }

    public Notificacion getNotificacionPersonal() {
        return notificacionPersonal;
    }

    public Integer getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Integer usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public List<Notificacion> getHistorialNotificaciones() {
        return historialNotificaciones;
    }

    public List<Notificacion> getNotificacionesUsuario() {
        return notificacionesUsuario;
    }

    public Notificacion getNotificacionEdicion() {
        return notificacionEdicion;
    }

    public void setNotificacionEdicion(Notificacion notificacionEdicion) {
        this.notificacionEdicion = notificacionEdicion;
    }

    public int getTotalUsuarios() {
        return listaUsuarios != null ? listaUsuarios.size() : 0;
    }

    public long getTotalNoLeidas() {
        if (notificacionesUsuario == null) {
            return 0;
        }

        long total = 0;
        for (Notificacion notif : notificacionesUsuario) {
            if (EnumEstadoNotificacion.NO_LEIDA.equals(notif.getEstado())) {
                total++;
            }
        }
        return total;
    }

    public String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }

        return fecha.format(formatter);
    }

    public void enviarNotificacionGeneral() {
        notificacionGeneral.setTipo(EnumTipoNotificacion.GENERAL);
        notificacionGeneral.setEstado(EnumEstadoNotificacion.NO_LEIDA);
        notificacionGeneral.setFechaEnvio(LocalDateTime.now());

        try {
            getNotificacionDAO().enviarGeneral(notificacionGeneral);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Notificación general enviada correctamente.");
            notificacionGeneral = crearBorrador(EnumTipoNotificacion.GENERAL);
            cargarHistorial();
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo enviar la notificación general.");
        }
    }

    public void prepararEdicion(Notificacion original) {
        if (original == null) {
            return;
        }

        Notificacion copia = new Notificacion();
        copia.setIdNotificacion(original.getIdNotificacion());
        copia.setTitulo(original.getTitulo());
        copia.setMensaje(original.getMensaje());
        copia.setFechaEnvio(original.getFechaEnvio());
        copia.setEstado(original.getEstado());
        copia.setTipo(original.getTipo());
        copia.setUsuario(original.getUsuario());

        notificacionEdicion = copia;
    }

    public void actualizarNotificacion() {
        if (notificacionEdicion == null) {
            return;
        }

        try {
            getNotificacionDAO().actualizar(notificacionEdicion);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Notificación actualizada correctamente.");
            cargarHistorial();
            notificacionEdicion = null;
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la notificación.");
        }
    }

    public void eliminarNotificacion(int idNotificacion) {
        try {
            getNotificacionDAO().eliminar(idNotificacion);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Notificación eliminada correctamente.");
            cargarHistorial();
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la notificación.");
        }
    }

    public void enviarNotificacionPersonal() {
        if (usuarioSeleccionado == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Seleccione un destinatario.");
            return;
        }

        notificacionPersonal.setTipo(EnumTipoNotificacion.PERSONAL);
        notificacionPersonal.setEstado(EnumEstadoNotificacion.NO_LEIDA);
        notificacionPersonal.setFechaEnvio(LocalDateTime.now());

        try {
            getNotificacionDAO().enviarPorUsuario(notificacionPersonal, usuarioSeleccionado);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Notificación enviada al usuario seleccionado.");
            notificacionPersonal = crearBorrador(EnumTipoNotificacion.PERSONAL);
            usuarioSeleccionado = null;
            cargarHistorial();
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo enviar la notificación personal.");
        }
    }

    public void cargarNotificacionesSesion() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }

        Usuario usuarioLogueado = (Usuario) context.getExternalContext().getSessionMap()
                .get("usuarioLogueado");

        if (usuarioLogueado == null) {
            return;
        }

        try {
            notificacionesUsuario = getNotificacionDAO().listarGeneralesYUsuario(usuarioLogueado.getIdUsuario());
        } catch (SQLException e) {
            notificacionesUsuario = new ArrayList<>();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las notificaciones.");
        }
    }

    public void marcarComoLeida(Notificacion notificacion) {
        if (notificacion == null || EnumEstadoNotificacion.LEIDA.equals(notificacion.getEstado())) {
            return;
        }

        try {
            getNotificacionDAO().actualizarEstado(notificacion.getIdNotificacion(), EnumEstadoNotificacion.LEIDA);
            cargarNotificacionesSesion();
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el estado de la notificación.");
        }
    }

    public void cancelarEdicion() {
        notificacionEdicion = null;
    }

    private void cargarHistorial() {
        try {
            historialNotificaciones = getNotificacionDAO().listar();
        } catch (SQLException e) {
            historialNotificaciones = new ArrayList<>();
        }
    }

    private void cargarUsuarios() {
        try {
            listaUsuarios = getUsuarioDAO().listar();
        } catch (SQLException e) {
            listaUsuarios = new ArrayList<>();
        }
    }

    private Notificacion crearBorrador(EnumTipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setTipo(tipo);
        notificacion.setEstado(EnumEstadoNotificacion.NO_LEIDA);
        return notificacion;
    }

    private void mostrarMensaje(FacesMessage.Severity severidad, String titulo, String detalle) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        if (contexto != null) {
            contexto.addMessage(null, new FacesMessage(severidad, titulo, detalle));
        }
    }

    private NotificacionDAO getNotificacionDAO() {
        if (notificacionDAO == null) {
            notificacionDAO = new NotificacionDAO();
        }
        return notificacionDAO;
    }

    private UsuarioDAO getUsuarioDAO() {
        if (usuarioDAO == null) {
            usuarioDAO = new UsuarioDAO();
        }
        return usuarioDAO;
    }
}
