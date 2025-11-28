package Controlador;

import DAO.PagoDAO;
import Modelo.Pago;
import Modelo.Usuario;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class PagoAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PagoDAO pagoDAO = new PagoDAO();
    private List<Pago> pagos;
    private List<Pago> pagosFiltrados;
    private Integer filtroIdPago;
    private Integer filtroIdReserva;
    private Integer filtroHuespedId;
    private BigDecimal totalPagadoHuesped = BigDecimal.ZERO;

    @PostConstruct
    public void init() {
        cargarPagos();
        aplicarFiltros();
    }

    private void cargarPagos() {
        try {
            pagos = pagoDAO.listar();
            if (pagos == null) {
                pagos = new ArrayList<>();
            }
            pagosFiltrados = new ArrayList<>(pagos);
        } catch (SQLException e) {
            pagos = new ArrayList<>();
            e.printStackTrace();
        }
    }

    public List<Pago> getPagos() {
        if (pagos == null) {
            pagos = new ArrayList<>();
        }
        return pagos;
    }

    public List<Pago> getPagosFiltrados() {
        if (pagosFiltrados == null) {
            pagosFiltrados = new ArrayList<>(getPagos());
        }
        return pagosFiltrados;
    }

    public Integer getFiltroIdPago() {
        return filtroIdPago;
    }

    public void setFiltroIdPago(Integer filtroIdPago) {
        this.filtroIdPago = filtroIdPago;
    }

    public Integer getFiltroIdReserva() {
        return filtroIdReserva;
    }

    public void setFiltroIdReserva(Integer filtroIdReserva) {
        this.filtroIdReserva = filtroIdReserva;
    }

    public Integer getFiltroHuespedId() {
        return filtroHuespedId;
    }

    public void setFiltroHuespedId(Integer filtroHuespedId) {
        this.filtroHuespedId = filtroHuespedId;
    }

    public BigDecimal getTotalPagadoHuesped() {
        return totalPagadoHuesped;
    }

    public List<Usuario> getHuespedesDisponibles() {
        Map<Integer, Usuario> mapaHuespedes = new LinkedHashMap<>();

        for (Pago pago : getPagos()) {
            if (pago.getReserva() != null && pago.getReserva().getUsuario() != null) {
                Usuario usuario = pago.getReserva().getUsuario();
                if (usuario.getIdUsuario() != 0 && !mapaHuespedes.containsKey(usuario.getIdUsuario())) {
                    mapaHuespedes.put(usuario.getIdUsuario(), usuario);
                }
            }
        }

        return new ArrayList<>(mapaHuespedes.values());
    }

    public void aplicarFiltros() {
        List<Pago> listaBase = getPagos();
        List<Pago> filtrados = new ArrayList<>();

        for (Pago pago : listaBase) {
            boolean coincide = true;

            if (filtroIdPago != null && !filtroIdPago.equals(pago.getIdPago())) {
                coincide = false;
            }

            if (coincide && filtroIdReserva != null) {
                if (pago.getReserva() == null || !filtroIdReserva.equals(pago.getReserva().getIdReserva())) {
                    coincide = false;
                }
            }

            if (coincide) {
                filtrados.add(pago);
            }
        }

        pagosFiltrados = filtrados;
    }

    public void calcularTotalHuesped() {
        if (filtroHuespedId == null) {
            totalPagadoHuesped = BigDecimal.ZERO;
            return;
        }

        BigDecimal acumulado = BigDecimal.ZERO;

        for (Pago pago : getPagos()) {
            if (pago.getReserva() != null && pago.getReserva().getUsuario() != null) {
                if (filtroHuespedId.equals(pago.getReserva().getUsuario().getIdUsuario())) {
                    if (pago.getMonto() != null) {
                        acumulado = acumulado.add(pago.getMonto());
                    }
                }
            }
        }

        totalPagadoHuesped = acumulado;
    }

    public void limpiarFiltros() {
        filtroIdPago = null;
        filtroIdReserva = null;
        filtroHuespedId = null;
        totalPagadoHuesped = BigDecimal.ZERO;
        pagosFiltrados = new ArrayList<>(getPagos());
    }
}
