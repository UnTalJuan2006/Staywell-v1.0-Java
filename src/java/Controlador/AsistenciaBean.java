package Controlador;

import Modelo.Asistencia;
import Modelo.Empleado;
import DAO.AsistenciaDAO;
import DAO.EmpleadoDAO;
import java.io.Serializable;
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
public class AsistenciaBean implements Serializable {

    private Asistencia asistencia;
    private transient AsistenciaDAO asistenciaDAO;
    private transient EmpleadoDAO empleadoDAO;

    private List<Asistencia> asistencias;
    private List<Empleado> listaEmpleados;

    // IMPORTANTE → debe ser Integer (acepta null)
    private Integer idEmpleadoSeleccionado;

    public Asistencia getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(Asistencia asistencia) {
        this.asistencia = asistencia;
    }

    public Integer getIdEmpleadoSeleccionado() {
        return idEmpleadoSeleccionado;
    }

    // CORREGIDO → Integer, no int
    public void setIdEmpleadoSeleccionado(Integer idEmpleadoSeleccionado) {
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado;
    }

    public List<Empleado> getListaEmpleados() {
        return listaEmpleados;
    }

    @PostConstruct
    public void init() {
        try {
            asistencia = new Asistencia();

            listaEmpleados = getEmpleadoDAO().listar();
            if (listaEmpleados == null) {
                listaEmpleados = new ArrayList<>();
            }

            asistencias = getAsistenciaDAO().listar();
            if (asistencias == null) {
                asistencias = new ArrayList<>();
            }

        } catch (Exception e) {
            System.out.println("💥 Error en @PostConstruct: " + e.getMessage());
            e.printStackTrace();
            listaEmpleados = new ArrayList<>();
            asistencias = new ArrayList<>();
        }
    }

    public List<Asistencia> getListAsistencias() {
        try {
            return getAsistenciaDAO().listar();
        } catch (SQLException e) {
            System.out.println("Error al listar Asistencia: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public String registrarEntrada() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {

            // Validación de empleado
            if (idEmpleadoSeleccionado == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Advertencia", "Debe seleccionar un empleado."));
                return null;
            }

            // Crear empleado básico
            Empleado empleado = new Empleado();
            empleado.setIdEmpleado(idEmpleadoSeleccionado);

            // Asignar datos
            asistencia.setEmpleado(empleado);
            asistencia.setFecha(new java.util.Date());   // FECHA AUTOMÁTICA
            asistencia.setHoraEntrada(LocalTime.now());  // HORA AUTOMÁTICA

            // Guardar en BD
            getAsistenciaDAO().registrarEntrada(asistencia);

            // Actualizar lista
            asistencias = getAsistenciaDAO().listar();

            // Limpiar campos
            asistencia = new Asistencia();
            idEmpleadoSeleccionado = null;

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Asistencia registrada correctamente."));

            return "Asistencias?faces-redirect=true";

        } catch (SQLException e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error SQL", "No se pudo registrar la asistencia."));
            return null;
        }
    }

    
    public void registrarSalida(Asistencia a) {
    try {
        // Solo seteamos la hora de salida
        a.setHoraSalida(LocalTime.now());

        // Actualizamos en la BD
        getAsistenciaDAO().registrarSalida(a);

        // Actualizamos la lista para refrescar la tabla
        asistencias = getAsistenciaDAO().listar();

    } catch (SQLException e) {
        e.printStackTrace();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Error SQL", "No se pudo registrar la salida."));
    }
}



    private EmpleadoDAO getEmpleadoDAO() {
        if (empleadoDAO == null) {
            empleadoDAO = new EmpleadoDAO();
        }
        return empleadoDAO;
    }

    private AsistenciaDAO getAsistenciaDAO() {
        if (asistenciaDAO == null) {
            asistenciaDAO = new AsistenciaDAO();
        }
        return asistenciaDAO;
    }
}
