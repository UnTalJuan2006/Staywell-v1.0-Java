package Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "localTimeConverter", forClass = LocalTime.class)
public class LocalTimeConverter implements Converter {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(value.trim(), INPUT_FORMATTER);
        } catch (DateTimeParseException ex) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Hora inválida",
                    "Utiliza el formato HH:mm (por ejemplo 09:30).");
            throw new ConverterException(msg, ex);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof LocalTime) {
            return ((LocalTime) value).format(OUTPUT_FORMATTER);
        }

        return value.toString();
    }
}
