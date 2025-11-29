package Controlador;

import DAO.EspacioDAO;
import DAO.HabitacionDAO;
import DAO.NovedadesDAO;
import Modelo.EnumEstadoNovedad;
import Modelo.Espacio;
import Modelo.Habitacion;
import Modelo.Novedades;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class NovedadesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Novedades novedad;
    private List<Novedades> novedades;
    private List<EnumEstadoNovedad> estados;

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
        estados = Arrays.asList(EnumEstadoNovedad.values());

        try {
            novedades = getNovedadesDAO().listar();
        } catch (SQLException e) {
            novedades = new ArrayList<>();
        }

        try {
            habitaciones = getHabitacionDAO().listar();
        } catch (SQLException e) {
            habitaciones = new ArrayList<>();
        }

        try {
            espacios = getEspacioDAO().listar();
        } catch (SQLException e) {
            espacios = new ArrayList<>();
        }
    }

    public List<Novedades> getNovedades() {
        return novedades;
    }

    public Novedades getNovedad() {
        return novedad;
    }

    public void setNovedad(Novedades novedad) {
        this.novedad = novedad;
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

    public List<EnumEstadoNovedad> getEstados() {
        return estados;
    }

    public void cargarNovedadPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Novedades encontrada = getNovedadesDAO().buscarPorId(id);
                if (encontrada != null) {
                    this.novedad = encontrada;
                    habitacionSeleccionada = (encontrada.getHabitacion() != null)
                            ? encontrada.getHabitacion().getIdHabitacion() : null;
                    espacioSeleccionado = (encontrada.getEspacio() != null)
                            ? encontrada.getEspacio().getIdEspacio() : null;
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
        if (habitacionSeleccionada == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Seleccione una habitación.");
            return null;
        }

        if (espacioSeleccionado != null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Solo puede elegir una habitación o un espacio.");
            return null;
        }

        configurarNovedadDestino(habitacionSeleccionada, null);
        return registrar();
    }

    public String registrarEspacio() {
        if (espacioSeleccionado == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Seleccione un espacio.");
            return null;
        }

        if (habitacionSeleccionada != null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Solo puede elegir una habitación o un espacio.");
            return null;
        }

        configurarNovedadDestino(null, espacioSeleccionado);
        return registrar();
    }

    private String registrar() {
        if (!validarFechasParaCreacion()) {
            return null;
        }

        novedad.setFechaRegistro(LocalDateTime.now());
        novedad.setEstado(EnumEstadoNovedad.ACTIVA);

        try {
            getNovedadesDAO().insertar(novedad);
            recargarNovedades();
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Novedad registrada correctamente.");
            limpiarFormulario();
            return "Novedades?faces-redirect=true";
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la novedad.");
            return null;
        }
    }

    public String actualizar() {
        if (!validarDestino()) {
            return null;
        }

        if (!validarFechaFin(novedad.getFechaFin(), novedad.getFechaRegistro())) {
            return null;
        }

        try {
            getNovedadesDAO().actualizar(novedad);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Novedad actualizada correctamente.");
            return "Novedades?faces-redirect=true";
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la novedad.");
            return null;
        }
    }

    public String eliminar(Novedades novedad) {
        try {
            getNovedadesDAO().eliminar(novedad.getIdNovedad());
            recargarNovedades();
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Eliminado", "La novedad fue eliminada correctamente.");
            return "Novedades?faces-redirect=true";
        } catch (SQLException e) {
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la novedad.");
            return null;
        }
    }

    public boolean esDeHabitacion(Novedades novedad) {
        return novedad.getHabitacion() != null;
    }

    public String obtenerDestino(Novedades novedad) {
        if (novedad.getHabitacion() != null) {
            return "Habitación " + novedad.getHabitacion().getNumHabitacion();
        }
        if (novedad.getEspacio() != null) {
            return novedad.getEspacio().getNombre();
        }
        return "No asignado";
    }

    public int totalNovedades() {
        try {
            return getNovedadesDAO().contarTotal();
        } catch (SQLException e) {
            return 0;
        }
    }

    public int totalActivas() {
        try {
            return getNovedadesDAO().contarPorEstado(EnumEstadoNovedad.ACTIVA);
        } catch (SQLException e) {
            return 0;
        }
    }

    public int totalResueltas() {
        try {
            return getNovedadesDAO().contarPorEstado(EnumEstadoNovedad.RESUELTA);
        } catch (SQLException e) {
            return 0;
        }
    }

    private void recargarNovedades() throws SQLException {
        novedades = getNovedadesDAO().listar();
    }

    private void configurarNovedadDestino(Integer idHabitacion, Integer idEspacio) {
        if (idHabitacion != null) {
            Habitacion habitacion = new Habitacion();
            habitacion.setIdHabitacion(idHabitacion);
            novedad.setHabitacion(habitacion);
            novedad.setEspacio(null);
        } else if (idEspacio != null) {
            Espacio espacio = new Espacio();
            espacio.setIdEspacio(idEspacio);
            novedad.setEspacio(espacio);
            novedad.setHabitacion(null);
        }
    }

    private boolean validarFechasParaCreacion() {
        LocalDateTime fechaRegistro = LocalDateTime.now();
        return validarFechaFin(novedad.getFechaFin(), fechaRegistro);
    }

    private boolean validarDestino() {
        if (habitacionSeleccionada != null && espacioSeleccionado != null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Seleccione solo un destino.");
            return false;
        }

        if (habitacionSeleccionada == null && espacioSeleccionado == null) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar habitación o espacio.");
            return false;
        }

        configurarNovedadDestino(habitacionSeleccionada, espacioSeleccionado);
        return true;
    }

    private boolean validarFechaFin(LocalDateTime fechaFin, LocalDateTime fechaRegistro) {
        if (fechaFin == null) {
            return true;
        }

        if (fechaFin.isBefore(fechaRegistro)) {
            mostrarMensaje(FacesMessage.SEVERITY_WARN, "Fecha inválida", "La fecha de cierre no puede ser anterior a la fecha de registro.");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        novedad = new Novedades();
        habitacionSeleccionada = null;
        espacioSeleccionado = null;
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumen, detalle));
    }

    private NovedadesDAO getNovedadesDAO() {
        if (novedadesDAO == null) {
            novedadesDAO = new NovedadesDAO();
        }
        return novedadesDAO;
    }

    private HabitacionDAO getHabitacionDAO() {
        if (habitacionDAO == null) {
            habitacionDAO = new HabitacionDAO();
        }
        return habitacionDAO;
    }

    private EspacioDAO getEspacioDAO() {
        if (espacioDAO == null) {
            espacioDAO = new EspacioDAO();
        }
        return espacioDAO;
    }
}
