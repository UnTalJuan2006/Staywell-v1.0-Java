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
    GENERAL("General", "General"),
    PERSONAL("Personal", "Personal"),
    RESERVANUEVA("ReservaNueva", "Nueva reserva");

    private final String etiqueta;
    private final String valorBaseDatos;

    EnumTipoNotificacion(String valorBaseDatos, String etiqueta) {
        this.valorBaseDatos = valorBaseDatos;
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getValorBaseDatos() {
        return valorBaseDatos;
    }

    public static EnumTipoNotificacion desdeBaseDatos(String valor) {
        if (valor == null) {
            return null;
        }

        for (EnumTipoNotificacion tipo : values()) {
            if (tipo.valorBaseDatos.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }

        return null;
    }
}
