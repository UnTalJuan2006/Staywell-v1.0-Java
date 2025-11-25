package Controlador;

import DAO.PagoDAO;
import DAO.ReservaDAO;
import Modelo.EnumPago;
import Modelo.Pago;
import Modelo.Reserva;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

@ManagedBean
@ViewScoped
public class PagoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Pago pago;
    private transient PagoDAO pagoDAO;
    private Reserva reserva;
    private int idReserva;
    private Integer cuotas; // solo se usa cuando es crédito (no se guarda)
    private Integer codigoPagoGenerado;
    private boolean pagoExitoso;
    private String mensajeExito;
    private String descripcionContexto;
    private String destinoRedireccion;
    private String etiquetaBotonDestino;
    private final LocalDate hoy = LocalDate.now();

    @PostConstruct
    public void init() {
        pago = new Pago();
        pagoDAO = new PagoDAO();
        pagoExitoso = false;
        codigoPagoGenerado = null;
        mensajeExito = null;
        pago.setMonto(BigDecimal.ZERO);
        descripcionContexto = "reserva";
        destinoRedireccion = "MisReservas.xhtml";
        etiquetaBotonDestino = "Ver mis reservas";

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idReservaParam = externalContext.getRequestParameterMap().get("idReserva");

        if (idReservaParam != null) {
            try {
                idReserva = Integer.parseInt(idReservaParam);
                ReservaDAO reservaDAO = new ReservaDAO();
                reserva = reservaDAO.buscar(idReserva);
            } catch (Exception e) {
                System.out.println("Error al cargar la reserva: " + e.getMessage());
            }
        }

        if (reserva != null) {
            pago.setReserva(reserva);
            configurarContextoReserva();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Contexto no disponible",
                            "No se pudo recuperar la información de la reserva."));
        }
    }

    public void registrarPago() {
        FacesContext context = FacesContext.getCurrentInstance();
        PrimeFaces primeFaces = PrimeFaces.current();
        if (primeFaces != null) {
            primeFaces.ajax().addCallbackParam("pagoExitoso", false);
            primeFaces.ajax().addCallbackParam("destino", null);
        }

        if (!validarFormulario(context)) {
            pagoExitoso = false;
            mensajeExito = null;
            codigoPagoGenerado = null;
            return;
        }

        try {
            pago.setFechaCreacion(LocalDateTime.now());
            if (pago.getMonto() == null) {
                pago.setMonto(BigDecimal.ZERO);
            }

            int idGenerado = getPagoDAO().agregarPago(pago);
            if (idGenerado > 0) {
                codigoPagoGenerado = idGenerado;
                pagoExitoso = true;
                mensajeExito = "¡Pago del " + descripcionContexto + " registrado exitosamente!";

                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Pago de " + descripcionContexto + " confirmado",
                                "Código de pago: " + idGenerado));

                if (primeFaces != null) {
                    primeFaces.ajax().addCallbackParam("pagoExitoso", true);
                    primeFaces.ajax().addCallbackParam("destino", construirUrlDestino(idGenerado));
                }
                propagarConfirmacion();
                limpiarFormulario();
            } else {
                pagoExitoso = false;
                mensajeExito = null;
                codigoPagoGenerado = null;
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "No se pudo registrar el pago",
                                "Intente nuevamente"));
            }
        } catch (SQLException e) {
            pagoExitoso = false;
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error SQL al registrar el pago",
                            e.getMessage()));
        }
    }

    public EnumPago[] getTiposTarjeta() {
        return EnumPago.values();
    }

    public boolean isCreditoSeleccionado() {
        return pago.getTipoTarjeta() == EnumPago.Credito;
    }

    public boolean isPagoExitoso() {
        return pagoExitoso;
    }

    public Integer getCodigoPagoGenerado() {
        return codigoPagoGenerado;
    }

    public String getMensajeExito() {
        return mensajeExito;
    }

    public String irMisReservas() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        if (mensajeExito != null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Pago confirmado",
                            mensajeExito + " Código: " + codigoPagoGenerado));
        }
        context.getExternalContext().getFlash().put("codigoPago", codigoPagoGenerado);
        context.getExternalContext().getFlash().put("mensajePago", mensajeExito);
        pagoExitoso = false;
        return destinoRedireccion + "?faces-redirect=true";
    }

    private void limpiarFormulario() {
        cuotas = null;
        pago = new Pago();
        pago.setReserva(reserva);
        pago.setMonto(BigDecimal.ZERO);
    }

    private void propagarConfirmacion() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        if (codigoPagoGenerado != null) {
            externalContext.getFlash().setKeepMessages(true);
            externalContext.getFlash().put("codigoPago", codigoPagoGenerado);
            externalContext.getFlash().put("mensajePago", mensajeExito);
        }
    }

    private String construirUrlDestino(int codigoPago) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String contextPath = externalContext.getRequestContextPath();
        StringBuilder destino = new StringBuilder();
        if (contextPath != null && !contextPath.isEmpty()) {
            destino.append(contextPath);
            if (!contextPath.endsWith("/")) {
                destino.append('/');
            }
        }
        destino.append(destinoRedireccion).append("?codigoPago=").append(codigoPago);
        return destino.toString();
    }

    private boolean validarFormulario(FacesContext context) {
        if (pago.getReserva() == null || pago.getReserva().getIdReserva() <= 0) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Contexto no válido",
                            "No se encontró la reserva asociada al pago."));
            return false;
        }

        LocalDate fechaVencimiento = pago.getFechaVencimiento();
        if (fechaVencimiento == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Vencimiento requerido",
                            "Seleccione la fecha de vencimiento de la tarjeta."));
            return false;
        }

        if (fechaVencimiento.isBefore(LocalDate.now())) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Vencimiento inválido",
                            "La tarjeta debe tener una fecha de vencimiento vigente."));
            return false;
        }

        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Monto inválido",
                            "Ingrese un monto mayor a cero."));
            return false;
        }

        if (pago.getTipoTarjeta() == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Tipo de tarjeta requerido",
                            "Seleccione un tipo de tarjeta válido."));
            return false;
        }

        if (pago.getNumeroTarjeta() == null || !pago.getNumeroTarjeta().matches("\\d{13,19}")) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Número de tarjeta inválido",
                            "Ingrese entre 13 y 19 dígitos numéricos."));
            return false;
        }

        if (pago.getTitular() == null || !pago.getTitular().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,60}")) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Titular inválido",
                            "Ingrese un nombre válido (solo letras y espacios)."));
            return false;
        }

        if (pago.getCodigoSeguridad() == null || !pago.getCodigoSeguridad().matches("\\d{3,4}")) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Código de seguridad inválido",
                            "Ingrese 3 o 4 dígitos numéricos."));
            return false;
        }

        if (isCreditoSeleccionado()) {
            if (cuotas == null || cuotas <= 0) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Cuotas requeridas",
                                "Ingrese el número de cuotas para pagos con crédito."));
                return false;
            }
        }

        return true;
    }

    private PagoDAO getPagoDAO() {
        if (pagoDAO == null) {
            pagoDAO = new PagoDAO();
        }
        return pagoDAO;
    }

    private void configurarContextoReserva() {
        descripcionContexto = "reserva";
        destinoRedireccion = "MisReservas.xhtml";
        etiquetaBotonDestino = "Ver mis reservas";
        if (pago.getMonto() == null) {
            pago.setMonto(BigDecimal.ZERO);
        }
    }

    // Getters y Setters
    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public Integer getCuotas() {
        return cuotas;
    }

    public void setCuotas(Integer cuotas) {
        this.cuotas = cuotas;
    }

    public boolean isContextoDisponible() {
        return reserva != null;
    }

    public String getDescripcionContexto() {
        return descripcionContexto;
    }

    public String getEtiquetaBotonDestino() {
        return etiquetaBotonDestino;
    }

    public LocalDate getHoy() {
        return hoy;
    }
}