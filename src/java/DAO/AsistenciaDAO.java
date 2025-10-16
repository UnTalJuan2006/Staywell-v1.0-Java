package DAO;


import Controlador.Conexion;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import Modelo.Asistencia;
import Modelo.Empleado;

public class AsistenciaDAO {

    PreparedStatement ps;
    ResultSet rs;

    // ✅ Listar todas las asistencias con los datos del empleado
    public List<Asistencia> listar()  throws SQLException{
        List<Asistencia> lista = new ArrayList<>();
        EmpleadoDAO empDAO = new EmpleadoDAO();

        try {
            String sql = "SELECT * FROM asistencia";
            ps = Conexion.conectar().prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Asistencia a = new Asistencia();
                a.setIdAsistencia(rs.getInt("idAsistencia"));
                a.setFecha(rs.getDate("fecha"));
                a.setHoraEntrada(rs.getTime("horaEntrada") != null ? rs.getTime("horaEntrada").toLocalTime() : null);
                a.setHoraSalida(rs.getTime("horaSalida") != null ? rs.getTime("horaSalida").toLocalTime() : null);
                a.setObservacion(rs.getString("observacion"));

                // Obtenemos el empleado asociado
                a.setEmpleado(empDAO.buscar(rs.getInt("idEmpleado")));

                lista.add(a);
            }

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error listando asistencias"));
        }

        return lista;
    }

    // ✅ Registrar entrada
   
 
    public void registrarEntrada(Asistencia a)  throws SQLException {
        try {
            String sql = "INSERT INTO asistencia (idEmpleado, fecha, horaEntrada, observacion) VALUES (?, ?, ?, ?)";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, a.getEmpleado().getIdEmpleado());
            ps.setDate(2, a.getFecha());
            ps.setTime(3, Time.valueOf(a.getHoraEntrada()));
            ps.setString(4, a.getObservacion());
            ps.executeUpdate();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Entrada registrada correctamente"));

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al registrar entrada: " + e.getMessage()));
            throw e;
        }
    }


    // ✅ Registrar salida
    public void registrarSalida(Asistencia a)  throws SQLException{
        try {
            String sql = "UPDATE asistencia SET horaSalida = ? WHERE idAsistencia = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setTime(1, Time.valueOf(a.getHoraSalida()));
            ps.setInt(2, a.getIdAsistencia());
            ps.executeUpdate();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso", "Salida registrada correctamente"));
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al registrar la salida"));
        }
    }

    // ✅ Buscar asistencia por ID
    public Asistencia buscar(int id) {
        Asistencia a = null;
        EmpleadoDAO empDAO = new EmpleadoDAO();

        try {
            String sql = "SELECT * FROM asistencia WHERE idAsistencia = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                a = new Asistencia();
                a.setIdAsistencia(rs.getInt("idAsistencia"));
                a.setFecha(rs.getDate("fecha"));
                a.setHoraEntrada(rs.getTime("horaEntrada") != null ? rs.getTime("horaEntrada").toLocalTime() : null);
                a.setHoraSalida(rs.getTime("horaSalida") != null ? rs.getTime("horaSalida").toLocalTime() : null);
                a.setObservacion(rs.getString("observacion"));
                a.setEmpleado(empDAO.buscar(rs.getInt("idEmpleado")));
            }

        } catch (SQLException e) {
            System.out.println("Error buscando asistencia: " + e.getMessage());
        }

        return a;
    }

}
