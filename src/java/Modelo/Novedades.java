
package Modelo;

import java.time.LocalDateTime;
import Modelo.EnumEstadoNovedad;

public class Novedades {
    private int idNovedad;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaFin;
    private EnumEstadoNovedad estado;
    private Habitacion habitacion;
    private Espacio espacio;

    public int getIdNovedad() {
        return idNovedad;
    }

    public void setIdNovedad(int idNovedad) {
        this.idNovedad = idNovedad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EnumEstadoNovedad getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoNovedad estado) {
        this.estado = estado;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public Espacio getEspacio() {
        return espacio;
    }

    public void setEspacio(Espacio espacio) {
        this.espacio = espacio;
    }
    
    
}
