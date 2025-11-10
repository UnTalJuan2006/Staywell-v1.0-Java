package Modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Habitacion implements Serializable {

    private static final long serialVersionUID = 1L;
    private int idHabitacion;
    private int numHabitacion;
    private TipoHabitacion tipoHabitacion;
    private EnumEstadoHabitacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String nombreTipoHabitacion;
   
    
    public int getIdHabitacion() {
        return idHabitacion;
    };
    
    public String getNombreTipoHabitacion(){
        return nombreTipoHabitacion;
    }
    
    public void setNombreTipoHabitacion(String tipoHabitacion){
        nombreTipoHabitacion = tipoHabitacion;
    }
    
     public void setIdHabitacion(int idHabitacion) {
        this.idHabitacion = idHabitacion;
    }
     
     public int getNumHabitacion(){
         return numHabitacion;
     }
     
     public void setNumHabitacion(int numHabitacion){
         this.numHabitacion = numHabitacion;
     }
     
     public EnumEstadoHabitacion getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoHabitacion estado) {
        this.estado = estado;
    }
    
      public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    } 

    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }
        
    
}
