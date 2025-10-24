package Controlador;

import DAO.HabitacionDAO;
import DAO.ReservaDAO;
import DAO.TipoHabitacionDAO;
import DAO.UsuarioDAO;
import Modelo.EnumEstadoReserva;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import Modelo.Usuario;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

@ManagedBean
@ViewScoped
public class ReservaCalendarioBean implements Serializable {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private List<Reserva> reservas = new ArrayList<>();
    private List<Habitacion> habitaciones = new ArrayList<>();
    private List<TipoHabitacion> tiposHabitacion = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();

    @PostConstruct
    public void init() {
        recargarReservas();
        try {
            habitaciones = habitacionDAO.listar();
        } catch (SQLException e) {
            habitaciones = new ArrayList<>();
            agregarMensajeError("No se pudieron cargar las habitaciones disponibles.");
        }

        try {
            tiposHabitacion = tipoHabitacionDAO.listar();
        } catch (SQLException e) {
            tiposHabitacion = new ArrayList<>();
            agregarMensajeError("No se pudieron cargar los tipos de habitación.");
        }

        try {
            usuarios = usuarioDAO.listar();
        } catch (SQLException e) {
            usuarios = new ArrayList<>();
            agregarMensajeError("No se pudieron cargar los huéspedes.");
        }
    }

    public void recargarReservas() {
        try {
            reservas = reservaDAO.listar();
        } catch (SQLException e) {
            reservas = new ArrayList<>();
            agregarMensajeError("No se pudieron cargar las reservas del calendario.");
        }
    }

