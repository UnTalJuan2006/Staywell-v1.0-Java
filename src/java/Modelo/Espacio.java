package Modelo;

import Modelo.EnumTipoEspacios;
import Modelo.EnumEstadoEspacio;
import java.time.LocalDateTime;

public class Espacio {

    private int idEspacio;
    private String nombre;
    private EnumTipoEspacios tipo;
    private String descripcion;
    private int capacidad;
    private float costoHora;
    private LocalDateTime fechaActualizacion;
    private EnumEstadoEspacio estado;
    private String imagen;

    public int getIdEspacio() {
        return idEspacio;
    }

    public void setIdEspacio(int idEspacio) {
        this.idEspacio = idEspacio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EnumTipoEspacios getTipo() {
        return tipo;
    }

    public void setTipo(EnumTipoEspacios tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public float getCostoHora() {
        return costoHora;
    }

    public void setCostoHora(float costoHora) {
        this.costoHora = costoHora;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public EnumEstadoEspacio getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoEspacio estado) {
        this.estado = estado;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    
    
}
