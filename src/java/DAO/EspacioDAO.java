package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoEspacio;
import Modelo.EnumTipoEspacios;
import Modelo.Espacio;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EspacioDAO {

    PreparedStatement ps;
    ResultSet rs;

    // Listar espacios
    public List<Espacio> listar() throws SQLException {
        List<Espacio> listaEspacios = new ArrayList<>();

        String sql = "SELECT * FROM espacio";
        ps = Conexion.conectar().prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            Espacio p = new Espacio();
            p.setIdEspacio(rs.getInt("idEspacio"));
            p.setNombre(rs.getString("nombre"));
            p.setTipo(EnumTipoEspacios.valueOf(rs.getString("tipo")));
            p.setDescripcion(rs.getString("descripcion"));
            p.setCapacidad(rs.getInt("capacidad"));
            p.setCostoHora(rs.getFloat("costoHora"));
            p.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
            p.setEstado(EnumEstadoEspacio.valueOf(rs.getString("estado")));
            p.setImagen(rs.getString("imagen"));

            listaEspacios.add(p);
        }

        return listaEspacios;
    }

    // Agregar espacio
    public void agregar(Espacio p) throws SQLException {
        String sql = "INSERT INTO espacio (nombre, tipo, descripcion, capacidad, costoHora, fechaActualizacion, estado, imagen) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        ps = Conexion.conectar().prepareStatement(sql);
        ps.setString(1, p.getNombre());
        ps.setString(2, p.getTipo().name());
        ps.setString(3, p.getDescripcion());
        ps.setInt(4, p.getCapacidad());
        ps.setFloat(5, p.getCostoHora());
        ps.setTimestamp(6, Timestamp.valueOf(p.getFechaActualizacion()));
        ps.setString(7, p.getEstado().name());
        ps.setString(8, p.getImagen());

        ps.executeUpdate();
    }

    public Espacio buscar(int id) {
        Espacio p = null;
        try {
            String sql = "SELECT * FROM espacio WHERE idEspacio = ?";
            ps = Conexion.conectar().prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                p = new Espacio();
                p.setIdEspacio(rs.getInt("idEspacio"));
                p.setNombre(rs.getString("nombre")); // ← CORREGIDO
                p.setTipo(EnumTipoEspacios.valueOf(rs.getString("tipo")));
                p.setDescripcion(rs.getString("descripcion"));
                p.setCapacidad(rs.getInt("capacidad"));
                p.setCostoHora(rs.getFloat("costoHora")); // ← ojo con el nombre de columna
                p.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                p.setEstado(EnumEstadoEspacio.valueOf(rs.getString("estado")));
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar espacio: " + e.getMessage());
        }
        return p;
    }

}
