package DAO;

import Controlador.Conexion;
import Modelo.Evento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import Modelo.Espacio;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class EventoDAO {
    PreparedStatement ps;
    ResultSet rs;
    
    public List<Evento> listar() throws SQLException{
        List<Evento> listaEvento = new ArrayList<>();
        EspacioDAO espDAO = new EspacioDAO();
        
        try{
            String sql = "SELECT * FROM evento";
            ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();
            
            while(rs.next()){
                Evento v = new Evento();
                v.setIdEvento(rs.getInt("idEvento"));
                v.setNombreEvento(rs.getString("nombreEvento"));
                v.setDescripcion(rs.getString("descripcion"));
                v.setFechaEvento(rs.getDate("fechaEvento"));
                v.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                v.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                
                 Time horaInicio = rs.getTime("horaInicio");
                if (horaInicio != null) {
                    v.setHoraInicio(horaInicio.toLocalTime());
                }

                Time horaFin = rs.getTime("horaFin");
                if (horaFin != null) {
                    v.setHoraFin(horaFin.toLocalTime());
                }
                
                v.setNombreCliente(rs.getString("nombreCliente"));
                v.setEspacio(espDAO.buscar(rs.getInt("idEspacio")));
                
                listaEvento.add(v);
            }
        }catch(SQLException e ){
              FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error listando asistencias"));
        }
        return listaEvento;
    }
    
   public void agregarEventoHuesped(Evento evento) throws SQLException{
    try {
        String sql = "INSERT INTO evento (nombreEvento, descripcion, fechaEvento, horaInicio, horaFin, fechaCreacion, fechaActualizacion, nombreCliente, idEspacio) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        ps = Conexion.conectar().prepareStatement(sql);
        ps.setString(1, evento.getNombreEvento());
        ps.setString(2, evento.getDescripcion());
        ps.setDate(3, new Date(evento.getFechaEvento().getTime())); 
        ps.setTime(4, Time.valueOf(evento.getHoraInicio()));
        ps.setTime(5, Time.valueOf(evento.getHoraFin()));
        ps.setTimestamp(6, Timestamp.valueOf(evento.getFechaCreacion()));
        ps.setTimestamp(7, Timestamp.valueOf(evento.getFechaActualizacion()));
        ps.setString(8, evento.getNombreCliente());
        ps.setInt(9, evento.getEspacio().getIdEspacio()); 

        ps.executeUpdate();

        
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva creada correctamente"));
        
    } catch (SQLException e) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al crear la reserva: " + e.getMessage()));
        System.out.println("Error al agregar evento (huésped): " + e.getMessage());
    }
}
    

 
}