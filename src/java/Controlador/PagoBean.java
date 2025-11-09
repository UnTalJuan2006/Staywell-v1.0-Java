package Controlador;

import DAO.PagoDAO;
import DAO.ReservaDAO;
import Modelo.EnumPago;
import Modelo.Pago;
import Modelo.Reserva;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
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

    @PostConstruct
    public void init() {
        pago = new Pago();
        pagoDAO = new PagoDAO();
        pagoExitoso = false;
        codigoPagoGenerado = null;
        mensajeExito = null;

        // Leer el idReserva desde los parámetros URL
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("idReserva");

        if (idParam != null) {
            try {
                idReserva = Integer.parseInt(idParam);
                ReservaDAO reservaDAO = new ReservaDAO();
                reserva = reservaDAO.buscar(idReserva);
                pago.setReserva(reserva);
          
            } catch (Exception e) {
                System.out.println("Error al cargar la reserva: " + e.getMessage());
            }
        }
    }

    public void registrarPago() {
        FacesContext context = FacesContext.getCurrentInstance();
        PrimeFaces primeFaces = PrimeFaces.current();
        if (primeFaces != null) {
            primeFaces.ajax().addCallbackParam("pagoExitoso", false);
        }

        if (!validarFormulario(context)) {
            pagoExitoso = false;
            mensajeExito = null;
            codigoPagoGenerado = null;
            return;
        }

        try {
            pago.setFechaCreacion(LocalDateTime.now());

            int idGenerado = getPagoDAO().agregarPago(pago);
            if (idGenerado > 0) {
                codigoPagoGenerado = idGenerado;
                pagoExitoso = true;
                mensajeExito = "¡Pago registrado exitosamente!";

                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Pago realizado con éxito",
                                "Código de pago: " + idGenerado));

                if (primeFaces != null) {
                    primeFaces.ajax().addCallbackParam("pagoExitoso", true);
                }
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
        pagoExitoso = false;
        return "MisReservas.xhtml?faces-redirect=true";
    }

    private void limpiarFormulario() {
        cuotas = null;
        pago = new Pago();
        pago.setReserva(reserva);
    }

    private boolean validarFormulario(FacesContext context) {
        if (pago.getReserva() == null || pago.getReserva().getIdReserva() <= 0) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Reserva no válida",
                            "No se encontró la reserva asociada al pago."));
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
}