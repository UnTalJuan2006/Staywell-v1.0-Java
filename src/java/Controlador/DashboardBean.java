package Controlador;

import DAO.EventoDAO;
import DAO.HabitacionDAO;
import DAO.ReservaDAO;
import DAO.UsuarioDAO;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MESES_A_MOSTRAR = 12;
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private List<String> etiquetasMeses = new ArrayList<>();
    private List<Integer> reservasHabitaciones = new ArrayList<>();
    private List<Integer> reservasEventos = new ArrayList<>();
    private List<Integer> ocupacionMensual = new ArrayList<>();

    private int totalUsuarios;
    private int eventosActivos;
    private int habitacionesDisponibles;

    @PostConstruct
    public void init() {
        LocalDate fechaInicio = LocalDate.now().minusMonths(MESES_A_MOSTRAR - 1).withDayOfMonth(1);
        etiquetasMeses = construirEtiquetas(fechaInicio);

        try {
            totalUsuarios = usuarioDAO.contarUsuarios();
            eventosActivos = eventoDAO.contarEventosActivos();
            habitacionesDisponibles = habitacionDAO.contarDisponibles();

            Map<String, Integer> reservasPorMes = reservaDAO.obtenerReservasPorMes(fechaInicio);
            Map<String, Integer> eventosPorMes = eventoDAO.obtenerEventosPorMes(fechaInicio);
            Map<String, Integer> ocupacionPorMes = reservaDAO.obtenerOcupacionPorMes(fechaInicio);

            reservasHabitaciones = alinearDatos(reservasPorMes);
            reservasEventos = alinearDatos(eventosPorMes);
            ocupacionMensual = alinearDatos(ocupacionPorMes);
        } catch (SQLException e) {
            totalUsuarios = 0;
            eventosActivos = 0;
            habitacionesDisponibles = 0;
            reservasHabitaciones = new ArrayList<>();
            reservasEventos = new ArrayList<>();
            ocupacionMensual = new ArrayList<>();
        }
    }

    private List<String> construirEtiquetas(LocalDate fechaInicio) {
        List<String> etiquetas = new ArrayList<>();
        for (int i = 0; i < MESES_A_MOSTRAR; i++) {
            etiquetas.add(fechaInicio.plusMonths(i).format(LABEL_FORMATTER));
        }
        return etiquetas;
    }

    private List<Integer> alinearDatos(Map<String, Integer> datos) {
        List<Integer> serie = new ArrayList<>();
        for (String etiqueta : etiquetasMeses) {
            serie.add(datos.getOrDefault(etiqueta, 0));
        }
        return serie;
    }

    private String toJsonArray(List<?> valores) {
        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < valores.size(); i++) {
            Object valor = valores.get(i);

            if (valor == null) {
                sb.append("null");
            } else if (valor instanceof String) {
                sb.append("\"").append(valor.toString().replace("\"", "\\\"")).append("\"");
            } else {
                sb.append(valor);
            }

            if (i < valores.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public String getEtiquetasJson() {
        return toJsonArray(etiquetasMeses);
    }

    public String getReservasHabitacionesJson() {
        return toJsonArray(reservasHabitaciones);
    }

    public String getReservasEventosJson() {
        return toJsonArray(reservasEventos);
    }

    public String getOcupacionMensualJson() {
        return toJsonArray(ocupacionMensual);
    }

    public int getTotalUsuarios() {
        return totalUsuarios;
    }

    public int getEventosActivos() {
        return eventosActivos;
    }

    public int getHabitacionesDisponibles() {
        return habitacionesDisponibles;
    }
}
