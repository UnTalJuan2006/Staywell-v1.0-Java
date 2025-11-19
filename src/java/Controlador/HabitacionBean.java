package Controlador;

import javax.annotation.PostConstruct;
import DAO.HabitacionDAO;
import DAO.TipoHabitacionDAO;
import Modelo.EnumEstadoHabitacion;
import Modelo.Habitacion;
import Modelo.TipoHabitacion;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class HabitacionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Habitacion habitacion;
    private transient HabitacionDAO habitacionDAO;
    private transient TipoHabitacionDAO tipoHabitacionDAO;

    private List<Habitacion> habitaciones;
    private List<Habitacion> habitacionesFiltradas;
    private List<TipoHabitacion> listaTipos;

    private Integer idTipoSeleccionado;
    private String filtro;

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public List<Habitacion> getHabitacionesFiltradas() {
        return habitacionesFiltradas;
    }

    @PostConstruct
    public void init() {
        System.out.println("⏳ Iniciando PostConstruct de HabitacionBean...");
        try {
            habitacion = new Habitacion();
            ensureDaos();

            System.out.println("➡️  Cargando lista de tipos...");
            listaTipos = getTipoHabitacionDAO().listar();
            if (listaTipos == null) {
                System.out.println("⚠️ tipoHabitacionDAO.listar() devolvió null, se crea lista vacía");
                listaTipos = new ArrayList<>();
            }

            System.out.println("➡️  Cargando lista de habitaciones...");
            habitaciones = getHabitacionDAO().listar();
            if (habitaciones == null) {
                System.out.println("⚠️ habitacionDAO.listar() devolvió null, se crea lista vacía");
                habitaciones = new ArrayList<>();
            }

            //habitacionesFiltradas = new ArrayList<>(habitaciones);
//            System.out.println("✅ Datos iniciales cargados correctamente. Total habitaciones: " + habitaciones.size());
        } catch (Exception e) {
            System.out.println("💥 Error en @PostConstruct: " + e.getMessage());
            e.printStackTrace();
            listaTipos = new ArrayList<>();
            habitaciones = new ArrayList<>();
            habitacionesFiltradas = new ArrayList<>();
            habitacionesFiltradas = new ArrayList<>(habitaciones);

        }
    }

    public List<Habitacion> getListaHabitaciones() {
        try {
            return getHabitacionDAO().listar();
        } catch (SQLException e) {
            System.out.println("Error al listar habitaciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- MÉTODO PARA FILTRAR ---
    public void filtrarHabitacionesPorTipo() {
        if (idTipoSeleccionado == null) {
            habitacionesFiltradas = new ArrayList<>(habitaciones);
            return;
        }
        habitacionesFiltradas.clear();
        for (Habitacion h : habitaciones) {
            if (h.getTipoHabitacion() != null
                    && h.getTipoHabitacion().getIdTipoHabitacion() == idTipoSeleccionado) {
                habitacionesFiltradas.add(h);
            }
        }
    }

    // --- MÉTODO PARA AGREGAR ---
    public String agregar() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            if (idTipoSeleccionado == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Advertencia", "Debe seleccionar un tipo de habitación."));
                return null;
            }

            // ✅ Crear el tipo solo con el ID seleccionado
            TipoHabitacion tipo = new TipoHabitacion();
            tipo.setIdTipoHabitacion(idTipoSeleccionado);

            habitacion.setTipoHabitacion(tipo);
            habitacion.setFechaCreacion(LocalDateTime.now());
            habitacion.setFechaActualizacion(LocalDateTime.now());
            habitacion.setEstado(EnumEstadoHabitacion.Disponible);

            getHabitacionDAO().agregar(habitacion);

            // Refrescar datos
            habitaciones = getHabitacionDAO().listar();
            habitacionesFiltradas = new ArrayList<>(habitaciones);

            // Reiniciar formulario
            habitacion = new Habitacion();
            idTipoSeleccionado = null;

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Habitación registrada correctamente."));
            return "Habitaciones?faces-redirect=true";

        } catch (SQLException e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error SQL", "No se pudo registrar la habitación."));
            return null;
        }
    }

    public String actualizar() {
        try {
            habitacion.setFechaActualizacion(LocalDateTime.now());
            getHabitacionDAO().actualizar(habitacion);
            habitacion = new Habitacion();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Habitacion actualizada correctamente."));
            return "Habitaciones?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la Habitacion."));
            return null;
        }
    }

    public void cargarHabitacionPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Habitacion habitacionEncontrada = getHabitacionDAO().buscarPorId(id);

                if (habitacionEncontrada != null) {
                    this.habitacion = habitacionEncontrada;

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Habitación cargada correctamente",
                                    "Se cargó la habitación con ID: " + id));

                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Advertencia",
                                    "La habitación no existe."));
                }

            } catch (NumberFormatException | SQLException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No se pudo cargar la habitación."));
                e.printStackTrace();
            }
        }
    }

    public String eliminar(Habitacion h) {
        try {
            HabitacionDAO habitacionDAO = new HabitacionDAO();
            habitacionDAO.eliminar(h);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Habitación eliminada correctamente", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar habitación: " + e.getMessage(), null));
        }
        return "Habitaciones?faces-redirect=true";
    }

  public void buscar() {
    try {
        // Asegurar listas no nulas
        if (habitaciones == null) {
            habitaciones = getHabitacionDAO().listar();
        }
        if (habitacionesFiltradas == null) {
            habitacionesFiltradas = new ArrayList<>(habitaciones);
        }

        // Si el filtro está vacío, restaurar la lista completa
        if (filtro == null || filtro.trim().isEmpty()) {
            habitacionesFiltradas = new ArrayList<>(habitaciones);
            return;
        }

        String txt = filtro.trim().toLowerCase();
        List<Habitacion> resultados = new ArrayList<>();

        for (Habitacion h : habitaciones) {
            // numHabitacion es int -> convertir a String
            String numStr = String.valueOf(h.getNumHabitacion());

            String tipo = h.getNombreTipoHabitacion() != null
                    ? h.getNombreTipoHabitacion().toLowerCase()
                    : "";

            String estado = h.getEstado() != null
                    ? h.getEstado().name().toLowerCase()
                    : "";

            boolean coincideNumero = numStr.toLowerCase().contains(txt);
            boolean coincideTipo = tipo.contains(txt);
            boolean coincideEstado = estado.contains(txt);

            if (coincideNumero || coincideTipo || coincideEstado) {
                resultados.add(h);
            }
        }

        habitacionesFiltradas = resultados;

    } catch (SQLException ex) {
        System.out.println("ERROR filtrando habitaciones: " + ex.getMessage());
        // en caso de error, no dejar la lista nula
        if (habitacionesFiltradas == null) {
            habitacionesFiltradas = new ArrayList<>();
        }
    }
}


    // --- CONTADORES POR TIPO ---
    public int totalHabitacionesEstandar() throws SQLException {
        return getHabitacionDAO().contarPorTipo(1);
    }

    public int totalHabitacionesFamiliar() throws SQLException {
        return getHabitacionDAO().contarPorTipo(2);
    }

    public int totalHabitacionesVip() throws SQLException {
        return getHabitacionDAO().contarPorTipo(3);
    }

    public int totalHabitacionesDuplex() throws SQLException {
        return getHabitacionDAO().contarPorTipo(4);
    }

    public int totalHabitacionesIndividual() throws SQLException {
        return getHabitacionDAO().contarPorTipo(5);
    }

    // --- GETTERS Y SETTERS ---
    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

  
    public List<TipoHabitacion> getListaTipos() {
        return listaTipos;
    }

    public Integer getIdTipoSeleccionado() {
        return idTipoSeleccionado;
    }

    public void setIdTipoSeleccionado(Integer idTipoSeleccionado) {
        this.idTipoSeleccionado = idTipoSeleccionado;
    }

    public EnumEstadoHabitacion[] getEstados() {
        return EnumEstadoHabitacion.values();
    }

    public void setListaTipos(List<TipoHabitacion> listaTipos) {
        this.listaTipos = listaTipos;
    }

    private void ensureDaos() {
        if (habitacionDAO == null) {
            habitacionDAO = new HabitacionDAO();
        }
        if (tipoHabitacionDAO == null) {
            tipoHabitacionDAO = new TipoHabitacionDAO();
        }
    }

    private HabitacionDAO getHabitacionDAO() {
        if (habitacionDAO == null) {
            habitacionDAO = new HabitacionDAO();
        }
        return habitacionDAO;
    }

    private TipoHabitacionDAO getTipoHabitacionDAO() {
        if (tipoHabitacionDAO == null) {
            tipoHabitacionDAO = new TipoHabitacionDAO();
        }
        return tipoHabitacionDAO;
    }
}
