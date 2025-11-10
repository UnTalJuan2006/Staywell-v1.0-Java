
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
        String sql = "INSERT INTO pago (idReserva, monto, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, fechaCreacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getReserva().getIdReserva());
            if (p.getMonto() != null) {
                ps.setBigDecimal(2, p.getMonto());
            } else {
                ps.setNull(2, Types.DECIMAL);
            }
            ps.setString(3, p.getTipoTarjeta().name());
            ps.setString(4, p.getNumeroTarjeta());
            ps.setString(5, p.getTitular());
            if (p.getFechaVencimiento() != null) {
                ps.setDate(6, java.sql.Date.valueOf(p.getFechaVencimiento()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, p.getCodigoSeguridad());
            ps.setTimestamp(8, Timestamp.valueOf(p.getFechaCreacion()));

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
