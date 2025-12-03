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
    GENERAL("GENERAL", "General"),
    PERSONAL("PERSONAL", "Personal"),
    RESERVANUEVA("NUEVARESERVA", "Nueva reserva", "ReservaNueva", "NuevaReserva");

    private final String etiqueta;
    private final String valorBaseDatos;
    private final String[] aliasBaseDatos;

    EnumTipoNotificacion(String valorBaseDatos, String etiqueta, String... aliasBaseDatos) {
        this.valorBaseDatos = valorBaseDatos;
        this.etiqueta = etiqueta;
        this.aliasBaseDatos = aliasBaseDatos;
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

            for (String alias : tipo.aliasBaseDatos) {
                if (alias.equalsIgnoreCase(valor)) {
                    return tipo;
                }
            }
        }

        return null;
    }
}
