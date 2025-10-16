package Controlador;

import Modelo.Asistencia;
import Modelo.Empleado;
import DAO.AsistenciaDAO;
import DAO.EmpleadoDAO;
import java.io.Serializable;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class AsistenciaBean {

    private Asistencia asistencia = new Asistencia();
    private List<Asistencia> listaAsistencia = new ArrayList<>();
    private List<Empleado> listaEmpleados = new ArrayList<>();
    private Empleado empleadoSeleccionado; // Agregar empleado seleccionado

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    
    @PostConstruct
    public void init() {
        try {
            listarEmpleados();
        } catch (SQLException e) {
            System.out.println("Error al inicializar empleados: " + e.getMessage());
        }
    }

    public List<Asistencia> getListaAsistencia(){
        try{
            return asistenciaDAO.listar();
            
        }catch(SQLException e){
            System.out.println("Error al listar Asistencias");
            return null;
        }    
    }

    public String registrarEntrada() {
        try {
            // Validar que se haya seleccionado un empleado
            if (empleadoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, 
                        "Advertencia", "Debe seleccionar un empleado."));
                return null;
            }
            
            asistencia.setEmpleado(empleadoSeleccionado); // Asignar empleado
            asistencia.setFecha(new Date(System.currentTimeMillis()));
            asistencia.setHoraEntrada(LocalTime.now());
            asistenciaDAO.registrarEntrada(asistencia);
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                    "Entrada registrada correctamente para " + empleadoSeleccionado.getNombre()));
            
            asistencia = new Asistencia();
            empleadoSeleccionado = null; // Limpiar selección
            
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo registrar la entrada: " + e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return "Asistencias?faces-redirect=true";
    }

    // 👥 Cargar lista de empleados
    public void listarEmpleados() throws SQLException {
        listaEmpleados = empleadoDAO.listar();
    }

    // 🧠 Getters y Setters
    public Asistencia getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(Asistencia asistencia) {
        this.asistencia = asistencia;
    }

    public List<Empleado> getListaEmpleados() {
        return listaEmpleados;
    }

    public void setListaEmpleados(List<Empleado> listaEmpleados) {
        this.listaEmpleados = listaEmpleados;
    }
    
    public Empleado getEmpleadoSeleccionado() {
        return empleadoSeleccionado;
    }

    public void setEmpleadoSeleccionado(Empleado empleadoSeleccionado) {
        this.empleadoSeleccionado = empleadoSeleccionado;
    }
}
