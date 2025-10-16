
package Controlador;

import DAO.HabitacionDAO;
import Modelo.EnumEstadoHabitacion;
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
import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import Modelo.Habitacion;
import Modelo.EnumTipoHabitacion;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.faces.bean.ViewScoped;



@ManagedBean
@ViewScoped
public class HabitacionBean {
    private Habitacion habitacion = new Habitacion();
    private HabitacionDAO habitacionDAO = new HabitacionDAO();
    
    public EnumEstadoHabitacion[] getEstados(){
        return EnumEstadoHabitacion.values();
    }
    
    public EnumTipoHabitacion[] getTipos(){
        return EnumTipoHabitacion.values();
    }

    public Habitacion getHabitacion(){
        return habitacion;
    }
    public void setHabitacion(Habitacion habitacion){
        this.habitacion = habitacion;
    }

    
    public List<Habitacion> getListaHabitaciones(){
        try{
            return habitacionDAO.listar();
        }catch(SQLException  e){
            System.out.println("Error al listar habitaciones");
            return null;
        }
    }
    
    public String agregar(){
        try{
            habitacion.setFechaCreacion(LocalDateTime.now());
            habitacion.setFechaActualizacion(LocalDateTime.now());
            habitacionDAO.agregar(habitacion);
            habitacion = new Habitacion();
            
        }catch(SQLException e){
            System.out.println("Error al insertar habitacion");
        }
        return "Habitaciones?faces-redirect=true";
    }
    
    public String editar(Habitacion h){
        this.habitacion = h;
        return "editarHabitacion?faces-redirect=true";
    }
    
  
 public String actualizar() {
    try {
        habitacion.setFechaActualizacion(LocalDateTime.now());
        habitacionDAO.actualizar(habitacion);
        habitacion = new Habitacion();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✔ Éxito", "Habitación actualizada correctamente"));
    } catch (Exception e) {
        System.out.println("Error al actualizar habitación: " + e.getMessage());
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Error", "No se pudo actualizar la habitación"));
    }
    return "Habitaciones?faces-redirect=true";
}


   
   public String eliminar(Habitacion h) {
    try {
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

    

public int getTotalHabitacionesDisponibles() {
    List<Habitacion> habitaciones = getListaHabitaciones();
    if (habitaciones == null) {
        return 0;
    }
    return (int) habitaciones.stream()
            .filter(h -> h.getEstado() == EnumEstadoHabitacion.Disponible)
            .count();
}

}
