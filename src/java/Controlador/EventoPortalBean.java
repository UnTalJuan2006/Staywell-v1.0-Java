package Controlador;

import DAO.EventoDAO;
import Modelo.EnumEstadoEvento;
import Modelo.Evento;
import Modelo.Usuario;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class EventoPortalBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventoDAO eventoDAO = new EventoDAO();

    private List<Evento> eventosDisponibles = new ArrayList<>();
    private List<Evento> eventosDelUsuario = new ArrayList<>();
    private Evento eventoDetalle;

    private Usuario usuarioLogueado;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            usuarioLogueado = (Usuario) context.getExternalContext()
                    .getSessionMap().get("usuarioLogueado");
        }
        cargarEventosDisponibles();
        cargarEventosDelUsuario();
    }

    public void cargarEventosDisponibles() {
        try {
            eventosDisponibles = eventoDAO.listarActivos();
        } catch (SQLException ex) {
            eventosDisponibles = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudieron cargar los eventos activos",
                            ex.getMessage()));
        }
    }

    public void cargarEventosDelUsuario() {
        if (usuarioLogueado == null) {
            eventosDelUsuario = new ArrayList<>();
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Inicia sesión",
                                "Debes autenticarte para ver tus eventos."));
            }
            return;
        }
        try {
            eventosDelUsuario = eventoDAO.listarPorUsuario(usuarioLogueado.getIdUsuario());
        } catch (SQLException ex) {
            eventosDelUsuario = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudieron cargar tus eventos",
                            ex.getMessage()));
        }
    }

    public void cargarEventoParaDetalle() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idParam = externalContext.getRequestParameterMap().get("id");

        if (idParam == null || idParam.isEmpty()) {
            eventoDetalle = null;
            return;
        }

        try {
            int idEvento = Integer.parseInt(idParam);
            Evento evento = eventoDAO.buscar(idEvento);
            if (evento == null) {
                mostrarAdvertencia("El evento solicitado no existe.");
                eventoDetalle = null;
                return;
            }

            if (!puedeVisualizar(evento)) {
                mostrarAdvertencia("No tienes permisos para visualizar este evento.");
                eventoDetalle = null;
                return;
            }

            eventoDetalle = evento;
        } catch (NumberFormatException ex) {
            mostrarAdvertencia("El identificador del evento no es válido.");
            eventoDetalle = null;
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la información del evento.", ex.getMessage());
            eventoDetalle = null;
        }
    }

    private boolean puedeVisualizar(Evento evento) {
        if (evento == null) {
            return false;
        }

        if (evento.getEstado() == EnumEstadoEvento.Activa) {
            return true;
        }

        return usuarioLogueado != null
                && evento.getUsuario() != null
                && usuarioLogueado.getIdUsuario() == evento.getUsuario().getIdUsuario();
    }

    public boolean isEventoDetallePropio() {
        return eventoDetalle != null
                && usuarioLogueado != null
                && eventoDetalle.getUsuario() != null
                && usuarioLogueado.getIdUsuario() == eventoDetalle.getUsuario().getIdUsuario();
    }

    public String obtenerNombreClienteSesion() {
        return usuarioLogueado != null ? usuarioLogueado.getNombre() : "";
    }

    private void mostrarAdvertencia(String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", detalle));
    }

    private void mostrarError(String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumen, detalle));
    }

    public List<Evento> getEventosDisponibles() {
        return eventosDisponibles;
    }

    public List<Evento> getEventosDelUsuario() {
        return eventosDelUsuario;
    }

    public Evento getEventoDetalle() {
        return eventoDetalle;
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }
}
