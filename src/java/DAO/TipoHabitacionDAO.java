package DAO;

import Controlador.Conexion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import Modelo.TipoHabitacion;

public class TipoHabitacionDAO {

    public List<TipoHabitacion> listar() throws SQLException {
        List<TipoHabitacion> listaTipoHabitaciones = new ArrayList<>();
        String sql = "SELECT * FROM tipohabitacion";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                listaTipoHabitaciones.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar Tipos: " + e.getMessage());
            throw e;
        }

        return listaTipoHabitaciones;
    }

    public void agregar(TipoHabitacion t) throws SQLException {
        String sql = "INSERT INTO tipohabitacion(nombre, descripcion, capacidad, precio, imagen) VALUES(?,?,?,?,?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {

            ps.setString(1, t.getNombre());
            ps.setString(2, t.getDescripcion());
            ps.setInt(3, t.getCapacidad());
            ps.setFloat(4, t.getPrecio());
            ps.setString(5, t.getImagen());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo insertar el tipo de habitación");
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar tipo: " + e.getMessage());
            throw e;
        }
    }

    public TipoHabitacion buscarPorId(int idTipoHabitacion) throws SQLException {
        TipoHabitacion t = null;
        String sql = "SELECT * FROM tipohabitacion WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idTipoHabitacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = new TipoHabitacion();
                    t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                    t.setNombre(rs.getString("nombre"));
                    t.setDescripcion(rs.getString("descripcion"));
                    t.setCapacidad(rs.getInt("capacidad"));
                    t.setPrecio(rs.getFloat("precio"));
                    t.setImagen(rs.getString("imagen"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar TipoHabitacion: " + e.getMessage());
            throw e;
        }

        return t;
    }

    public void eliminar(TipoHabitacion t) throws SQLException {
        String sql = "DELETE FROM tipohabitacion WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, t.getIdTipoHabitacion());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar el tipo");
            throw e;
        }

    }

    public void actualizar(TipoHabitacion t) throws SQLException {
        String sql = "UPDATE tipohabitacion SET nombre = ?, descripcion = ?, capacidad = ?, precio = ?, imagen = ? WHERE idTipoHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getDescripcion());
            ps.setInt(3, t.getCapacidad());
            ps.setFloat(4, t.getPrecio());
            ps.setString(5, t.getImagen());
            ps.setInt(6, t.getIdTipoHabitacion());

            ps.executeUpdate();
            System.out.println("Tipo de habitación actualizado correctamente.");

        }
    }

    public List<TipoHabitacion> buscar(String filtro) throws SQLException {
        List<TipoHabitacion> lista = new ArrayList<>();

        String sql = "SELECT * FROM tipohabitacion "
                + "WHERE nombre LIKE ? "
                + "OR precio = ? "
                + "ORDER BY precio ASC";

        PreparedStatement ps = Conexion.conectar().prepareStatement(sql);

        ps.setString(1, "%" + filtro + "%");

        try {
            float precio = Float.parseFloat(filtro);
            ps.setFloat(2, precio);
        } catch (NumberFormatException e) {
            ps.setFloat(2, -1); // ningún precio será -1
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            TipoHabitacion t = new TipoHabitacion();
            t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
            t.setNombre(rs.getString("nombre"));
            t.setDescripcion(rs.getString("descripcion"));
            t.setCapacidad(rs.getInt("capacidad"));
            t.setPrecio(rs.getFloat("precio"));
            t.setImagen(rs.getString("imagen"));
            lista.add(t);
        }

        rs.close();
        ps.close();

        return lista;
    }

    public List<TipoHabitacion> listarPorPrecioAsc() throws SQLException {
        String sql = "SELECT * FROM tipohabitacion ORDER BY precio ASC";
        List<TipoHabitacion> lista = new ArrayList<>();

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                lista.add(t);
            }
        }
        return lista;
    }

    public List<TipoHabitacion> listarPorPrecioDesc() throws SQLException {
        String sql = "SELECT * FROM tipohabitacion ORDER BY precio DESC";
        List<TipoHabitacion> lista = new ArrayList<>();

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                lista.add(t);
            }
        }
        return lista;
    }
// 1. Total de tipos de habitación

    public int totalTipos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM tipohabitacion";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

// 2. Capacidad máxima disponible
    public int capacidadMaxima() throws SQLException {
        String sql = "SELECT MAX(capacidad) AS maxCapacidad FROM tipohabitacion";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("maxCapacidad");
            }
        }
        return 0;
    }

// 3. Precio promedio
    public float precioPromedio() throws SQLException {
        String sql = "SELECT AVG(precio) AS promedio FROM tipohabitacion";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getFloat("promedio");
            }
        }
        return 0f;
    }
    
    public List<TipoHabitacion> filtrarPorNombre(String nombre) throws SQLException {
    List<TipoHabitacion> lista = new ArrayList<>();

    String sql = "SELECT * FROM tipohabitacion WHERE nombre LIKE ?";
    try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
        ps.setString(1, "%" + nombre + "%");

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoHabitacion t = new TipoHabitacion();
                t.setIdTipoHabitacion(rs.getInt("idTipoHabitacion"));
                t.setNombre(rs.getString("nombre"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setCapacidad(rs.getInt("capacidad"));
                t.setPrecio(rs.getFloat("precio"));
                t.setImagen(rs.getString("imagen"));
                lista.add(t);
            }
        }
    }
    return lista;
}

}
