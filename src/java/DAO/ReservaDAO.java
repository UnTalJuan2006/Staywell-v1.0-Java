
package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoReserva;
import Modelo.Reserva;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import Modelo.Habitacion;
import Modelo.Usuario;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;



public class ReservaDAO {
    PreparedStatement ps;
    ResultSet rs;
    
public void agregarReserva(Reserva reserva) throws SQLException {
    String sql = "INSERT INTO reserva (checkin, checkout, fechaReserva, estado, nombreCliente, email, telefono, observaciones, idHabitacion, idUsuario) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
        ps.setTimestamp(1, Timestamp.valueOf(reserva.getCheckin()));
        ps.setTimestamp(2, Timestamp.valueOf(reserva.getCehckout())); 
        ps.setTimestamp(3, Timestamp.valueOf(reserva.getFechaReserva()));
        ps.setString(4, reserva.getEstado().name()); 
        ps.setString(5, reserva.getNombreCliente());
        ps.setString(6, reserva.getEmail());
        ps.setString(7, reserva.getTelefono());
        ps.setString(8, reserva.getObservaciones());
        ps.setInt(9, reserva.getHabitacion().getIdHabitacion());
        ps.setInt(10, reserva.getUsuario().getIdUsuario());
        
        ps.executeUpdate();
        System.out.println("✅ Reserva agregada correctamente.");
    } catch (SQLException e) {
        System.err.println("❌ Error al agregar la reserva: " + e.getMessage());
        throw e;
    }
}

}
