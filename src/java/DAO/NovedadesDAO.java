package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoNovedad;
import Modelo.Espacio;
import Modelo.Novedades;
import Modelo.Habitacion;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NovedadesDAO {

    public List<Novedades> listar() throws SQLException {
        List<Novedades> listaNovedades = new ArrayList<>();
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, "
                + "n.idHabitacion, n.idEspacio, h.numHabitacion, e.nombre AS nombreEspacio "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "ORDER BY n.fechaRegistro DESC";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaNovedades.add(mapearNovedad(rs));
            }
        }

        return listaNovedades;
    }

    public Novedades buscarPorId(int idNovedad) throws SQLException {
        Novedades novedad = null;
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, "
                + "n.idHabitacion, n.idEspacio, h.numHabitacion, e.nombre AS nombreEspacio "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "WHERE n.idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idNovedad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    novedad = mapearNovedad(rs);
                }
            }
        }

        return novedad;
    }

    public int agregar(Novedades novedad) throws SQLException {
        String sql = "INSERT INTO novedades (descripcion, fechaRegistro, fechaFin, estado, idHabitacion, idEspacio) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, novedad.getDescripcion());
            ps.setTimestamp(2, Timestamp.valueOf(novedad.getFechaRegistro()));
            if (novedad.getFechaFin() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(novedad.getFechaFin()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setString(4, novedad.getEstado() != null ? novedad.getEstado().name() : EnumEstadoNovedad.ACTIVA.name());

            if (novedad.getHabitacion() != null) {
                ps.setInt(5, novedad.getHabitacion().getIdHabitacion());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (novedad.getEspacio() != null) {
                ps.setInt(6, novedad.getEspacio().getIdEspacio());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        return -1;
    }

    public void actualizar(Novedades novedad) throws SQLException {
        String sql = "UPDATE novedades SET descripcion = ?, fechaRegistro = ?, fechaFin = ?, estado = ?, idHabitacion = ?, idEspacio = ? "
                + "WHERE idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, novedad.getDescripcion());
            ps.setTimestamp(2, Timestamp.valueOf(novedad.getFechaRegistro()));

            if (novedad.getFechaFin() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(novedad.getFechaFin()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setString(4, novedad.getEstado() != null ? novedad.getEstado().name() : null);

            if (novedad.getHabitacion() != null) {
                ps.setInt(5, novedad.getHabitacion().getIdHabitacion());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (novedad.getEspacio() != null) {
                ps.setInt(6, novedad.getEspacio().getIdEspacio());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, novedad.getIdNovedad());

            ps.executeUpdate();
        }
    }

    public void eliminar(Novedades novedad) throws SQLException {
        String sql = "DELETE FROM novedades WHERE idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, novedad.getIdNovedad());
            ps.executeUpdate();
        }
    }

    public List<Novedades> listarPorHabitacion(int idHabitacion) throws SQLException {
        List<Novedades> lista = new ArrayList<>();
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, n.idHabitacion, n.idEspacio, "
                + "h.numHabitacion, e.nombre AS nombreEspacio "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "WHERE n.idHabitacion = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearNovedad(rs));
                }
            }
        }

        return lista;
    }

    public List<Novedades> listarPorEspacio(int idEspacio) throws SQLException {
        List<Novedades> lista = new ArrayList<>();
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, n.idHabitacion, n.idEspacio, "
                + "h.numHabitacion, e.nombre AS nombreEspacio "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "WHERE n.idEspacio = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idEspacio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearNovedad(rs));
                }
            }
        }

        return lista;
    }

    private Novedades mapearNovedad(ResultSet rs) throws SQLException {
        Novedades novedad = new Novedades();
        novedad.setIdNovedad(rs.getInt("idNovedad"));
        novedad.setDescripcion(rs.getString("descripcion"));

        Timestamp fechaRegistro = rs.getTimestamp("fechaRegistro");
        if (fechaRegistro != null) {
            novedad.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        Timestamp fechaFin = rs.getTimestamp("fechaFin");
        if (fechaFin != null) {
            novedad.setFechaFin(fechaFin.toLocalDateTime());
        }

        String estado = rs.getString("estado");
        if (estado != null && !estado.isEmpty()) {
            novedad.setEstado(EnumEstadoNovedad.valueOf(estado));
        }

        int idHabitacion = rs.getInt("idHabitacion");
        if (!rs.wasNull()) {
            Habitacion habitacion = new Habitacion();
            habitacion.setIdHabitacion(idHabitacion);
            habitacion.setNumHabitacion(rs.getInt("numHabitacion"));
            novedad.setHabitacion(habitacion);
        }

        int idEspacio = rs.getInt("idEspacio");
        if (!rs.wasNull()) {
            Espacio espacio = new Espacio();
            espacio.setIdEspacio(idEspacio);
            espacio.setNombre(rs.getString("nombreEspacio"));
            novedad.setEspacio(espacio);
        }

        return novedad;
    }
}
