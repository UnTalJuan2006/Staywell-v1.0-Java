package Converter;

import DAO.EmpleadoDAO;
import Modelo.Empleado;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "empleadoConverter")
public class EmpleadoConverter implements Converter {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            return empleadoDAO.buscar(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Empleado) {
            return String.valueOf(((Empleado) value).getIdEmpleado());
        }
        return "";
    }
}
