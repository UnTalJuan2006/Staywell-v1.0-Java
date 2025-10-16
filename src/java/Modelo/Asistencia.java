package Modelo;

import java.sql.Date;
import java.time.LocalTime;


public class Asistencia {
    private int idAsistencia;
    private Empleado empleado;
    private Date fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String observacion;

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Empleado getEmpleado() {
        return empleado;
    }    
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
       return horaSalida;
   }

   public void setHoraSalida(LocalTime horaSalida) {
       this.horaSalida = horaSalida;
   }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    
  
}