
package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PagoEvento {
     private int idPagoEvento;
     private  Evento evento;
     private EnumPago tipoTarjeta;
    private String numeroTarjeta;
    private String titular;
    private LocalDate fechaVencimiento;
    private String codigoSeguridad;
    private LocalDateTime fechaCreacion;
    private BigDecimal monto = BigDecimal.ZERO;

    public int getIdPagoEvento() {
        return idPagoEvento;
    }

    public void setIdPagoEvento(int idPagoEvento) {
        this.idPagoEvento = idPagoEvento;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public EnumPago getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(EnumPago tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getCodigoSeguridad() {
        return codigoSeguridad;
    }

    public void setCodigoSeguridad(String codigoSeguridad) {
        this.codigoSeguridad = codigoSeguridad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    
    
}