    public String getEventosJson() {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < reservas.size(); i++) {
            json.append(construirEventoJson(reservas.get(i)));
            if (i < reservas.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    public String getTiposHabitacionJson() {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < tiposHabitacion.size(); i++) {
            TipoHabitacion tipo = tiposHabitacion.get(i);
            json.append("{")
                    .append("\"id\":").append(tipo.getIdTipoHabitacion()).append(",")
                    .append("\"nombre\":\"").append(escaparJson(tipo.getNombre())).append("\",")
                    .append("\"descripcion\":\"").append(escaparJson(tipo.getDescripcion())).append("\",")
                    .append("\"capacidad\":").append(tipo.getCapacidad()).append(",")
                    .append("\"precio\":").append(tipo.getPrecio())
                    .append("}");
            if (i < tiposHabitacion.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    public String getHabitacionesJson() {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < habitaciones.size(); i++) {
            Habitacion habitacion = habitaciones.get(i);
            TipoHabitacion tipo = habitacion.getTipoHabitacion();
            json.append("{")
                    .append("\"id\":").append(habitacion.getIdHabitacion()).append(",")
                    .append("\"numero\":").append(habitacion.getNumHabitacion()).append(",")
                    .append("\"tipoId\":").append(tipo != null ? tipo.getIdTipoHabitacion() : 0).append(",")
                    .append("\"tipoNombre\":\"").append(escaparJson(tipo != null ? tipo.getNombre() : "")).append("\"")
                    .append("}");
            if (i < habitaciones.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    public String getUsuariosJson() {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario usuario = usuarios.get(i);
            json.append("{")
                    .append("\"id\":").append(usuario.getIdUsuario()).append(",")
                    .append("\"nombre\":\"").append(escaparJson(usuario.getNombre())).append("\",")
                    .append("\"email\":\"").append(escaparJson(usuario.getEmail())).append("\",")
                    .append("\"telefono\":\"").append(escaparJson(usuario.getTelefono())).append("\"")
                    .append("}");
            if (i < usuarios.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    public void actualizarFechas() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        PrimeFaces primeFaces = PrimeFaces.current();

        try {
            int idReserva = Integer.parseInt(params.get("id"));
            LocalDateTime inicio = parsearFecha(params.get("start"));
            LocalDateTime fin = parsearFecha(params.get("end"));

            reservaDAO.actualizarFechas(idReserva, inicio, fin);
            recargarReservas();

            primeFaces.ajax().addCallbackParam("success", true);
        } catch (NumberFormatException | SQLException | DateTimeParseException e) {
            primeFaces.ajax().addCallbackParam("success", false);
            agregarMensajeError("No se pudo actualizar las fechas de la reserva seleccionada.");
        }
    }

    public void crearReservaDesdeCalendario() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        PrimeFaces primeFaces = PrimeFaces.current();

        try {
            LocalDateTime inicio = parsearFecha(params.get("start"));
            LocalDateTime fin = parsearFecha(params.get("end"));
            int habitacionId = Integer.parseInt(params.get("habitacionId"));
            int usuarioId = Integer.parseInt(params.get("usuarioId"));

            Habitacion habitacion = obtenerHabitacionPorId(habitacionId);
            Usuario usuario = obtenerUsuarioPorId(usuarioId);

            if (habitacion == null) {
                throw new IllegalArgumentException("Habitación no encontrada");
            }

            if (usuario == null) {
                throw new IllegalArgumentException("Huésped no encontrado");
            }

            Reserva reserva = new Reserva();
            reserva.setCheckin(inicio);
            reserva.setCehckout(fin);
            reserva.setFechaReserva(LocalDateTime.now());
            reserva.setEstado(obtenerEstado(params.get("estado")));
            reserva.setHabitacion(habitacion);
            reserva.setUsuario(usuario);

            String nombreCliente = params.get("clienteNombre");
            String email = params.get("email");
            String telefono = params.get("telefono");
            String observaciones = params.get("observaciones");

            reserva.setNombreCliente(nombreCliente != null && !nombreCliente.isEmpty() ? nombreCliente : usuario.getNombre());
            reserva.setEmail(email != null && !email.isEmpty() ? email : usuario.getEmail());
            reserva.setTelefono(telefono != null && !telefono.isEmpty() ? telefono : usuario.getTelefono());
            reserva.setObservaciones(observaciones);

            int nuevoId = reservaDAO.agregarReserva(reserva);
            if (nuevoId <= 0) {
                throw new SQLException("No se generó un identificador para la nueva reserva");
            }

            Reserva creada = reservaDAO.buscar(nuevoId);
            recargarReservas();

            primeFaces.ajax().addCallbackParam("success", true);
            primeFaces.ajax().addCallbackParam("evento", construirEventoJson(creada));
            agregarMensajeInformacion("Reserva creada correctamente.");
        } catch (NumberFormatException | SQLException | DateTimeParseException | IllegalArgumentException e) {
            primeFaces.ajax().addCallbackParam("success", false);
            agregarMensajeError("No se pudo crear la nueva reserva desde el calendario.");
        }
    }

    private Habitacion obtenerHabitacionPorId(int habitacionId) {
        return habitaciones.stream()
                .filter(h -> h.getIdHabitacion() == habitacionId)
                .findFirst()
                .orElseGet(() -> {
                    try {
                        for (Habitacion habitacion : habitacionDAO.listar()) {
                            if (habitacion.getIdHabitacion() == habitacionId) {
                                return habitacion;
                            }
                        }
                    } catch (SQLException e) {
                        agregarMensajeError("No se pudo obtener la habitación seleccionada.");
                    }
                    return null;
                });
    }

    private Usuario obtenerUsuarioPorId(int usuarioId) {
        return usuarios.stream()
                .filter(u -> u.getIdUsuario() == usuarioId)
                .findFirst()
                .orElseGet(() -> {
                    try {
                        for (Usuario usuario : usuarioDAO.listar()) {
                            if (usuario.getIdUsuario() == usuarioId) {
                                return usuario;
                            }
                        }
                    } catch (SQLException e) {
                        agregarMensajeError("No se pudo obtener el huésped seleccionado.");
                    }
                    return null;
                });
    }

    private String construirEventoJson(Reserva reserva) {
        if (reserva == null) {
            return "{}";
        }

        Habitacion habitacion = reserva.getHabitacion();
        Usuario usuario = reserva.getUsuario();
        TipoHabitacion tipoHabitacion = habitacion != null ? habitacion.getTipoHabitacion() : null;

        String titulo = "Habitación " + (habitacion != null ? habitacion.getNumHabitacion() : "Sin asignar");
        if (reserva.getNombreCliente() != null && !reserva.getNombreCliente().isEmpty()) {
            titulo += " • " + reserva.getNombreCliente();
        }

        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"id\":").append(reserva.getIdReserva()).append(",")
                .append("\"title\":\"").append(escaparJson(titulo)).append("\",")
                .append("\"start\":\"").append(formatearFecha(reserva.getCheckin())).append("\",")
                .append("\"end\":\"").append(formatearFecha(reserva.getCehckout())).append("\",")
                .append("\"extendedProps\":{")
                .append("\"habitacionId\":").append(habitacion != null ? habitacion.getIdHabitacion() : 0).append(",")
                .append("\"habitacionNumero\":").append(habitacion != null ? habitacion.getNumHabitacion() : 0).append(",")
                .append("\"tipoHabitacion\":\"").append(escaparJson(tipoHabitacion != null ? tipoHabitacion.getNombre() : "")).append("\",")
                .append("\"cliente\":\"").append(escaparJson(reserva.getNombreCliente())).append("\",")
                .append("\"email\":\"").append(escaparJson(reserva.getEmail())).append("\",")
                .append("\"telefono\":\"").append(escaparJson(reserva.getTelefono())).append("\",")
                .append("\"estado\":\"").append(reserva.getEstado() != null ? reserva.getEstado().name() : "").append("\",")
                .append("\"observaciones\":\"").append(escaparJson(reserva.getObservaciones())).append("\",")
                .append("\"usuarioId\":").append(usuario != null ? usuario.getIdUsuario() : 0)
                .append("}");
        json.append("}");

        return json.toString();
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? ISO_FORMATTER.format(fecha) : "";
    }

    private LocalDateTime parsearFecha(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException ex) {
            return OffsetDateTime.parse(valor).toLocalDateTime();
        }
    }

    private EnumEstadoReserva obtenerEstado(String estado) {
        if (estado == null || estado.isEmpty()) {
            return EnumEstadoReserva.ACTIVA;
        }
        try {
            return EnumEstadoReserva.valueOf(estado);
        } catch (IllegalArgumentException ex) {
            return EnumEstadoReserva.ACTIVA;
        }
    }

    private void agregarMensajeError(String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detalle));
    }

    private void agregarMensajeInformacion(String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", detalle));
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public List<TipoHabitacion> getTiposHabitacion() {
        return tiposHabitacion;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }
}
