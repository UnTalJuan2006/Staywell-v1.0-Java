package Controlador;

import DAO.PagoDAO;
import DAO.PagoEventoDAO;
import Modelo.Usuario;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class MisPagosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal totalReservas;
    private BigDecimal totalEventos;
    private BigDecimal totalGeneral;

    private transient PagoDAO pagoDAO;
    private transient PagoEventoDAO pagoEventoDAO;

    @PostConstruct
    public void init() {
        pagoDAO = new PagoDAO();
        pagoEventoDAO = new PagoEventoDAO();

        totalReservas = BigDecimal.ZERO;
        totalEventos = BigDecimal.ZERO;
        totalGeneral = BigDecimal.ZERO;

        Usuario usuarioLogueado = (Usuario) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("usuarioLogueado");

        if (usuarioLogueado != null) {
            cargarTotales(usuarioLogueado.getIdUsuario());
        }
    }

    private void cargarTotales(int idUsuario) {
        try {
            totalReservas = pagoDAO.obtenerTotalPagosPorUsuario(idUsuario);
            totalEventos = pagoEventoDAO.obtenerTotalPagosPorUsuario(idUsuario);
            totalGeneral = totalReservas.add(totalEventos);
        } catch (Exception e) {
            totalReservas = BigDecimal.ZERO;
            totalEventos = BigDecimal.ZERO;
            totalGeneral = BigDecimal.ZERO;
            System.err.println("Error al cargar los totales de pagos: " + e.getMessage());
        }
    }

    public BigDecimal getTotalReservas() {
        return totalReservas;
    }

    public BigDecimal getTotalEventos() {
        return totalEventos;
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }
}
