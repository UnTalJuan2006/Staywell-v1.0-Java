package Controlador;

import javax.annotation.PostConstruct;
import DAO.HabitacionDAO;
import Modelo.EnumEstadoHabitacion;
import Modelo.Habitacion;
import Modelo.TipoHabitacion;
import DAO.TipoHabitacionDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;


@ManagedBean
@ViewScoped
public class HabitacionBean {

    private Habitacion habitacion = new Habitacion();
    private HabitacionDAO habitacionDAO = new HabitacionDAO();

    private List<Habitacion> habitaciones;          // todas las habitaciones
    private List<Habitacion> habitacionesFiltradas; // las filtradas por tipo
    private List<TipoHabitacion> listaTipos;

    private TipoHabitacion tipoSeleccionado;

    @PostConstruct
    public void init() {
        try {
            TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();
            listaTipos = tipoDAO.listar();

            // Cargar todas las habitaciones una sola vez
            habitaciones = habitacionDAO.listar();
            habitacionesFiltradas = new ArrayList<>(habitaciones);

        } catch (SQLException e) {
            System.out.println("Error al cargar datos iniciales: " + e.getMessage());
            listaTipos = new ArrayList<>();
            habitaciones = new ArrayList<>();
            habitacionesFiltradas = new ArrayList<>();
        }
    }
    
        public List<Habitacion> getListaHabitaciones() {
        try {
            return habitacionDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar habitaciones");
            return null;
        }
    }

    // --- MÉTODO PARA FILTRAR ---
    public void filtrarHabitacionesPorTipo() {
        if (tipoSeleccionado == null) {
            habitacionesFiltradas = new ArrayList<>(habitaciones); // sin filtro
            return;
        }

        habitacionesFiltradas.clear();
        for (Habitacion h : habitaciones) {
            if (h.getTipoHabitacion() != null &&
                h.getTipoHabitacion().getIdTipoHabitacion() == tipoSeleccionado.getIdTipoHabitacion()) {
                habitacionesFiltradas.add(h);
            }
        }
    }

    // --- GETTERS Y SETTERS ---
    public List<Habitacion> getHabitacionesFiltradas() {
        return habitacionesFiltradas;
    }

    public List<TipoHabitacion> getListaTipos() {
        return listaTipos;
    }

    public TipoHabitacion getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(TipoHabitacion tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public EnumEstadoHabitacion[] getEstados() {
        return EnumEstadoHabitacion.values();
    }

    public String agregar() throws SQLException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (tipoSeleccionado == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Advertencia", "Debe seleccionar un tipo de habitación."));
            return null;
        }
        try {
            habitacion.setTipoHabitacion(tipoSeleccionado);
            habitacion.setEstado(EnumEstadoHabitacion.Disponible);
            habitacion.setFechaCreacion(LocalDateTime.now());
            habitacion.setFechaActualizacion(LocalDateTime.now());
            habitacionDAO.agregar(habitacion);

            // Refrescar la lista
            habitaciones = habitacionDAO.listar();
            habitacionesFiltradas = new ArrayList<>(habitaciones);

            habitacion = new Habitacion();
            tipoSeleccionado = null;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Habitación agregada correctamente."));
        } catch (SQLException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo registrar la habitación: " + e.getMessage()));
            return null;
        }
        return "Habitaciones?faces-redirect=true";
    }
}
