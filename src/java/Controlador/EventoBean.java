
package Controlador;

import DAO.EventoDAO;
import Modelo.Evento;
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

public class EventoBean {
    private Evento evento = new Evento();
    private EventoDAO eventoDAO = new EventoDAO();
     private List<Evento> lstEvento;
     
    public Evento getEvento() {
    return evento;
}

    public void setEvento(Evento evento) {
    this.evento = evento;
}

    
    public List<Evento> getListaEventos(){
        try{
            return eventoDAO.listar();
            
        }catch(SQLException e){
            System.out.println("Error al listar habitaciones");
            return null;
        }
    }
    public String agregar(){
        try{
            evento.setFechaCreacion(LocalDateTime.now());
            evento.setFechaActualizacion(LocalDateTime.now());
            eventoDAO.agregar(evento);
            evento = new Evento();
            
            FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
            "Éxito", "Evento registrado correctamente."));
            
        }catch(SQLException e){
             FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Error", "No se pudo registrar el evento."));
             return null;
        }
        return "Eventos?faces-redirect=true";
    }
    
    
    public String editar(Evento v){
        this.evento = v;
        return "editarEvento?faces-redirect=true";
    }
    
    
    public String actualizar(){
      try{
       evento.setFechaActualizacion(LocalDateTime.now());
       eventoDAO.actualizar(evento);
       evento = new Evento();
         FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, " Éxito", "Evento actualizado correctamente"));
    } catch (Exception e) {
        System.out.println("Error al actualizar habitación: " + e.getMessage());
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el evento"));
    }
    return "Eventos?faces-redirect=true";

    }
    
    public String eliminar (Evento v){
        try{
            eventoDAO.eliminar(v);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Evento eliminado correctamente", null));
        }catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error al eliminar evento: " + e.getMessage(), null));
    }
    return "Eventos?faces-redirect=true"; 
    }
    
    
    public String ver (Evento v){
        try{
            Evento eventoEncontrado = eventoDAO.buscar(v.getIdEvento());
            if(eventoEncontrado != null){
                this.evento = eventoEncontrado;
                return "verEvento";
            }else {
              FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "No encontrado", "Evento no existe"));
            return "Eventos";
            }
        }catch(Exception e){
             FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, " Error", "No se pudo cargar el evento"));
        return "Eventos";
        }
    }
       
        
        // Total de eventos
public int getTotalEventos() {
    List<Evento> eventos = getListaEventos();
    return (eventos != null) ? eventos.size() : 0;
}


}