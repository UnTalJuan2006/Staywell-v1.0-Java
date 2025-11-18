
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
import javax.annotation.PostConstruct;
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
  private List<Empleado> listaEmpleados;
  
  
  public Empleado getEmpleado(){
      return empleado;
  }
  
  public void setEmpleado(Empleado empleado){
      this.empleado = empleado;
  }
  
  public void setListaEmpleados(List<Empleado> listaEmpleados){
      this.listaEmpleados = listaEmpleados;
  }
  public EnumCargoEmpleado[] getCargos() {
        return EnumCargoEmpleado.values();
    }
  
  public EnumEstadoEmpleado[] getEstados(){
      return EnumEstadoEmpleado.values();
  }
   @PostConstruct 
    public void init(){
        empleado = new Empleado();
        empleadoDAO = new EmpleadoDAO();
        getListaEmpleados(); 
    } 
    
    public List<Empleado> getListaEmpleados(){
        try {
            return empleadoDAO.listar();
        }catch(SQLException e){
            System.out.println("Error al listar los empleados");
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
     
     public void cargarEmpleadoPorId(){
         String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
         
         if(idParam != null){
             try{
                 int id = Integer.parseInt(idParam);
                 Empleado empleadoEncontrado = empleadoDAO.buscarPorId(id);
                 
                 if(empleadoEncontrado != null){
                     this.empleado = empleadoEncontrado;
                     
                     
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Empleado cargado correctamente",
                                    "Se cargó el tipo  con ID: " + id));
                 }else{
                     FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Advertencia",
                                    "El Empleado no existe."));
                 }
                 
             }catch(NumberFormatException | SQLException e){
                     FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No se pudo cargar el tipohabitación."));
                e.printStackTrace();
             }
         }
     }
     
     public String eliminar(Empleado m){
         try{
             EmpleadoDAO empleadoDAO = new EmpleadoDAO();
             empleadoDAO.eliminar(m);
              FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Empleado eliminado correctamente", null));
         }catch(Exception e){
              FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar el empleado: " + e.getMessage(), null));
         }
         return "Empleados?faces-redirect=true";
     }
     
     public String actualizar() {
    try {
        // Actualizamos la fecha de modificación
        empleado.setFechaActualizacion(LocalDateTime.now());

        // Ejecuta el DAO
        empleadoDAO.actualizar(empleado);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Empleado actualizado correctamente."));

        // Limpia el formulario
        empleado = new Empleado();

        // Redirección a la tabla
        return "Empleados?faces-redirect=true";

    } catch (SQLException e) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "No se pudo actualizar el empleado: " + e.getMessage()));
        return null;
    }
}

   
}
