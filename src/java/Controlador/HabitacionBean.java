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

    private List<TipoHabitacion> listaTipos;
    private TipoHabitacion tipoSeleccionado;

    @PostConstruct
    public void init() {
        try {
            TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();
            listaTipos = tipoDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al cargar tipos de habitación: " + e.getMessage());
            listaTipos = new ArrayList<>();
        }
    }

    public EnumEstadoHabitacion[] getEstados() {
        return EnumEstadoHabitacion.values();
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
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

    public List<Habitacion> getListaHabitaciones() {
        try {
            return habitacionDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar habitaciones");
            return null;
        }
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

            habitacion = new Habitacion(); // limpiar formulario
            tipoSeleccionado = null; // Limpiar tipo seleccionado
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Habitación agregada correctamente."));

        } catch (SQLException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo registrar la habitación: " + e.getMessage()));
            return null;
        }

        return "Habitaciones?faces-redirect=true"; // redirige a la lista
    }


}
