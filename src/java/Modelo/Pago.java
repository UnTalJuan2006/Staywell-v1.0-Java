
package Modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import Modelo.EnumPago;
import Modelo.Reserva;
import java.time.LocalDate;


public class Pago {
    private int idPago;
    private Reserva reserva;
    private EnumPago tipoTarjeta;
    private String numeroTarjeta;
    private String titular;
    private LocalDate fechaVencimiento;
    private String codigoSeguridad;
    private LocalDateTime fechaCreacion;
    private BigDecimal monto = BigDecimal.ZERO;

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
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
    

