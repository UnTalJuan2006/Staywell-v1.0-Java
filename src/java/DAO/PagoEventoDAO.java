package DAO;

import Controlador.Conexion;
import Modelo.PagoEvento;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

public class PagoEventoDAO {

    public int agregarPagoEvento(PagoEvento pagoevento) throws SQLException {
        if (pagoevento == null) {
            throw new SQLException("No se proporcionaron datos de pago.");
        }

        if (pagoevento.getEvento() == null || pagoevento.getEvento().getIdEvento() <= 0) {
            throw new SQLException("El pago debe estar asociado a un Evento valido.");
        }

        String sql = "INSERT INTO pagoevento (idEvento, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, monto, fechaCreacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {
            if (conexion == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }
            try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pagoevento.getEvento().getIdEvento());
                ps.setString(2, pagoevento.getTipoTarjeta() != null ? pagoevento.getTipoTarjeta().name() : "Desconocido");
                ps.setString(3, pagoevento.getNumeroTarjeta() != null ? pagoevento.getNumeroTarjeta() : "");
                ps.setString(4, pagoevento.getTitular() != null ? pagoevento.getTitular() : "");
                
                if(pagoevento.getFechaVencimiento() != null){
                    ps.setDate(5, java.sql.Date.valueOf(pagoevento.getFechaVencimiento()));
                }else{
                    ps.setNull(5, Types.DATE);
                }
                
                ps.setString(6, pagoevento.getCodigoSeguridad() != null ? pagoevento.getCodigoSeguridad() : "");
                ps.setBigDecimal(7, pagoevento.getMonto() != null ? pagoevento.getMonto() : java.math.BigDecimal.ZERO);
                ps.setTimestamp(8, Timestamp.valueOf(pagoevento.getFechaCreacion()));

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
