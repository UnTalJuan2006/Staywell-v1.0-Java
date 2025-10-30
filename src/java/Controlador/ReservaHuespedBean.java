package Controlador;

import DAO.HabitacionDAO;
import DAO.ReservaDAO;
import DAO.TipoHabitacionDAO;
import Modelo.EnumEstadoReserva;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import Modelo.Usuario;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class ReservaHuespedBean implements Serializable {

    private static final DateTimeFormatter RESUMEN_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    private List<TipoHabitacion> tiposHabitacion = new ArrayList<>();
    private List<Habitacion> habitacionesDisponibles = new ArrayList<>();

    private Integer tipoHabitacionSeleccionada;
    private Integer habitacionSeleccionada;

    private Date checkin;
    private Date checkout;

    private String observaciones;

    private String nombreCliente;
    private String email;
    private String telefono;

    private BigDecimal precioPorNoche = BigDecimal.ZERO;
    private BigDecimal totalReserva = BigDecimal.ZERO;
    private long numeroNoches = 0;

    private Usuario usuarioLogueado;

    @PostConstruct
    public void init() {
        cargarTiposHabitacion();
        usuarioLogueado = (Usuario) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("usuarioLogueado");

        if (usuarioLogueado != null) {
            nombreCliente = usuarioLogueado.getNombre();
            email = usuarioLogueado.getEmail();
            telefono = usuarioLogueado.getTelefono();
        }
    }

    private void cargarTiposHabitacion() {
        try {
            tiposHabitacion = tipoHabitacionDAO.listar();
        } catch (SQLException ex) {
            tiposHabitacion = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudieron cargar los tipos de habitación."));
        }
    }

    public void prepararNuevaReserva() {
        tipoHabitacionSeleccionada = null;
        habitacionSeleccionada = null;
        habitacionesDisponibles = new ArrayList<>();
        checkin = null;
        checkout = null;
        observaciones = null;
        numeroNoches = 0;
        totalReserva = BigDecimal.ZERO;
        precioPorNoche = BigDecimal.ZERO;
    }

    public void onTipoHabitacionChange() {
        actualizarHabitacionesDisponibles();
        actualizarPrecioPorNoche();
        recalcularResumen();
    }

    public void onHabitacionChange() {
        recalcularResumen();
    }

    public void onFechasChange() {
        recalcularResumen();
    }

    private void actualizarHabitacionesDisponibles() {
        habitacionesDisponibles = new ArrayList<>();
        habitacionSeleccionada = null;

        if (tipoHabitacionSeleccionada == null) {
            return;
        }

        try {
            habitacionesDisponibles = habitacionDAO.listarPorTipo(tipoHabitacionSeleccionada);
        } catch (SQLException ex) {
            habitacionesDisponibles = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudieron cargar las habitaciones."));
        }
    }

    private void actualizarPrecioPorNoche() {
        precioPorNoche = BigDecimal.ZERO;
        TipoHabitacion tipo = obtenerTipoSeleccionado();
        if (tipo != null) {
            precioPorNoche = BigDecimal.valueOf(tipo.getPrecio());
        }
    }

    private void recalcularResumen() {
        actualizarPrecioPorNoche();
        numeroNoches = 0;
        totalReserva = BigDecimal.ZERO;

        LocalDate fechaEntrada = convertirADia(checkin);
        LocalDate fechaSalida = convertirADia(checkout);

        if (fechaEntrada == null || fechaSalida == null) {
            return;
        }

        if (!fechaSalida.isAfter(fechaEntrada)) {
            return;
        }

        numeroNoches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        if (numeroNoches <= 0) {
            numeroNoches = 0;
            return;
        }

        if (precioPorNoche.compareTo(BigDecimal.ZERO) > 0) {
            totalReserva = precioPorNoche.multiply(BigDecimal.valueOf(numeroNoches));
        }
    }

    private LocalDate convertirADia(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime convertirAHoraExacta(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private TipoHabitacion obtenerTipoSeleccionado() {
        if (tipoHabitacionSeleccionada == null) {
            return null;
        }
        Optional<TipoHabitacion> seleccionado = tiposHabitacion.stream()
                .filter(t -> t.getIdTipoHabitacion() == tipoHabitacionSeleccionada)
                .findFirst();
        return seleccionado.orElse(null);
    }

    private Habitacion obtenerHabitacionSeleccionada() {
        if (habitacionSeleccionada == null) {
            return null;
        }

        Optional<Habitacion> seleccionada = habitacionesDisponibles.stream()
                .filter(h -> h.getIdHabitacion() == habitacionSeleccionada)
                .findFirst();

        if (seleccionada.isPresent()) {
            return seleccionada.get();
        }

        try {
            return habitacionDAO.buscarPorId(habitacionSeleccionada);
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo obtener la información de la habitación."));
            return null;
        }
    }

    public String confirmarReserva() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (usuarioLogueado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Debe iniciar sesión para realizar una reserva."));
            return null;
        }

        if (tipoHabitacionSeleccionada == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Seleccione un tipo de habitación."));
            return null;
        }

        Habitacion habitacion = obtenerHabitacionSeleccionada();
        if (habitacion == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Seleccione una habitación disponible."));
            return null;
        }

        LocalDateTime fechaEntrada = convertirAHoraExacta(checkin);
        LocalDateTime fechaSalida = convertirAHoraExacta(checkout);

        if (fechaEntrada == null || fechaSalida == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Debe seleccionar las fechas de entrada y salida."));
            return null;
        }

        if (!fechaSalida.isAfter(fechaEntrada)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "La fecha de salida debe ser posterior a la fecha de entrada."));
            return null;
        }

        try {
            if (!reservaDAO.habitacionDisponible(habitacion.getIdHabitacion(), fechaEntrada, fechaSalida, null)) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No disponible",
                        "La habitación no está disponible en el rango seleccionado."));
                return null;
            }
        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo verificar la disponibilidad de la habitación."));
            return null;
        }

        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setUsuario(usuarioLogueado);
        reserva.setEstado(EnumEstadoReserva.ACTIVA);
        reserva.setNombreCliente(nombreCliente != null ? nombreCliente : usuarioLogueado.getNombre());
        reserva.setEmail(email != null ? email : usuarioLogueado.getEmail());
        reserva.setTelefono(telefono != null ? telefono : usuarioLogueado.getTelefono());
        reserva.setObservaciones(observaciones);
        reserva.setCheckin(fechaEntrada);
        reserva.setCehckout(fechaSalida);
        reserva.setFechaReserva(LocalDateTime.now());

        try {
            reservaDAO.agregarReserva(reserva);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Reserva registrada correctamente."));
            prepararNuevaReserva();
        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo registrar la reserva."));
        }
        return null;
    }

    public String getResumenTipoHabitacion() {
        TipoHabitacion tipo = obtenerTipoSeleccionado();
        return tipo != null ? tipo.getNombre() : "Sin seleccionar";
    }

    public String getResumenHabitacion() {
        Habitacion habitacion = obtenerHabitacionSeleccionada();
        return habitacion != null ? "Habitación " + habitacion.getNumHabitacion() : "Sin seleccionar";
    }

    public String getResumenCheckin() {
        LocalDate fecha = convertirADia(checkin);
        return fecha != null ? RESUMEN_FORMATTER.format(fecha) : "--";
    }

    public String getResumenCheckout() {
        LocalDate fecha = convertirADia(checkout);
        return fecha != null ? RESUMEN_FORMATTER.format(fecha) : "--";
    }

    public List<TipoHabitacion> getTiposHabitacion() {
        return tiposHabitacion;
    }

    public List<Habitacion> getHabitacionesDisponibles() {
        return habitacionesDisponibles;
    }

    public Integer getTipoHabitacionSeleccionada() {
        return tipoHabitacionSeleccionada;
    }

    public void setTipoHabitacionSeleccionada(Integer tipoHabitacionSeleccionada) {
        this.tipoHabitacionSeleccionada = tipoHabitacionSeleccionada;
    }

    public Integer getHabitacionSeleccionada() {
        return habitacionSeleccionada;
    }

    public void setHabitacionSeleccionada(Integer habitacionSeleccionada) {
        this.habitacionSeleccionada = habitacionSeleccionada;
    }

    public Date getCheckin() {
        return checkin;
    }

    public void setCheckin(Date checkin) {
        this.checkin = checkin;
    }

    public Date getCheckout() {
        return checkout;
    }

    public void setCheckout(Date checkout) {
        this.checkout = checkout;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }

    public BigDecimal getTotalReserva() {
        return totalReserva;
    }

    public long getNumeroNoches() {
        return numeroNoches;
    }

    public boolean isHabitacionInputDeshabilitado() {
        return tipoHabitacionSeleccionada == null || habitacionesDisponibles.isEmpty();
    }

    public Date getCheckoutMinDate() {
        return checkin;
    }
}
