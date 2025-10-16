
package Controlador;


import Modelo.Empleado;
import Modelo.EnumCargoEmpleado;
import Modelo.EnumEstadoEmpleado;
import DAO.EmpleadoDAO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;


@ManagedBean
@ViewScoped

public class EmpleadoBean {
    private Empleado empleado = new Empleado();
    private EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private List<Empleado> lstEmpleado;
    
    public EnumCargoEmpleado[]getCargos(){
        return EnumCargoEmpleado.values();
    }
    
    public Empleado getEmpleado(){
        return empleado;
    }
    
    public void setEmpleado(Empleado empleado ){
        this.empleado = empleado;
    }
    
    public List<Empleado> getListaEmpleados(){
        try{
            return empleadoDAO.listar();
            
        }catch(SQLException e){
            System.out.println("Error al listar Eventos");
            return null;
        }    
    }
    
    public String agregar(){
        try{
           empleado.setFechaCreacion(LocalDateTime.now());
           empleado.setFechaActualizacion(LocalDateTime.now());
            empleado.setEstado(EnumEstadoEmpleado.Activo); 
           empleadoDAO.agregar(empleado);
           empleado = new Empleado();
           
           FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
            "Éxito", "Empleado registrado correctamente."));
        }catch(SQLException e){
            FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Error", "No se pudo registrar el Empleado."));
             return null;
        }
        return "Empleados?faces-redirect=true";
    }
   
    public String editar(Empleado m){
        this.empleado = m;
        return "editarEmpleado";
    }
    
    public String actualizar(){
        try{
            empleado.setFechaActualizacion(LocalDateTime.now());
            empleadoDAO.actualizar(empleado);
            empleado = new Empleado();
            
             FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, " Éxito", "Evento Empleado correctamente"));
            
        }catch(Exception e){
             FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el evento"));
            
        }
        return "Empleados?faces-redirect=true";
    }
    
    
  public void ver(Empleado m) {
    try {
        Empleado empleadoEncontrado = empleadoDAO.buscar(m.getIdEmpleado());
        if (empleadoEncontrado != null) {
            this.empleado = empleadoEncontrado;
            System.out.println("Empleado cargado para edición: " + empleado.getNombre());
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No encontrado", "Empleado no existe"));
        }
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar el empleado"));
    }
}


}
