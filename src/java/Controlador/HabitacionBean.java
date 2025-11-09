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

    private Habitacion habitacion;
    private HabitacionDAO habitacionDAO;
    private TipoHabitacionDAO tipoHabitacionDAO;

    private List<Habitacion> habitaciones;
    private List<Habitacion> habitacionesFiltradas;
    private List<TipoHabitacion> listaTipos;

    private Integer idTipoSeleccionado; // ✅ Solo manejamos el ID

@PostConstruct
public void init() {
    System.out.println("⏳ Iniciando PostConstruct de HabitacionBean...");
    try {
        habitacion = new Habitacion();
        habitacionDAO = new HabitacionDAO();
        tipoHabitacionDAO = new TipoHabitacionDAO();

        System.out.println("➡️  Cargando lista de tipos...");
        listaTipos = tipoHabitacionDAO.listar();
        if (listaTipos == null) {
            System.out.println("⚠️ tipoHabitacionDAO.listar() devolvió null, se crea lista vacía");
            listaTipos = new ArrayList<>();
        }

        System.out.println("➡️  Cargando lista de habitaciones...");
        habitaciones = habitacionDAO.listar();
        if (habitaciones == null) {
            System.out.println("⚠️ habitacionDAO.listar() devolvió null, se crea lista vacía");
            habitaciones = new ArrayList<>();
        }

        int ddd = 0;
        
        if(habitaciones != null)
            ddd = habitaciones.size();
        
        habitacionesFiltradas = new ArrayList<>(habitaciones);
        
        System.out.println("✅ Datos iniciales cargados correctamente. Total habitaciones: " + ddd);

    } catch (Exception e) {
        System.out.println("💥 Error en @PostConstruct: " + e.getMessage());
        e.printStackTrace();
        listaTipos = new ArrayList<>();
        habitaciones = new ArrayList<>();
        habitacionesFiltradas = new ArrayList<>();
    }
}


    public List<Habitacion> getListaHabitaciones() {
        try {
            return habitacionDAO.listar();
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
            if (h.getTipoHabitacion() != null &&
                h.getTipoHabitacion().getIdTipoHabitacion() == idTipoSeleccionado) {
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

            habitacionDAO.agregar(habitacion);

            // Refrescar datos
            habitaciones = habitacionDAO.listar();
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
    
    public String actualizar(){
        try{
            habitacion.setFechaActualizacion(LocalDateTime.now());
            habitacionDAO.actualizar(habitacion);
            habitacion = new Habitacion();
            
             FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Habitacion actualizada correctamente."));
             return "Habitaciones?faces-redirect=true";
        }catch(SQLException e){
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
                Habitacion habitacionEncontrada = habitacionDAO.buscarPorId(id);

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

    // --- CONTADORES POR TIPO ---
    public int totalHabitacionesEstandar() throws SQLException {
        return habitacionDAO.contarPorTipo(1);
    }

    public int totalHabitacionesFamiliar() throws SQLException {
        return habitacionDAO.contarPorTipo(2);
    }

    public int totalHabitacionesVip() throws SQLException {
        return habitacionDAO.contarPorTipo(3);
    }

    public int totalHabitacionesDuplex() throws SQLException {
        return habitacionDAO.contarPorTipo(4);
    }

    public int totalHabitacionesIndividual() throws SQLException {
        return habitacionDAO.contarPorTipo(5);
    }

    // --- GETTERS Y SETTERS ---
    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public List<Habitacion> getHabitacionesFiltradas() {
        return habitacionesFiltradas;
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
}
