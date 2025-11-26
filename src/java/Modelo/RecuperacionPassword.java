package Modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RecuperacionPassword implements Serializable {

    private int idRecuperacion;
    private int idUsuario;
    private String token;
    private LocalDateTime fechaExpiracion;
    private boolean usado;

    // ✅ Constructor vacío
    public RecuperacionPassword() {
    }

    // ✅ Constructor completo
    public RecuperacionPassword(int idRecuperacion, int idUsuario, String token, 
                                 LocalDateTime fechaExpiracion, boolean usado) {
        this.idRecuperacion = idRecuperacion;
        this.idUsuario = idUsuario;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    // ✅ Getters y Setters
    public int getIdRecuperacion() {
        return idRecuperacion;
    }

    public void setIdRecuperacion(int idRecuperacion) {
        this.idRecuperacion = idRecuperacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}
