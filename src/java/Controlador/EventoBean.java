
package Controlador;

import DAO.EventoDAO;
import Modelo.Espacio;
import Modelo.Evento;
import Modelo.Usuario;
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
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;


@ManagedBean
@ViewScoped

public class EventoBean {
    private Evento evento = new Evento();
    private EventoDAO eventoDAO = new EventoDAO();
    private List<Evento> listaEvento;
    private List<Espacio> listaEspacios = new ArrayList<>(); 
    private Espacio espacioSeleccionado;
     
    public Evento getEvento() {
    return evento;
}

    public void setEvento(Evento evento) {
    this.evento = evento;
}
     public Espacio getEspacioSeleccionado() {
        return espacioSeleccionado;
    }

    public void setEspacioSeleccionado(Espacio espacioSeleccionado) {
        this.espacioSeleccionado = espacioSeleccionado;
    }
    

    
    public List<Evento> getListaEventos(){
        try{
            return eventoDAO.listar();
            
        }catch(SQLException e){
            System.out.println("Error al listar habitaciones");
            return null;
        }
    }
    
      // 👉 Método para seleccionar el espacio al que se le hace reserva
    public void seleccionarEspacio(Espacio espacio) {
        this.espacioSeleccionado = espacio;
        evento = new Evento(); 
        evento.setEspacio(espacio);
    }
    
     public void agregarEventoHuesped() {
        try {
           
            Usuario usuarioLogueado = (Usuario) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get("usuarioLogueado");

            if (usuarioLogueado == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe iniciar sesión para reservar."));
                return;
            }

            // Asignar datos al evento
            evento.setNombreCliente(usuarioLogueado.getNombre()); 
            evento.setFechaCreacion(LocalDateTime.now());
            evento.setFechaActualizacion(LocalDateTime.now());
            evento.setEspacio(espacioSeleccionado);

            // Guardar en la BD
            eventoDAO.agregarEventoHuesped(evento);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva exitosa",
                            "Has reservado el espacio: " + espacioSeleccionado.getNombre()));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la reserva: " + e.getMessage()));
        }
    }

   
    
//    public String agregar(){
//        try{
//            evento.setFechaCreacion(LocalDateTime.now());
//            evento.setFechaActualizacion(LocalDateTime.now());
//            eventoDAO.agregar(evento);
//            evento = new Evento();
//            
//            FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(FacesMessage.SEVERITY_INFO, 
//            "Éxito", "Evento registrado correctamente."));
//            
//        }catch(SQLException e){
//             FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//            "Error", "No se pudo registrar el evento."));
//             return null;
//        }
//        return "Eventos?faces-redirect=true";
//    }
//    
//    
//    public String editar(Evento v){
//        this.evento = v;
//        return "editarEvento?faces-redirect=true";
//    }
//    
//    
//    public String actualizar(){
//      try{
//       evento.setFechaActualizacion(LocalDateTime.now());
//       eventoDAO.actualizar(evento);
//       evento = new Evento();
//         FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(FacesMessage.SEVERITY_INFO, " Éxito", "Evento actualizado correctamente"));
//    } catch (Exception e) {
//        System.out.println("Error al actualizar habitación: " + e.getMessage());
//        FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el evento"));
//    }
//    return "Eventos?faces-redirect=true";
//
//    }
//    
//    public String eliminar (Evento v){
//        try{
//            eventoDAO.eliminar(v);
//            FacesContext.getCurrentInstance().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO,
//                "Evento eliminado correctamente", null));
//        }catch (Exception e) {
//        FacesContext.getCurrentInstance().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                "Error al eliminar evento: " + e.getMessage(), null));
//    }
//    return "Eventos?faces-redirect=true"; 
//    }
//    
//    
//    public String ver (Evento v){
//        try{
//            Evento eventoEncontrado = eventoDAO.buscar(v.getIdEvento());
//            if(eventoEncontrado != null){
//                this.evento = eventoEncontrado;
//                return "verEvento";
//            }else {
//              FacesContext.getCurrentInstance().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_WARN, "No encontrado", "Evento no existe"));
//            return "Eventos";
//            }
//        }catch(Exception e){
//             FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(FacesMessage.SEVERITY_ERROR, " Error", "No se pudo cargar el evento"));
//        return "Eventos";
//        }
//    }
       
        
        // Total de eventos
public int getTotalEventos() {
    List<Evento> eventos = getListaEventos();
    return (eventos != null) ? eventos.size() : 0;
}


}