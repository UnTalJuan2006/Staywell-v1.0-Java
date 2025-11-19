package Controlador;

import DAO.EspacioDAO;
import DAO.EventoDAO;
import DAO.PagoDAO;
import Modelo.Espacio;
import Modelo.Evento;
import Modelo.EnumPago;
import Modelo.EnumEstadoEvento;
import Modelo.Pago;
import Modelo.Usuario;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class EventoHuespedBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EspacioDAO espacioDAO = new EspacioDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    private List<Espacio> espacios = new ArrayList<>();
    private String fechasOcupadasJson = "[]";

    private Integer espacioSeleccionado;
    private String nombreEvento;
    private String descripcion;
    private Date fechaEvento;              // fecha (un solo día)
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreCliente;

    private BigDecimal precioDia = BigDecimal.ZERO;
    private BigDecimal totalEvento = BigDecimal.ZERO;

    private Usuario usuarioLogueado;

    // --- Pago similar a reservas
    private EnumPago tipoPagoSeleccionado;
    private String numeroTarjeta;
    private String titularTarjeta;
    private String fechaVencimientoTarjeta;
    private String codigoSeguridadTarjeta;
    private java.time.LocalDate fechaVencimientoTarjetaParseada;

    @PostConstruct
    public void init() {
        cargarEspacios();
        usuarioLogueado = (Usuario) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("usuarioLogueado");

        if (usuarioLogueado != null) {
            nombreCliente = usuarioLogueado.getNombre();
        }

        prepararNuevoEvento();
        aplicarEspacioPreseleccionado();
    }

    // -------------------------
    // Carga y preparación
    // -------------------------
    private void cargarEspacios() {
        try {
            espacios = espacioDAO.listar();
        } catch (SQLException ex) {
            espacios = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error cargando espacios", ex.getMessage()));
            System.err.println("Error cargarEspacios(): " + ex.getMessage());
        }
    }

    public void prepararNuevoEvento() {
        espacioSeleccionado = null;
        nombreEvento = null;
        descripcion = null;
        fechaEvento = null;
        horaInicio = null;
        horaFin = null;
        nombreCliente = usuarioLogueado != null ? usuarioLogueado.getNombre() : null;
        precioDia = BigDecimal.ZERO;
        totalEvento = BigDecimal.ZERO;
        fechasOcupadasJson = "[]";
        tipoPagoSeleccionado = null;
        numeroTarjeta = null;
        titularTarjeta = null;
        fechaVencimientoTarjeta = null;
        codigoSeguridadTarjeta = null;
        fechaVencimientoTarjetaParseada = null;
    }

    private void aplicarEspacioPreseleccionado() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }

        String idEspacioParam = context.getExternalContext()
                .getRequestParameterMap().get("idEspacio");

        if (idEspacioParam == null || idEspacioParam.isEmpty()) {
            return;
        }

        try {
            int idEspacio = Integer.parseInt(idEspacioParam);
            if (idEspacio > 0) {
                setEspacioSeleccionado(idEspacio);
                recalcularTotal();
            }
        } catch (NumberFormatException ex) {
            System.err.println("Id de espacio inválido: " + idEspacioParam);
        }
    }

    // -------------------------
    // UI: cambios y cálculos
    // -------------------------
    public void onEspacioChange() {
        actualizarPrecioPorEspacio();
        actualizarFechasOcupadas();
        recalcularTotal();
    }

    public void onFechasChange() {
        recalcularTotal();
    }

    /**
     * Versión corregida: sin reflexión, sin catch vacío.
     * Asume que Espacio.getCostoHora() devuelve double (ajusta si tu modelo difiere).
     */
    private void actualizarPrecioPorEspacio() {
        precioDia = BigDecimal.ZERO;
        if (espacioSeleccionado == null) return;

        // 1) Buscar en la lista cargada
        Optional<Espacio> opt = espacios.stream()
                .filter(e -> e.getIdEspacio() == espacioSeleccionado)
                .findFirst();

        if (opt.isPresent()) {
            Espacio esp = opt.get();
            try {
                precioDia = BigDecimal.valueOf(esp.getCostoHora());
            } catch (Exception ex) {
                precioDia = BigDecimal.ZERO;
                System.err.println("Error parsing costoHora desde lista: " + ex.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Precio no disponible",
                                "No se pudo determinar el precio del espacio."));
            }
            return;
        }

        // 2) Si no está en la lista, cargar desde DAO
        Espacio e = espacioDAO.buscar(espacioSeleccionado);
        if (e != null) {
            precioDia = BigDecimal.valueOf(e.getCostoHora());
        } else {
            precioDia = BigDecimal.ZERO;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Espacio no encontrado",
                            "No se encontró información del espacio seleccionado."));
        }
    }

    private void recalcularTotal() {
        if (precioDia == null) {
            precioDia = BigDecimal.ZERO;
        }

        if (espacioSeleccionado == null || precioDia.compareTo(BigDecimal.ZERO) <= 0) {
            totalEvento = BigDecimal.ZERO;
            return;
        }

        BigDecimal totalCalculado = precioDia;

        if (horaInicio != null && horaFin != null && horaFin.isAfter(horaInicio)) {
            long minutos = Duration.between(horaInicio, horaFin).toMinutes();
            if (minutos > 0) {
                BigDecimal horas = BigDecimal.valueOf(minutos)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                if (horas.compareTo(BigDecimal.ONE) < 0) {
                    horas = BigDecimal.ONE;
                }

                totalCalculado = precioDia.multiply(horas);
            }
        }

        totalEvento = totalCalculado.setScale(2, RoundingMode.HALF_UP);
    }

    // -------------------------
    // Fechas ocupadas (JSON)
    // -------------------------
    private void actualizarFechasOcupadas() {
        fechasOcupadasJson = "[]";

        if (espacioSeleccionado == null) return;

        try {
            List<Evento> ocupaciones = eventoDAO.listarOcupacionesEspacio(espacioSeleccionado, null);

            if (ocupaciones == null || ocupaciones.isEmpty()) {
                fechasOcupadasJson = "[]";
                return;
            }

            StringBuilder jsonBuilder = new StringBuilder("[");
            boolean first = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (Evento ev : ocupaciones) {
                if (ev.getFechaEvento() == null) continue;
                String fecha = sdf.format(ev.getFechaEvento());
                if (!first) jsonBuilder.append(',');
                jsonBuilder.append("{")
                        .append("\"from\":\"").append(fecha).append("\",")
                        .append("\"to\":\"").append(fecha).append("\"")
                        .append("}");
                first = false;
            }
            jsonBuilder.append("]");
            fechasOcupadasJson = jsonBuilder.toString();
        } catch (SQLException ex) {
            fechasOcupadasJson = "[]";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo consultar la disponibilidad del espacio: " + ex.getMessage()));
            System.err.println("Error actualizarFechasOcupadas(): " + ex.getMessage());
        }
    }

    // -------------------------
    // Registro de evento + pago
    // -------------------------
    public String confirmarEvento() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (usuarioLogueado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Debe iniciar sesión para crear un evento."));
            return null;
        }

        if (espacioSeleccionado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Seleccione un espacio."));
            return null;
        }

        if (fechaEvento == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Seleccione la fecha del evento."));
            return null;
        }

        // Validar horas
        if (horaInicio == null || horaFin == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Debe seleccionar hora de inicio y hora de fin."));
            return null;
        }

        if (!horaFin.isAfter(horaInicio)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "La hora de fin debe ser posterior a la hora de inicio."));
            return null;
        }

        if (!validarDatosPago(context)) {
            return null;
        }

        // Construir Evento
        Evento evento = new Evento();
        evento.setNombreEvento(nombreEvento);
        evento.setDescripcion(descripcion);
        evento.setFechaEvento(fechaEvento);
        evento.setHoraInicio(horaInicio);
        evento.setHoraFin(horaFin);
        evento.setNombreCliente(nombreCliente != null ? nombreCliente : usuarioLogueado.getNombre());
        evento.setUsuario(usuarioLogueado);
        evento.setFechaCreacion(LocalDateTime.now());
        evento.setFechaActualizacion(LocalDateTime.now());
        evento.setEstado(EnumEstadoEvento.Activa);

        // asociación espacio (manejo de errores visible)
        Espacio esp = espacioDAO.buscar(espacioSeleccionado);
        if (esp == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "El espacio seleccionado no existe."));
            return null;
        }
        evento.setEspacio(esp);

        // Guardar evento y luego pago; si pago falla, revertir evento
        try {
            int idGenerado = eventoDAO.agregarEvento(evento);
            if (idGenerado <= 0) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "No se pudo crear el evento."));
                return null;
            }
            evento.setIdEvento(idGenerado);

            // Registrar pago
            if (!registrarPagoParaEvento(context, evento)) {
                // si falla el pago, intento eliminar evento
                try {
                    eventoDAO.eliminar(evento.getIdEvento());
                } catch (SQLException ex) {
                    System.err.println("No se pudo revertir evento (id " + evento.getIdEvento() + "): " + ex.getMessage());
                }
                return null;
            }

            context.getExternalContext().getFlash().setKeepMessages(true);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Evento y pago registrados correctamente."));
            prepararNuevoEvento();
            return "MisEventos.xhtml?faces-redirect=true";

        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo registrar el evento: " + ex.getMessage()));
            System.err.println("Error agregarEvento(): " + ex.getMessage());
            return null;
        }
    }

    private boolean registrarPagoParaEvento(FacesContext context, Evento evento) {
        Pago pago = new Pago();
        pago.setEvento(evento);
        pago.setMonto(totalEvento != null ? totalEvento : BigDecimal.ZERO);
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
                        "No se pudo guardar el pago asociado al evento."));
                return false;
            }
            return true;
        } catch (SQLException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar el pago",
                    "No se pudo almacenar el pago del evento: " + ex.getMessage()));
            System.err.println("Error agregarPago() evento: " + ex.getMessage());
            return false;
        }
    }

    // -------------------------
    // Validaciones de pago
    // -------------------------
    private boolean validarDatosPago(FacesContext context) {
        if (totalEvento == null || totalEvento.compareTo(BigDecimal.ZERO) <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Importe pendiente",
                    "Calcula el total del evento antes de continuar."));
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
        if (fechaVencimientoTarjeta == null || fechaVencimientoTarjeta.isEmpty()) {
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
        if (codigoSeguridadTarjeta == null || codigoSeguridadTarjeta.isEmpty()) {
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

    // -------------------------
    // Utilitarios & getters/setters
    // -------------------------
    private boolean isNullOrTrimmedEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Getters / Setters (incluyo los más importantes)
    public List<Espacio> getEspacios() {
        return espacios;
    }

    public String getFechasOcupadasJson() {
        return fechasOcupadasJson;
    }

    public Integer getEspacioSeleccionado() {
        return espacioSeleccionado;
    }

    public void setEspacioSeleccionado(Integer espacioSeleccionado) {
        this.espacioSeleccionado = espacioSeleccionado;
        actualizarPrecioPorEspacio();
        actualizarFechasOcupadas();
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Date fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public BigDecimal getPrecioDia() {
        return precioDia;
    }

    public BigDecimal getTotalEvento() {
        return totalEvento;
    }

    public String getNombreEspacioSeleccionado() {
        if (espacioSeleccionado == null) {
            return null;
        }

        return espacios.stream()
                .filter(e -> e.getIdEspacio() == espacioSeleccionado)
                .map(Espacio::getNombre)
                .findFirst()
                .orElse(null);
    }

    public EnumPago[] getTiposPago() {
        return EnumPago.values();
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

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }
}
