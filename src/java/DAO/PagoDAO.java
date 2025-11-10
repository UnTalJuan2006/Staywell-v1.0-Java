
package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoReserva;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import Modelo.Usuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import Modelo.EnumPago;
import Modelo.Pago;

public class PagoDAO {

    public int agregarPago(Pago p) throws SQLException {
    String sql = "INSERT INTO pago (idReserva, tipoTarjeta, numeroTarjeta, titular, fechaVencimiento, codigoSeguridad, fechaCreacion) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, p.getReserva().getIdReserva());
        ps.setString(2, p.getTipoTarjeta().name());
        ps.setString(3, p.getNumeroTarjeta());
        ps.setString(4, p.getTitular());
        ps.setDate(5, java.sql.Date.valueOf(p.getFechaVencimiento())); 
        ps.setString(6, p.getCodigoSeguridad());
        ps.setTimestamp(7, Timestamp.valueOf(p.getFechaCreacion()));   

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
