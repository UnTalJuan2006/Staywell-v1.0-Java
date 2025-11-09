package Controlador;

import DAO.PagoDAO;
import DAO.ReservaDAO;
import Modelo.Pago;
import Modelo.Reserva;
import Modelo.EnumPago;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class PagoBean implements Serializable {

    private Pago pago;
    private PagoDAO pagoDAO;
    private Reserva reserva;
    private int idReserva;
    private Integer cuotas; // solo se usa cuando es crédito (no se guarda)

    @PostConstruct
    public void init() {
        pago = new Pago();
        pagoDAO = new PagoDAO();

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

    public String registrarPago() {
        try {
            pago.setFechaCreacion(LocalDateTime.now());

            int idGenerado = pagoDAO.agregarPago(pago);
            if (idGenerado > 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Pago realizado con éxito",
                                "Código de pago: " + idGenerado));
                return "MisReservas.xhtml?faces-redirect=true";
              
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "No se pudo registrar el pago",
                                "Intente nuevamente"));
            }
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error SQL al registrar el pago",
                            e.getMessage()));
        }
        return null;
    }                       

    public EnumPago[] getTiposTarjeta() {
        return EnumPago.values();
    }

    public boolean isCreditoSeleccionado() {
        return pago.getTipoTarjeta() == EnumPago.Credito;
    }

    private void limpiarFormulario() {
        cuotas = null;
        pago = new Pago();
        pago.setReserva(reserva);
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