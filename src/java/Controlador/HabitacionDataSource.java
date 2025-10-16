package Controlador;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import Modelo.Habitacion;
import Modelo.EnumEstadoHabitacion;
import Modelo.EnumTipoHabitacion;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class HabitacionDataSource implements JRDataSource {
    private List<Habitacion> lstHabitacion;
    private int indice = -1;

    public HabitacionDataSource() {
        lstHabitacion = new ArrayList<>();
        try {
            lstHabitacion = listar(); // aquí ya cargas los datos
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para consultar BD y llenar la lista
    private List<Habitacion> listar() throws SQLException {
        List<Habitacion> listaHabitaciones = new ArrayList<>();

        String sql = "SELECT * FROM habitacion";
        try (
            PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Habitacion h = new Habitacion();
                h.setIdHabitacion(rs.getInt("idHabitacion"));
                h.setNumHabitacion(rs.getInt("numHabitacion"));

                String estado = rs.getString("estado");
                if (estado != null) {
                    try {
                        h.setEstado(EnumEstadoHabitacion.valueOf(estado.trim()));
                    } catch (IllegalArgumentException ex) {
                        h.setEstado(null);
                    }
                }

                if (rs.getTimestamp("fechaCreacion") != null) {
                    h.setFechaCreacion(rs.getTimestamp("fechaCreacion").toLocalDateTime());
                }
                if (rs.getTimestamp("fechaActualizacion") != null) {
                    h.setFechaActualizacion(rs.getTimestamp("fechaActualizacion").toLocalDateTime());
                }

                String tipo = rs.getString("tipoHabitacion");
                if (tipo != null) {
                    try {
                        h.setTipoHabitacion(EnumTipoHabitacion.valueOf(tipo.trim()));
                    } catch (IllegalArgumentException ex) {
                        h.setTipoHabitacion(null);
                    }
                }

                listaHabitaciones.add(h);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar habitaciones: " + e.getMessage());
        }

        return listaHabitaciones;
    }

    @Override
    public boolean next() throws JRException {
        indice++;
        return indice < lstHabitacion.size();
    }

    @Override
    public Object getFieldValue(JRField jrf) throws JRException {
        Habitacion hab = lstHabitacion.get(indice);
        switch (jrf.getName()) {
            case "numHabitacion": return hab.getNumHabitacion();
            case "estado": return hab.getEstado() != null ? hab.getEstado().name() : "";
            case "fechaCreacion": return hab.getFechaCreacion() != null ? java.sql.Timestamp.valueOf(hab.getFechaCreacion()) : null;
            case "fechaActualizacion": return hab.getFechaActualizacion() != null ? java.sql.Timestamp.valueOf(hab.getFechaActualizacion()) : null;
            case "tipoHabitacion": return hab.getTipoHabitacion() != null ? hab.getTipoHabitacion().name() : "";
            default: return null;
        }
    }
}
