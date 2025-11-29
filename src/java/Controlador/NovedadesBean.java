package Controlador;

import DAO.EspacioDAO;
import DAO.HabitacionDAO;
import DAO.NovedadesDAO;
import Modelo.Espacio;
import Modelo.EnumEstadoNovedad;
import Modelo.Habitacion;
import Modelo.Novedades;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class NovedadesBean implements Serializable {

    private Novedades novedad;
    private List<Novedades> listaNovedades;
    private List<Habitacion> habitaciones;
    private List<Espacio> espacios;

    private Integer habitacionSeleccionada;
    private Integer espacioSeleccionado;

    private transient NovedadesDAO novedadesDAO;
    private transient HabitacionDAO habitacionDAO;
    private transient EspacioDAO espacioDAO;

    @PostConstruct
    public void init() {
        novedad = new Novedades();
        listaNovedades = new ArrayList<>();
        habitaciones = new ArrayList<>();
        espacios = new ArrayList<>();

        novedadesDAO = new NovedadesDAO();
        habitacionDAO = new HabitacionDAO();
        espacioDAO = new EspacioDAO();

        cargarListas();
    }

    public void cargarListas() {
        try {
            listaNovedades = novedadesDAO.listar();
            habitaciones = habitacionDAO.listar();
            espacios = espacioDAO.listar();
        } catch (SQLException e) {
            e.printStackTrace();
            listaNovedades = new ArrayList<>();
            habitaciones = new ArrayList<>();
            espacios = new ArrayList<>();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo cargar la información de novedades."));
        }
    }

    public void cargarNovedadPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Novedades novedadEncontrada = novedadesDAO.buscarPorId(id);
                if (novedadEncontrada != null) {
                    this.novedad = novedadEncontrada;

                    habitacionSeleccionada = novedad.getHabitacion() != null
                            ? novedad.getHabitacion().getIdHabitacion() : null;
                    espacioSeleccionado = novedad.getEspacio() != null
                            ? novedad.getEspacio().getIdEspacio() : null;

                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Advertencia", "La novedad no existe."));
                }
            } catch (NumberFormatException | SQLException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "No se pudo cargar la novedad."));
            }
        }
    }

    public String registrarHabitacion() {
        habitacionSeleccionada = habitacionSeleccionada != null && habitacionSeleccionada == 0 ? null : habitacionSeleccionada;
        espacioSeleccionado = null;

        if (habitacionSeleccionada == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Seleccione una habitación."));
            return null;
        }

        if (novedad.getFechaFin() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Debe indicar la fecha de finalización."));
            return null;
        }

        try {
            Habitacion h = new Habitacion();
            h.setIdHabitacion(habitacionSeleccionada);

            novedad.setHabitacion(h);
            novedad.setEspacio(null);
            novedad.setFechaRegistro(LocalDateTime.now());
            novedad.setEstado(EnumEstadoNovedad.ACTIVA);

            if (!validarFechaFinPosterior()) {
                return null;
            }

            novedadesDAO.agregar(novedad);
            limpiarFormulario();
            cargarListas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Novedad registrada para la habitación."));

            return "Novedades?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo registrar la novedad."));
            return null;
        }
    }

    public String registrarEspacio() {
        espacioSeleccionado = espacioSeleccionado != null && espacioSeleccionado == 0 ? null : espacioSeleccionado;
        habitacionSeleccionada = null;

        if (espacioSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Seleccione un espacio."));
            return null;
        }

        if (novedad.getFechaFin() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Debe indicar la fecha de finalización."));
            return null;
        }

        try {
            Espacio e = new Espacio();
            e.setIdEspacio(espacioSeleccionado);

            novedad.setEspacio(e);
            novedad.setHabitacion(null);
            novedad.setFechaRegistro(LocalDateTime.now());
            novedad.setEstado(EnumEstadoNovedad.ACTIVA);

            if (!validarFechaFinPosterior()) {
                return null;
            }

            novedadesDAO.agregar(novedad);
            limpiarFormulario();
            cargarListas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Novedad registrada para el espacio."));

            return "Novedades?faces-redirect=true";
        } catch (SQLException ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo registrar la novedad."));
            return null;
        }
    }

    public String actualizar() {
        try {
            if (!validarDestinoSeleccionado()) {
                return null;
            }

            if (habitacionSeleccionada != null) {
                Habitacion h = new Habitacion();
                h.setIdHabitacion(habitacionSeleccionada);
                novedad.setHabitacion(h);
                novedad.setEspacio(null);
            } else if (espacioSeleccionado != null) {
                Espacio e = new Espacio();
                e.setIdEspacio(espacioSeleccionado);
                novedad.setEspacio(e);
                novedad.setHabitacion(null);
            }

            if (novedad.getFechaRegistro() == null) {
                novedad.setFechaRegistro(LocalDateTime.now());
            }

            if (!validarFechaFinPosterior()) {
                return null;
            }

            novedadesDAO.actualizar(novedad);
            cargarListas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Novedad actualizada correctamente."));
            return "Novedades?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo actualizar la novedad."));
            return null;
        }
    }

    public String eliminar(Novedades n) {
        try {
            novedadesDAO.eliminar(n);
            cargarListas();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Eliminado", "Novedad eliminada correctamente."));
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo eliminar la novedad."));
        }

        return "Novedades?faces-redirect=true";
    }

    public EnumEstadoNovedad[] getEstados() {
        return EnumEstadoNovedad.values();
    }

    public boolean esDeHabitacion(Novedades n) {
        return n.getHabitacion() != null;
    }

    public String obtenerTipo(Novedades n) {
        return esDeHabitacion(n) ? "Habitación" : "Espacio";
    }

    public String obtenerDestino(Novedades n) {
        if (n.getHabitacion() != null) {
            return "Habitación " + n.getHabitacion().getNumHabitacion();
        }

        if (n.getEspacio() != null) {
            return n.getEspacio().getNombre();
        }

        return "Sin asignar";
    }

    private boolean validarFechaFinPosterior() {
        if (novedad.getFechaRegistro() != null && novedad.getFechaFin() != null
                && novedad.getFechaFin().isBefore(novedad.getFechaRegistro())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "La fecha de finalización no puede ser anterior a la fecha de registro."));
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        novedad = new Novedades();
        habitacionSeleccionada = null;
        espacioSeleccionado = null;
    }

    private boolean validarDestinoSeleccionado() {
        boolean habitacionAsignada = habitacionSeleccionada != null;
        boolean espacioAsignado = espacioSeleccionado != null;

        if (habitacionAsignada && espacioAsignado) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Seleccione únicamente habitación o espacio."));
            return false;
        }

        if (!habitacionAsignada && !espacioAsignado) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "Debe seleccionar una habitación o un espacio."));
            return false;
        }

        return true;
    }

    public Novedades getNovedad() {
        return novedad;
    }

    public void setNovedad(Novedades novedad) {
        this.novedad = novedad;
    }

    public List<Novedades> getListaNovedades() {
        return listaNovedades;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public List<Espacio> getEspacios() {
        return espacios;
    }

    public Integer getHabitacionSeleccionada() {
        return habitacionSeleccionada;
    }

    public void setHabitacionSeleccionada(Integer habitacionSeleccionada) {
        this.habitacionSeleccionada = habitacionSeleccionada;
    }

    public Integer getEspacioSeleccionado() {
        return espacioSeleccionado;
    }

    public void setEspacioSeleccionado(Integer espacioSeleccionado) {
        this.espacioSeleccionado = espacioSeleccionado;
    }
}
