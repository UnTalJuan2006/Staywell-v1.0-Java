package Controlador;

import DAO.HabitacionDAO;
import DAO.ReservaDAO;
import DAO.UsuarioDAO;
import Modelo.EnumEstadoReserva;
import Modelo.Habitacion;
import Modelo.Reserva;
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
public class ReservaBean implements Serializable {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private Reserva reserva = new Reserva();
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Habitacion> listaHabitaciones = new ArrayList<>();
    private List<Usuario> listaUsuarios = new ArrayList<>();

    private Integer habitacionIdSeleccionada;
    private Integer usuarioIdSeleccionado;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @PostConstruct
    public void init() {
        try {
            listaHabitaciones = habitacionDAO.listar();
        } catch (SQLException e) {
            listaHabitaciones = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las habitaciones."));
        }

        try {
            listaUsuarios = usuarioDAO.listar();
        } catch (SQLException e) {
            listaUsuarios = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los usuarios."));
        }

        cargarReservas();
    }

    public void cargarReservas() {
        try {
            listaReservas = reservaDAO.listar();
        } catch (SQLException e) {
            listaReservas = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las reservas."));
        }
    }

    public void cargarReservaPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Reserva reservaEncontrada = reservaDAO.buscar(id);
                if (reservaEncontrada != null) {
                    this.reserva = reservaEncontrada;
                    habitacionIdSeleccionada = (reserva.getHabitacion() != null)
                            ? reserva.getHabitacion().getIdHabitacion() : null;
                    usuarioIdSeleccionado = (reserva.getUsuario() != null)
                            ? reserva.getUsuario().getIdUsuario() : null;
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "La reserva no existe."));
                }
            } catch (NumberFormatException | SQLException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar la reserva."));
            }
        }
    }

    public String guardar() {
        try {
            if (!asignarRelaciones()) {
                return null;
            }

            if (reserva.getFechaReserva() == null) {
                reserva.setFechaReserva(LocalDateTime.now());
            }

            reservaDAO.agregarReserva(reserva);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva registrada correctamente."));
            limpiarFormulario();
            return "Reservas?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la reserva."));
            return null;
        }
    }

    public String actualizar() {
        try {
            if (!asignarRelaciones()) {
                return null;
            }

            reservaDAO.actualizar(reserva);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva actualizada correctamente."));
            return "Reservas?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la reserva."));
            return null;
        }
    }

    public void eliminar(Reserva reservaSeleccionada) {
        try {
            reservaDAO.eliminar(reservaSeleccionada.getIdReserva());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva eliminada correctamente."));
            cargarReservas();
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la reserva."));
        }
    }

    private boolean asignarRelaciones() {
        Habitacion habitacion = obtenerHabitacionPorId(habitacionIdSeleccionada);
        Usuario usuario = obtenerUsuarioPorId(usuarioIdSeleccionado);

        if (habitacion == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar una habitación."));
            return false;
        }

        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar un huésped."));
            return false;
        }

        reserva.setHabitacion(habitacion);
        reserva.setUsuario(usuario);

        if (reserva.getNombreCliente() == null || reserva.getNombreCliente().isEmpty()) {
            reserva.setNombreCliente(usuario.getNombre());
        }

        if (reserva.getEmail() == null || reserva.getEmail().isEmpty()) {
            reserva.setEmail(usuario.getEmail());
        }

        if (reserva.getTelefono() == null || reserva.getTelefono().isEmpty()) {
            reserva.setTelefono(usuario.getTelefono());
        }
        return true;
    }

    private Habitacion obtenerHabitacionPorId(Integer id) {
        if (id == null) {
            return null;
        }

        return listaHabitaciones.stream()
                .filter(h -> h.getIdHabitacion() == id)
                .findFirst()
                .orElseGet(() -> {
                    Habitacion habitacion = new Habitacion();
                    habitacion.setIdHabitacion(id);
                    return habitacion;
                });
    }

    private Usuario obtenerUsuarioPorId(Integer id) {
        if (id == null) {
            return null;
        }

        return listaUsuarios.stream()
                .filter(u -> u.getIdUsuario() == id)
                .findFirst()
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(id);
                    return usuario;
                });
    }

    private void limpiarFormulario() {
        reserva = new Reserva();
        habitacionIdSeleccionada = null;
        usuarioIdSeleccionado = null;
    }

    public String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(DISPLAY_FORMATTER) : "";
    }

    public String obtenerNombreHabitacion(Reserva reserva) {
        if (reserva == null || reserva.getHabitacion() == null) {
            return "Sin asignar";
        }

        return "Habitación " + reserva.getHabitacion().getNumHabitacion();
    }

    public EnumEstadoReserva[] getEstados() {
        return EnumEstadoReserva.values();
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public List<Reserva> getListaReservas() {
        return listaReservas;
    }

    public List<Habitacion> getListaHabitaciones() {
        return listaHabitaciones;
    }

    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public Integer getHabitacionIdSeleccionada() {
        return habitacionIdSeleccionada;
    }

    public void setHabitacionIdSeleccionada(Integer habitacionIdSeleccionada) {
        this.habitacionIdSeleccionada = habitacionIdSeleccionada;
    }

    public Integer getUsuarioIdSeleccionado() {
        return usuarioIdSeleccionado;
    }

    public void setUsuarioIdSeleccionado(Integer usuarioIdSeleccionado) {
        this.usuarioIdSeleccionado = usuarioIdSeleccionado;
    }
}
