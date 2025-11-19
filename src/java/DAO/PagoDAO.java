
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

            if (p.getReserva() != null && p.getReserva().getIdReserva() > 0) {
                ps.setInt(1, p.getReserva().getIdReserva());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (p.getEvento() != null && p.getEvento().getIdEvento() > 0) {
                ps.setInt(2, p.getEvento().getIdEvento());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (p.getMonto() != null) {
                ps.setBigDecimal(3, p.getMonto());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            ps.setString(4, p.getTipoTarjeta().name());
            ps.setString(5, p.getNumeroTarjeta());
            ps.setString(6, p.getTitular());
            if (p.getFechaVencimiento() != null) {
                ps.setDate(7, java.sql.Date.valueOf(p.getFechaVencimiento()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, p.getCodigoSeguridad());
            ps.setTimestamp(9, Timestamp.valueOf(p.getFechaCreacion()));

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }


    
}
