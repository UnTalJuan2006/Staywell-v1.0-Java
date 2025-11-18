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
import Modelo.EnumCargoEmpleado;
import Modelo.EnumEstadoEmpleado;

public class AsistenciaDAO {

    PreparedStatement ps;
    ResultSet rs;

    public List<Asistencia> listar() throws SQLException {
        List<Asistencia> lista = new ArrayList<>();

        String sql = "SELECT a.idAsistencia, a.fecha, a.horaEntrada, a.horaSalida, a.observacion, "
                + "e.idEmpleado, e.nombre, e.documento, e.email, e.telefono, "
                + "e.fechaCreacion, e.fechaActualizacion, e.cargo, "
                + "e.horarioEntrada AS empHorarioEntrada, "
                + "e.horarioSalida AS empHorarioSalida, "
                + "e.estado "
                + "FROM asistencia a "
                + "LEFT JOIN empleado e ON a.idEmpleado = e.idEmpleado";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAsistencia(rs));
            }
        }

        return lista;
    }

public void registrarEntrada(Asistencia a) throws SQLException {
    String sql = "INSERT INTO asistencia (idEmpleado, fecha, horaEntrada, observacion) VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

        // idEmpleado
        ps.setInt(1, a.getEmpleado().getIdEmpleado());

        
        if (a.getFecha() != null) {
            ps.setDate(2, new java.sql.Date(a.getFecha().getTime()));
        } else {
            ps.setNull(2, Types.DATE);
        }

        // Hora Entrada (LocalTime → Time)
        if (a.getHoraEntrada() != null) {
            ps.setTime(3, Time.valueOf(a.getHoraEntrada()));
        } else {
            ps.setNull(3, Types.TIME);
        }

        ps.setString(4, a.getObservacion());

        ps.executeUpdate();

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Entrada registrada correctamente"));
    }
}


public void registrarSalida(Asistencia a) throws SQLException {
    String sql = "UPDATE asistencia SET horaSalida = ? WHERE idAsistencia = ?";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

        // Hora de salida
        ps.setTime(1, Time.valueOf(a.getHoraSalida()));

        // idAsistencia
        ps.setInt(2, a.getIdAsistencia());

        int filasActualizadas = ps.executeUpdate();

        if (filasActualizadas > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Salida registrada correctamente"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se encontró asistencia para actualizar"));
        }
    }
}


    private Asistencia mapearAsistencia(ResultSet rs) throws SQLException {
    Asistencia asistencia = new Asistencia();

    asistencia.setIdAsistencia(rs.getInt("idAsistencia"));

    java.util.Date fecha = rs.getDate("fecha");
    if (fecha != null) {
        asistencia.setFecha(fecha);
    }

    Time entrada = rs.getTime("horaEntrada");
    asistencia.setHoraEntrada(entrada != null ? entrada.toLocalTime() : null);

    Time salida = rs.getTime("horaSalida");
    asistencia.setHoraSalida(salida != null ? salida.toLocalTime() : null);

    asistencia.setObservacion(rs.getString("observacion"));

    // 🔥 MAPEAMOS EL EMPLEADO COMPLETO
    Empleado empleado = new Empleado();
    empleado.setIdEmpleado(rs.getInt("idEmpleado"));
    empleado.setNombre(rs.getString("nombre"));
    empleado.setDocumento(rs.getString("documento"));
    empleado.setEmail(rs.getString("email"));
    empleado.setTelefono(rs.getString("telefono"));

    // 🔥 ESTO FALTABA
    asistencia.setEmpleado(empleado);

    return asistencia;
}


}
