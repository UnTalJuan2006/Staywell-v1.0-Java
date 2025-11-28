package DAO;

import Controlador.Conexion;
import Modelo.EnumPago;
import Modelo.Pago;
import Modelo.Reserva;
import Modelo.Usuario;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

   public List<Pago> listar() throws SQLException {
    List<Pago> listaPagos = new ArrayList<>();

    String sql =
        "SELECT p.*, r.idUsuario, u.nombre AS nombreUsuario " +
        "FROM pago p " +
        "LEFT JOIN reserva r ON p.idReserva = r.idReserva " +
        "LEFT JOIN usuario u ON r.idUsuario = u.idUsuario";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Pago p = new Pago();
            p.setIdPago(rs.getInt("idPago"));
            Reserva reserva = new Reserva();
            reserva.setIdReserva(rs.getInt("idReserva"));
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(rs.getInt("idUsuario"));
            usuario.setNombre(rs.getString("nombreUsuario"));
            reserva.setUsuario(usuario);
            p.setReserva(reserva);
            
            p.setTipoTarjeta(EnumPago.valueOf(
                rs.getString("tipoTarjeta")
            ));

            p.setNumeroTarjeta(rs.getString("numeroTarjeta"));
            p.setTitular(rs.getString("titular"));

            if (rs.getDate("fechaVencimiento") != null) {
                p.setFechaVencimiento(
                    rs.getDate("fechaVencimiento").toLocalDate()
                );
            }

            p.setCodigoSeguridad(rs.getString("codigoSeguridad"));

            if (rs.getTimestamp("fechaCreacion") != null) {
                p.setFechaCreacion(
                    rs.getTimestamp("fechaCreacion").toLocalDateTime()
                );
            }

            p.setMonto(rs.getBigDecimal("monto"));

            listaPagos.add(p);
        }
    }

    return listaPagos;
}

    public int agregarPago(Pago pago) throws SQLException {
        if (pago == null) {
            throw new SQLException("No se proporcionaron datos de pago.");
        }

        if (pago.getReserva() == null || pago.getReserva().getIdReserva() <= 0) {
            throw new SQLException("El pago debe estar asociado a una reserva válida.");
        }

        if (pago.getFechaCreacion() == null) {
            pago.setFechaCreacion(LocalDateTime.now());
        }

        String sql = "INSERT INTO pago (idReserva, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, monto, fechaCreacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                throw new SQLException("No se pudo establecer conexión con la base de datos.");
            }

            try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, pago.getReserva().getIdReserva());

                // Evitar valores nulos en columnas obligatorias del esquema.
                ps.setString(2, pago.getTipoTarjeta() != null ? pago.getTipoTarjeta().name() : "Desconocido");
                ps.setString(3, pago.getNumeroTarjeta() != null ? pago.getNumeroTarjeta() : "");
                ps.setString(4, pago.getTitular() != null ? pago.getTitular() : "");

                if (pago.getFechaVencimiento() != null) {
                    ps.setDate(5, java.sql.Date.valueOf(pago.getFechaVencimiento()));
                } else {
                    ps.setNull(5, Types.DATE);
                }

                ps.setString(6, pago.getCodigoSeguridad() != null ? pago.getCodigoSeguridad() : "");
                ps.setBigDecimal(7, pago.getMonto() != null ? pago.getMonto() : java.math.BigDecimal.ZERO);
                ps.setTimestamp(8, Timestamp.valueOf(pago.getFechaCreacion()));

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
                    try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                        if (rs.next()) {
                            idGenerado = rs.getInt(1);
                        }
                    }
                }

                return idGenerado > 0 ? idGenerado : filas;
            }
        }
    }

    public BigDecimal obtenerTotalPagos() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS totalPagos FROM pago";

        try (Connection conexion = Conexion.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("totalPagos");
            }
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal obtenerTotalPagosPorUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT COALESCE(SUM(p.monto), 0) AS totalPagos "
                + "FROM pago p "
                + "INNER JOIN reserva r ON p.idReserva = r.idReserva "
                + "WHERE r.idUsuario = ?";

        try (Connection conexion = Conexion.conectar();
                PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("totalPagos");
                }
            }
        }

        return BigDecimal.ZERO;
    }
}
