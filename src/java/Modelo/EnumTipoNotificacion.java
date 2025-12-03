/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author juanm
 */
public enum EnumTipoNotificacion {
    GENERAL("General"),
    PERSONAL("Personal"),
    NUEVARESERVA("Nueva reserva");
    

    private final String etiqueta;

    EnumTipoNotificacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
