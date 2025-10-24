package Converter;

import DAO.TipoHabitacionDAO;
import Modelo.TipoHabitacion;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.sql.SQLException;

@FacesConverter("tipoHabitacionConverter")
public class TipoHabitacionConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        try {
            int idTipo = Integer.parseInt(value);
            TipoHabitacionDAO tipoDAO = new TipoHabitacionDAO();
            return tipoDAO.buscar(idTipo);
        } catch (SQLException e) {
            System.out.println("Error al convertir tipo de habitación: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof TipoHabitacion) {
            TipoHabitacion tipo = (TipoHabitacion) value;
            return String.valueOf(tipo.getIdTipoHabitacion());
        }
        
        return "";
    }
}
