package Controlador;

import DAO.PagoDAO;
import Modelo.Pago;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class PagoAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PagoDAO pagoDAO = new PagoDAO();
    private List<Pago> pagos;

    @PostConstruct
    public void init() {
        cargarPagos();
    }

    private void cargarPagos() {
        try {
            pagos = pagoDAO.listar();
            if (pagos == null) {
                pagos = new ArrayList<>();
            }
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
}
