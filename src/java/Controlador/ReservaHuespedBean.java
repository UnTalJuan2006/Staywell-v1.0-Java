package Controlador;

import DAO.HabitacionDAO;
import DAO.PagoDAO;
import DAO.ReservaDAO;
import DAO.TipoHabitacionDAO;
import Modelo.EnumEstadoReserva;
import Modelo.EnumPago;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.Pago;
import Modelo.TipoHabitacion;
import Modelo.Usuario;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
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
    private static final DateTimeFormatter HTML_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    private List<TipoHabitacion> tiposHabitacion = new ArrayList<>();
    private List<Habitacion> habitacionesDisponibles = new ArrayList<>();
    private String fechasOcupadasJson = "[]";

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

    private EnumPago tipoPagoSeleccionado;
    private String numeroTarjeta;
    private String titularTarjeta;
    private String fechaVencimientoTarjeta;
    private String codigoSeguridadTarjeta;
    private LocalDate fechaVencimientoTarjetaParseada;

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

        prepararNuevaReserva();
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
        setHabitacionSeleccionada(null);
        habitacionesDisponibles = new ArrayList<>();
        checkin = null;
        checkout = null;
        observaciones = null;
        numeroNoches = 0;
        totalReserva = BigDecimal.ZERO;
        precioPorNoche = BigDecimal.ZERO;
        fechasOcupadasJson = "[]";
        tipoPagoSeleccionado = null;
        numeroTarjeta = null;
        titularTarjeta = null;
        fechaVencimientoTarjeta = null;
        codigoSeguridadTarjeta = null;
        fechaVencimientoTarjetaParseada = null;
    }

    public void onTipoHabitacionChange() {
        actualizarHabitacionesDisponibles();
        actualizarPrecioPorNoche();
        recalcularResumen();
        fechasOcupadasJson = "[]";
    }

    public void onHabitacionChange() {
        actualizarFechasOcupadas();
        recalcularResumen();
    }

    public void onFechasChange() {
        recalcularResumen();
    }

    private void actualizarHabitacionesDisponibles() {
        habitacionesDisponibles = new ArrayList<>();
        setHabitacionSeleccionada(null);

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

    private void actualizarFechasOcupadas() {
        fechasOcupadasJson = "[]";

        if (habitacionSeleccionada == null) {
            return;
        }

        try {
            List<Reserva> ocupaciones = reservaDAO.listarOcupacionesHabitacion(habitacionSeleccionada, null);

            if (ocupaciones.isEmpty()) {
                return;
            }

            StringBuilder jsonBuilder = new StringBuilder("[");
            boolean first = true;

            for (Reserva ocupacion : ocupaciones) {
                LocalDateTime entrada = ocupacion.getCheckin();
                LocalDateTime salida = ocupacion.getCheckout();

                if (entrada == null || salida == null) {
                    continue;
                }

                if (!first) {
                    jsonBuilder.append(',');
                }

                jsonBuilder.append('{')
                        .append("\"from\":\"")
                        .append(entrada.truncatedTo(ChronoUnit.MINUTES).format(HTML_INPUT_FORMATTER))
                        .append("\",\"to\":\"")
                        .append(salida.truncatedTo(ChronoUnit.MINUTES).format(HTML_INPUT_FORMATTER))
                        .append("\"}");

                first = false;
            }

            jsonBuilder.append(']');
            fechasOcupadasJson = jsonBuilder.toString();
        } catch (SQLException ex) {
            fechasOcupadasJson = "[]";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudieron consultar las reservas de la habitación seleccionada."));
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

        if (!validarDatosPago(context)) {
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
        reserva.setCheckout(fechaSalida);
        reserva.setFechaReserva(LocalDateTime.now());

        try {
            int idGenerado = reservaDAO.reservaHuespd(reserva);

            if (idGenerado > 0) {
                reserva.setIdReserva(idGenerado);

                if (!registrarPagoParaReserva(context, reserva)) {
                    return null;
                }

                context.getExternalContext().getFlash().setKeepMessages(true);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                        "Reserva y pago confirmados correctamente."));

                prepararNuevaReserva();
                return "MisReservas.xhtml?faces-redirect=true";
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "No se pudo obtener el ID de la reserva creada."));
            }

        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo registrar la reserva."));
        }

        return null;
    }


    private boolean validarDatosPago(FacesContext context) {
        if (totalReserva == null || totalReserva.compareTo(BigDecimal.ZERO) <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Importe pendiente",
                    "Calcula el total de la reserva antes de continuar."));
            return false;
        }

        if (tipoPagoSeleccionado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Tipo de pago requerido",
                    "Selecciona el tipo de tarjeta."));
            return false;
        }

        String numeroNormalizado = numeroTarjeta != null ? numeroTarjeta.replaceAll("\\s+", "") : null;
        if (numeroNormalizado == null || numeroNormalizado.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Número requerido",
                    "Ingresa el número de la tarjeta."));
            return false;
        }

        if (!numeroNormalizado.matches("\\d{13,19}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Número inválido",
                    "El número de tarjeta debe contener entre 13 y 19 dígitos."));
            return false;
        }
        numeroTarjeta = numeroNormalizado;

        titularTarjeta = titularTarjeta != null ? titularTarjeta.trim() : null;
        if (titularTarjeta == null || titularTarjeta.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Titular requerido",
                    "Ingresa el nombre del titular de la tarjeta."));
            return false;
        }

        fechaVencimientoTarjeta = fechaVencimientoTarjeta != null ? fechaVencimientoTarjeta.trim() : null;
        if (isNullOrTrimmedEmpty(fechaVencimientoTarjeta)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vencimiento requerido",
                    "Selecciona la fecha de vencimiento de la tarjeta."));
            return false;
        }

        try {
            YearMonth yearMonth = YearMonth.parse(fechaVencimientoTarjeta);
            fechaVencimientoTarjetaParseada = yearMonth.atEndOfMonth();
        } catch (DateTimeParseException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida",
                    "La fecha de vencimiento no tiene un formato válido."));
            return false;
        }

        codigoSeguridadTarjeta = codigoSeguridadTarjeta != null ? codigoSeguridadTarjeta.trim() : null;
        if (isNullOrTrimmedEmpty(codigoSeguridadTarjeta)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Código requerido",
                    "Ingresa el código de seguridad."));
            return false;
        }

        if (!codigoSeguridadTarjeta.matches("\\d{3,4}")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Código inválido",
                    "El código de seguridad debe ser numérico (3 o 4 dígitos)."));
            return false;
        }

        return true;
    }

    private boolean isNullOrTrimmedEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean registrarPagoParaReserva(FacesContext context, Reserva reserva) {
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMonto(totalReserva);
        pago.setTipoTarjeta(tipoPagoSeleccionado);
        pago.setNumeroTarjeta(numeroTarjeta);
        pago.setTitular(titularTarjeta);
        pago.setFechaVencimiento(fechaVencimientoTarjetaParseada);
        pago.setCodigoSeguridad(codigoSeguridadTarjeta);
        pago.setFechaCreacion(LocalDateTime.now());

        try {
            int idPago = pagoDAO.agregarPago(pago);
            if (idPago <= 0) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pago no registrado",
                        "No se pudo guardar el pago asociado a la reserva."));
                intentarRevertirReserva(reserva.getIdReserva());
                return false;
            }
        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar el pago",
                    "No se pudo almacenar el pago de la reserva."));
            intentarRevertirReserva(reserva.getIdReserva());
            return false;
        }

        return true;
    }

    private void intentarRevertirReserva(int idReserva) {
        try {
            reservaDAO.eliminar(idReserva);
        } catch (SQLException ex) {
            System.err.println("No se pudo revertir la reserva con ID " + idReserva + ": " + ex.getMessage());
        }
    }

    public EnumPago[] getTiposPago() {
        return EnumPago.values();
    }

    public String getResumenMetodoPago() {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return "Aún no has ingresado los datos de la tarjeta.";
        }

        String digitos = numeroTarjeta.replaceAll("\\D", "");
        if (digitos.length() >= 4) {
            String ultimos = digitos.substring(digitos.length() - 4);
            String titularNormalizado = titularTarjeta != null ? titularTarjeta.trim() : null;
            return "Tarjeta terminada en " + ultimos + (titularNormalizado != null && !titularNormalizado.isEmpty()
                    ? " a nombre de " + titularNormalizado : "");
        }

        return "Datos de tarjeta registrados.";
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
        actualizarFechasOcupadas();
    }

    public Date getCheckin() {
        return checkin;
    }

    public void setCheckin(Date checkin) {
        this.checkin = checkin;
        recalcularResumen();
    }

    public Date getCheckout() {
        return checkout;
    }

    public void setCheckout(Date checkout) {
        this.checkout = checkout;
        recalcularResumen();
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

    public String getFechasOcupadasJson() {
        return fechasOcupadasJson;
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public EnumPago getTipoPagoSeleccionado() {
        return tipoPagoSeleccionado;
    }

    public void setTipoPagoSeleccionado(EnumPago tipoPagoSeleccionado) {
        this.tipoPagoSeleccionado = tipoPagoSeleccionado;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTitularTarjeta() {
        return titularTarjeta;
    }

    public void setTitularTarjeta(String titularTarjeta) {
        this.titularTarjeta = titularTarjeta;
    }

    public String getFechaVencimientoTarjeta() {
        return fechaVencimientoTarjeta;
    }

    public void setFechaVencimientoTarjeta(String fechaVencimientoTarjeta) {
        this.fechaVencimientoTarjeta = fechaVencimientoTarjeta;
    }

    public String getCodigoSeguridadTarjeta() {
        return codigoSeguridadTarjeta;
    }

    public void setCodigoSeguridadTarjeta(String codigoSeguridadTarjeta) {
        this.codigoSeguridadTarjeta = codigoSeguridadTarjeta;
    }
}
