
package Modelo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import Modelo.EnumEstadoReserva;

public class Reserva {
    private int idReserva;
    private LocalDateTime checkin;
    private LocalDateTime checkout;
    private LocalDateTime fechaReserva;
    private EnumEstadoReserva estado;
    private String nombreCliente;
    private String email;
    private String telefono;
    private String observaciones;
    private Habitacion habitacion;
    private java.util.List<Habitacion> habitaciones = new java.util.ArrayList<>();
    private Usuario usuario;

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public LocalDateTime getCheckin() {
        return checkin;
    }

    public void setCheckin(LocalDateTime checkin) {
        this.checkin = checkin;
    }

    public LocalDateTime getCheckout() {
        return checkout;
    }

    public void setCheckout(LocalDateTime checkout) {
        this.checkout = checkout;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EnumEstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoReserva estado) {
        this.estado = estado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Habitacion getHabitacion() {
        if (habitacion != null) {
            return habitacion;
        }
        if (habitaciones != null && !habitaciones.isEmpty()) {
            return habitaciones.get(0);
        }
        return null;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
        if (habitacion != null) {
            this.habitaciones = new java.util.ArrayList<>();
            this.habitaciones.add(habitacion);
        }
    }

    public java.util.List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(java.util.List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
        if (habitaciones != null && !habitaciones.isEmpty()) {
            this.habitacion = habitaciones.get(0);
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    
    
    
    
}
