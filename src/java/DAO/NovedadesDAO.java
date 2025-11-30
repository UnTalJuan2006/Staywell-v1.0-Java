package DAO;

import Controlador.Conexion;
import Modelo.EnumEstadoNovedad;
import Modelo.Espacio;
import Modelo.Habitacion;
import Modelo.Novedades;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NovedadesDAO {

    public List<Novedades> listar() throws SQLException {
        List<Novedades> lista = new ArrayList<>();
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, "
                + "n.idHabitacion, n.idEspacio, h.numHabitacion, e.nombre "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "ORDER BY n.fechaRegistro DESC";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearNovedad(rs));
            }
        }

        return lista;
    }

    public Novedades buscarPorId(int id) throws SQLException {
        String sql = "SELECT n.idNovedad, n.descripcion, n.fechaRegistro, n.fechaFin, n.estado, "
                + "n.idHabitacion, n.idEspacio, h.numHabitacion, e.nombre "
                + "FROM novedades n "
                + "LEFT JOIN habitacion h ON h.idHabitacion = n.idHabitacion "
                + "LEFT JOIN espacio e ON e.idEspacio = n.idEspacio "
                + "WHERE n.idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearNovedad(rs);
                }
            }
        }

        return null;
    }

    public void insertar(Novedades novedad) throws SQLException {
        String sql = "INSERT INTO novedades (descripcion, fechaRegistro, fechaFin, estado, idHabitacion, idEspacio) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, novedad.getDescripcion());
            ps.setTimestamp(2, Timestamp.valueOf(novedad.getFechaRegistro()));
            if (novedad.getFechaFin() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(novedad.getFechaFin()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setString(4, novedad.getEstado().name());

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
        }
    }

    public void actualizar(Novedades novedad) throws SQLException {
        String sql = "UPDATE novedades SET descripcion = ?, fechaFin = ?, estado = ?, idHabitacion = ?, idEspacio = ? "
                + "WHERE idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, novedad.getDescripcion());

            if (novedad.getFechaFin() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(novedad.getFechaFin()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setString(3, novedad.getEstado().name());

            if (novedad.getHabitacion() != null) {
                ps.setInt(4, novedad.getHabitacion().getIdHabitacion());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (novedad.getEspacio() != null) {
                ps.setInt(5, novedad.getEspacio().getIdEspacio());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, novedad.getIdNovedad());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idNovedad) throws SQLException {
        String sql = "DELETE FROM novedades WHERE idNovedad = ?";

        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setInt(1, idNovedad);
            ps.executeUpdate();
        }
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM novedades";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int contarPorEstado(EnumEstadoNovedad estado) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM novedades WHERE estado = ?";
        try (PreparedStatement ps = Conexion.conectar().prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private Novedades mapearNovedad(ResultSet rs) throws SQLException {
        Novedades novedad = new Novedades();
        novedad.setIdNovedad(rs.getInt("idNovedad"));
        novedad.setDescripcion(rs.getString("descripcion"));

        Timestamp registro = rs.getTimestamp("fechaRegistro");
        if (registro != null) {
            novedad.setFechaRegistro(registro.toLocalDateTime());
        } else {
            novedad.setFechaRegistro(LocalDateTime.now());
        }

        Timestamp fin = rs.getTimestamp("fechaFin");
        if (fin != null) {
            novedad.setFechaFin(fin.toLocalDateTime());
        }

        String estado = rs.getString("estado");
        if (estado != null) {
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
            espacio.setNombre(rs.getString("nombre"));
            novedad.setEspacio(espacio);
        }

        return novedad;
    }
}
