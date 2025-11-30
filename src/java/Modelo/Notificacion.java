
package Modelo;
import Modelo.EnumEstadoNotificacion;
import Modelo.EnumTipoNotificacion;
import java.time.LocalDateTime;


public class Notificacion {
    private int idNotificacion;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private EnumEstadoNotificacion estado;
    private EnumTipoNotificacion tipo;
    private Usuario usuario;

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public EnumEstadoNotificacion getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoNotificacion estado) {
        this.estado = estado;
    }

    public EnumTipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(EnumTipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    
    
}
