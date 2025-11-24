package DAO;

import Controlador.Conexion;
import Modelo.Pago;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

public class PagoDAO {

    public int agregarPago(Pago pago) throws SQLException {
        if (pago == null) {
            throw new SQLException("No se proporcionaron datos de pago.");
        }

        if ((pago.getReserva() == null || pago.getReserva().getIdReserva() <= 0)
                && (pago.getEvento() == null || pago.getEvento().getIdEvento() <= 0)) {
            throw new SQLException("El pago debe estar asociado a una reserva o un evento válido.");
        }

        if (pago.getFechaCreacion() == null) {
            pago.setFechaCreacion(LocalDateTime.now());
        }

        String sql = "INSERT INTO pago (idReserva, idEvento, monto, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, fechaCreacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }

            try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                if (pago.getReserva() != null && pago.getReserva().getIdReserva() > 0) {
                    ps.setInt(1, pago.getReserva().getIdReserva());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }

                if (pago.getEvento() != null && pago.getEvento().getIdEvento() > 0) {
                    ps.setInt(2, pago.getEvento().getIdEvento());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }

                if (pago.getMonto() != null) {
                    ps.setBigDecimal(3, pago.getMonto());
                } else {
                    ps.setNull(3, Types.NUMERIC);
                }

                if (pago.getTipoTarjeta() != null) {
                    ps.setString(4, pago.getTipoTarjeta().name());
                } else {
                    ps.setNull(4, Types.VARCHAR);
                }

                ps.setString(5, pago.getNumeroTarjeta());
                ps.setString(6, pago.getTitular());

                if (pago.getFechaVencimiento() != null) {
                    ps.setDate(7, java.sql.Date.valueOf(pago.getFechaVencimiento()));
                } else {
                    ps.setNull(7, Types.DATE);
                }

                ps.setString(8, pago.getCodigoSeguridad());
                ps.setTimestamp(9, Timestamp.valueOf(pago.getFechaCreacion()));

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("El pago no se pudo insertar en la base de datos.");
                }

                int idGenerado = -1;
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                    }
                }

                // En algunos entornos (o si la columna no está marcada como AUTO_INCREMENT)
                // el controlador puede no retornar la llave generada. Usamos LAST_INSERT_ID()
                // como respaldo para evitar falsos negativos en el registro de pagos.
                if (idGenerado <= 0) {
                    try (Statement st = conexion.createStatement();
                            ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                        if (rs.next()) {
                            idGenerado = rs.getInt(1);
                        }
                    }
                }

                return idGenerado > 0 ? idGenerado : filas;
            }
        }
    }
}
