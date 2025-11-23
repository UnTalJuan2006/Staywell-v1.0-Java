
package DAO;

import Controlador.Conexion;
import Modelo.Pago;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class PagoDAO {

    public int agregarPago(Pago p) throws SQLException {
    String sql = "INSERT INTO pago (idReserva, idEvento, monto, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, fechaCreacion) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

        // idReserva
        if (p.getReserva() != null && p.getReserva().getIdReserva() > 0) {
            ps.setInt(1, p.getReserva().getIdReserva());
        } else {
            ps.setNull(1, Types.INTEGER);
        }

        // idEvento
        if (p.getEvento() != null && p.getEvento().getIdEvento() > 0) {
            ps.setInt(2, p.getEvento().getIdEvento());
        } else {
            ps.setNull(2, Types.INTEGER);
        }

        // Monto
        if (p.getMonto() != null) {
            ps.setBigDecimal(3, p.getMonto());
        } else {
            ps.setNull(3, Types.NUMERIC); // <-- CORREGIDO
        }

        // Enum tipo tarjeta
        ps.setString(4, p.getTipoTarjeta().name());

        // Número tarjeta (asegúrate que la columna sea >= 19)
        ps.setString(5, p.getNumeroTarjeta());

        // Titular
        ps.setString(6, p.getTitular());

        // Fecha vencimiento
        if (p.getFechaVencimiento() != null) {
            ps.setDate(7, java.sql.Date.valueOf(p.getFechaVencimiento())); // Debe ser DATE en MySQL
        } else {
            ps.setNull(7, Types.DATE);
        }

        ps.setString(8, p.getCodigoSeguridad());
        ps.setTimestamp(9, Timestamp.valueOf(p.getFechaCreacion()));

        int filas = ps.executeUpdate();

        if (filas == 0) {
            return -1;
        }

        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        // Algunos drivers pueden no retornar claves generadas; consideramos éxito si se insertó al menos un registro
        return filas;
    }
    
}

    
}
