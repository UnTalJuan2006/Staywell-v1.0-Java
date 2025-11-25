package DAO;

import Controlador.Conexion;
import Modelo.Empleado;
import Modelo.EnumCargoEmpleado;
import Modelo.EnumEstadoEmpleado;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public List<Empleado> listar() throws SQLException {
        List<Empleado> listaEmpleados = new ArrayList<>();
        String sql = "SELECT * FROM empleado";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado m = new Empleado();
                m.setIdEmpleado(rs.getInt("idEmpleado"));
                m.setNombre(rs.getString("nombre"));
                m.setDocumento(rs.getString("documento"));
                m.setEmail(rs.getString("email"));
                m.setTelefono(rs.getString("telefono"));
                m.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                m.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                m.setCargo(EnumCargoEmpleado.valueOf(rs.getString("cargo")));

                Time entrada = rs.getTime("horarioEntrada");
                if (entrada != null) {
                    m.setHorarioEntrada(entrada.toLocalTime());
                }

                Time salida = rs.getTime("horarioSalida");
                if (salida != null) {
                    m.setHorarioSalida(salida.toLocalTime());
                }

                m.setEstado(EnumEstadoEmpleado.valueOf(rs.getString("estado")));

                listaEmpleados.add(m);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar Empleados: " + e.getMessage());
            throw e;
        }

        return listaEmpleados;
    }

    public void agregar(Empleado m) throws SQLException {
        String sql = "INSERT INTO empleado(nombre, documento, email, telefono, fechaCreacion, fechaActualizacion, cargo, horarioEntrada, horarioSalida, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getDocumento());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getTelefono());
            ps.setTimestamp(5, Timestamp.valueOf(m.getFechaCreacion()));
            ps.setTimestamp(6, Timestamp.valueOf(m.getFechaActualizacion()));
            ps.setString(7, m.getCargo().name());
            if (m.getHorarioEntrada() != null) {
                ps.setTime(8, Time.valueOf(m.getHorarioEntrada()));
            } else {
                ps.setNull(8, java.sql.Types.TIME);
            }

            if (m.getHorarioSalida() != null) {
                ps.setTime(9, Time.valueOf(m.getHorarioSalida()));
            } else {
                ps.setNull(9, java.sql.Types.TIME);
            }
            ps.setString(10, m.getEstado().name());

            ps.executeUpdate();

            System.out.println(" Empleado agregado con éxito: " + m.getNombre());

        } catch (SQLException e) {
            System.out.println(" Error al registrar empleado: " + e.getMessage());
            throw e;
        }
    }

    public void actualizar(Empleado m) throws SQLException {
        String sql = "UPDATE empleado SET nombre = ?, documento = ?, email = ?, telefono = ?, "
                + "fechaActualizacion = ?, cargo = ?, horarioEntrada = ?, horarioSalida = ?, estado = ? "
                + "WHERE idEmpleado = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

            ps.setString(1, m.getNombre());
            ps.setString(2, m.getDocumento());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getTelefono());
            ps.setTimestamp(5, Timestamp.valueOf(m.getFechaActualizacion()));
            ps.setString(6, m.getCargo().name());

            if (m.getHorarioEntrada() != null) {
                ps.setTime(7, Time.valueOf(m.getHorarioEntrada()));
            } else {
                ps.setNull(7, java.sql.Types.TIME);
            }

            if (m.getHorarioSalida() != null) {
                ps.setTime(8, Time.valueOf(m.getHorarioSalida()));
            } else {
                ps.setNull(8, java.sql.Types.TIME);
            }

            ps.setString(9, m.getEstado().name());
            ps.setInt(10, m.getIdEmpleado());

            ps.executeUpdate();
            System.out.println("Empleado actualizado correctamente");
        } catch (SQLException e) {
            System.out.println("Error al actualizar empleado: " + e.getMessage());
            throw e;
        }
    }

    public Empleado buscarPorId(int idEmpleado) throws SQLException {
        Empleado m = null;
        String sql = "SELECT * FROM empleado WHERE idEmpleado = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    m = new Empleado();

                    m.setIdEmpleado(rs.getInt("idEmpleado"));
                    m.setNombre(rs.getString("nombre"));
                    m.setDocumento(rs.getString("documento"));
                    m.setEmail(rs.getString("email"));
                    m.setTelefono(rs.getString("telefono"));
                    m.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                    m.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());

                    String cargoStr = rs.getString("cargo");
                    if (cargoStr != null) {
                        String normalizado = cargoStr.substring(0, 1).toUpperCase()
                                + cargoStr.substring(1).toLowerCase();
                        m.setCargo(EnumCargoEmpleado.valueOf(normalizado));
                    }

                    Time horaEntrada = rs.getTime("horarioEntrada");
                    if (horaEntrada != null) {
                        m.setHorarioEntrada(horaEntrada.toLocalTime());
                    }

                    Time horaSalida = rs.getTime("horarioSalida");
                    if (horaSalida != null) {
                        m.setHorarioSalida(horaSalida.toLocalTime());
                    }

                    String estadoStr = rs.getString("estado");
                    if (estadoStr != null) {
                        String normalizadoEstado = estadoStr.substring(0, 1).toUpperCase()
                                + estadoStr.substring(1).toLowerCase();
                        m.setEstado(EnumEstadoEmpleado.valueOf(normalizadoEstado));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar empleado: " + e.getMessage());
            throw e;
        }

        return m;
    }

    public void eliminar(Empleado m) throws SQLException {
        String sql = "DELETE FROM empleado WHERE idEmpleado = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, m.getIdEmpleado());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar el empleado");
            throw e;
        }
    }

    public List<Empleado> buscar(String filtro) throws SQLException {
        List<Empleado> lista = new ArrayList();
       String sql = "SELECT * FROM empleado "
           + "WHERE nombre LIKE ? OR email LIKE ? OR documento LIKE ?";


        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

            ps.setString(1, "%" + filtro + "%");
            ps.setString(2, "%" + filtro + "%");
            ps.setString(3, "%" + filtro + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Empleado m = new Empleado();
                    m.setIdEmpleado(rs.getInt("idEmpleado"));
                    m.setNombre(rs.getString("nombre"));
                    m.setDocumento(rs.getString("documento"));
                    m.setEmail(rs.getString("email"));
                    m.setTelefono(rs.getString("telefono"));

                    Timestamp fechaCreacion = rs.getTimestamp("fechaCreacion");
                    if (fechaCreacion != null) {
                        m.setFechaCreacion(fechaCreacion.toLocalDateTime());
                    }
                    Timestamp fechaActualizacion = rs.getTimestamp("fechaActualizacion");
                    if (fechaActualizacion != null) {
                        m.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                    }

                    String estado = rs.getString("estado");
                    if (estado != null) {
                        m.setEstado(EnumEstadoEmpleado.valueOf(estado));
                    }

                    Time timeEntrada = rs.getTime("horarioEntrada");
                    if (timeEntrada != null) {
                        m.setHorarioEntrada(timeEntrada.toLocalTime());
                    }

                    Time timeSalida = rs.getTime("horarioSalida");
                    if (timeSalida != null) {
                        m.setHorarioSalida(timeSalida.toLocalTime());
                    }
                    
                    lista.add(m);

                }
                rs.close();
                ps.close();
            }

        }catch(SQLException e){
             System.out.println("Error al buscar empleados: " + e.getMessage());
            throw e;
        }
        
        return lista;

    }
    
    public int contarPorCargo(EnumCargoEmpleado cargo) throws SQLException {
    String sql = "SELECT COUNT(*) AS total FROM empleado WHERE cargo = ?";

    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
        ps.setString(1, cargo.name());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) return rs.getInt("total");

    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
        throw e;
    }

    return 0;
}

    public int contarEmpleados() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM empleado";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

}
