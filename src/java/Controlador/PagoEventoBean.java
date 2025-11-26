package Controlador;

import DAO.EventoDAO;
import DAO.PagoEventoDAO;
import Modelo.EnumPago;
import Modelo.Evento;
import Modelo.PagoEvento;
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
public class PagoEventoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private PagoEvento pagoEvento;
    private transient PagoEventoDAO pagoEventoDAO;
    private Evento evento;
    private int idEvento;
    private Integer cuotas;
    private Integer codigoPagoGenerado;
    private boolean pagoExitoso;
    private String mensajeExito;
    private String descripcionContexto;
    private String destinoRedireccion;
    private String etiquetaBotonDestino;
    private final LocalDate hoy = LocalDate.now();

    @PostConstruct
    public void init() {
        pagoEvento = new PagoEvento();
        pagoEventoDAO = new PagoEventoDAO();
        pagoExitoso = false;
        codigoPagoGenerado = null;
        mensajeExito = null;
        pagoEvento.setMonto(BigDecimal.ZERO);
        descripcionContexto = "evento";
        destinoRedireccion = "MisEventos.xhtml";
        etiquetaBotonDestino = "Ver mis eventos";

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idEventoParam = externalContext.getRequestParameterMap().get("idEvento");

        if (idEventoParam != null) {
            try {
                idEvento = Integer.parseInt(idEventoParam);
                EventoDAO eventoDAO = new EventoDAO();
                evento = eventoDAO.buscar(idEvento);
            } catch (Exception e) {
                System.out.println("Error al cargar el evento: " + e.getMessage());
            }
        }

        if (evento != null) {
            pagoEvento.setEvento(evento);
            configurarContextoEvento();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Contexto no disponible",
                            "No se pudo recuperar la información del evento."));
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
            pagoEvento.setFechaCreacion(LocalDateTime.now());
            if (pagoEvento.getMonto() == null) {
                pagoEvento.setMonto(BigDecimal.ZERO);
            }

            int idGenerado = getPagoEventoDAO().agregarPagoEvento(pagoEvento);
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
        return pagoEvento.getTipoTarjeta() == EnumPago.Credito;
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

    public String irMisEventos() {
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
        pagoEvento = new PagoEvento();
        pagoEvento.setEvento(evento);
        pagoEvento.setMonto(BigDecimal.ZERO);
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
        if (pagoEvento.getEvento() == null || pagoEvento.getEvento().getIdEvento() <= 0) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Contexto no válido",
                            "No se encontró el evento asociado al pago."));
            return false;
        }

        LocalDate fechaVencimiento = pagoEvento.getFechaVencimiento();
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

        if (pagoEvento.getMonto() == null || pagoEvento.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Monto inválido",
                            "Ingrese un monto mayor a cero."));
            return false;
        }

        if (pagoEvento.getTipoTarjeta() == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Tipo de tarjeta requerido",
                            "Seleccione un tipo de tarjeta válido."));
            return false;
        }

        if (pagoEvento.getNumeroTarjeta() == null || !pagoEvento.getNumeroTarjeta().matches("\\d{13,19}")) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Número de tarjeta inválido",
                            "Ingrese entre 13 y 19 dígitos numéricos."));
            return false;
        }

        if (pagoEvento.getTitular() == null || !pagoEvento.getTitular().matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,60}")) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Titular inválido",
                            "Ingrese un nombre válido (solo letras y espacios)."));
            return false;
        }

        if (pagoEvento.getCodigoSeguridad() == null || !pagoEvento.getCodigoSeguridad().matches("\\d{3,4}")) {
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

    private PagoEventoDAO getPagoEventoDAO() {
        if (pagoEventoDAO == null) {
            pagoEventoDAO = new PagoEventoDAO();
        }
        return pagoEventoDAO;
    }

    private void configurarContextoEvento() {
        descripcionContexto = "evento";
        destinoRedireccion = "MisEventos.xhtml";
        etiquetaBotonDestino = "Ver mis eventos";
        if (pagoEvento.getMonto() == null) {
            pagoEvento.setMonto(BigDecimal.ZERO);
        }
    }

    // Getters y Setters
    public PagoEvento getPagoEvento() {
        return pagoEvento;
    }

    public void setPagoEvento(PagoEvento pagoEvento) {
        this.pagoEvento = pagoEvento;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getCuotas() {
        return cuotas;
    }

    public void setCuotas(Integer cuotas) {
        this.cuotas = cuotas;
    }

    public boolean isContextoDisponible() {
        return evento != null;
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
