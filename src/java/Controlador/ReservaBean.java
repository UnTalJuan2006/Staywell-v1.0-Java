package Controlador;

import DAO.HabitacionDAO;
import DAO.ReservaDAO;
import DAO.UsuarioDAO;
import Modelo.EnumEstadoReserva;
import Modelo.EnumRoles;
import Modelo.Habitacion;
import Modelo.Reserva;
import Modelo.TipoHabitacion;
import Modelo.Usuario;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class ReservaBean implements Serializable {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private List<TipoHabitacion> listaTipoHabitaciones;

    private Reserva reserva = new Reserva();
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Habitacion> listaHabitaciones = new ArrayList<>();
    private List<Usuario> listaUsuarios = new ArrayList<>();

    private Integer habitacionIdSeleccionada;
    private Integer usuarioIdSeleccionado;
    private String fechasOcupadasJson = "[]";

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HTML_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private Usuario usuarioLogueado;

    private List<Habitacion> habitaciones;
    private List<Habitacion> habitacionesFiltradas;
    private TipoHabitacion tipoSeleccionado;
    public List<Reserva> listarPorUsuario = new ArrayList<>();
    

  @PostConstruct
public void init() {
    FacesContext context = FacesContext.getCurrentInstance();

    try {
      
        if (context == null) {
            System.out.println("⚠ No hay FacesContext activo. Posible navegación directa o refresco fuera del ciclo JSF.");
            inicializarListasVacias();
            return;
        }

        
        usuarioLogueado = (Usuario) context.getExternalContext()
                .getSessionMap().get("usuarioLogueado");

        if (usuarioLogueado == null) {
            System.out.println("⚠ No hay usuario logueado en sesión. Se detiene la carga de datos.");
            inicializarListasVacias();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe iniciar sesión para acceder a las reservas."));
            return;
        }

 
        try {
            habitaciones = habitacionDAO.listar();
            habitacionesFiltradas = new ArrayList<>(habitaciones);
            listaHabitaciones = new ArrayList<>(habitaciones);
            listaUsuarios = usuarioDAO.listar();
        } catch (SQLException e) {
            System.out.println("❌ Error al cargar habitaciones o usuarios: " + e.getMessage());
            inicializarListasVacias();
        }
        
        
        
        try {
            if (usuarioLogueado.getRol() == EnumRoles.ADMIN) {
                cargarReservas(); // todas las reservas
            } else if (usuarioLogueado.getRol() == EnumRoles.HUESPED) {
                listarReservasDelUsuarioLogueado();
            } else {
                listaReservas = new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("❌ Error al cargar reservas: " + e.getMessage());
            listaReservas = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las reservas."));
        }

        System.out.println("✅ ReservaBean inicializado correctamente para el usuario: " + usuarioLogueado.getNombre());

    } catch (Exception ex) {
        System.out.println("❌ Error inesperado en init(): " + ex.getMessage());
        ex.printStackTrace();
        inicializarListasVacias();
    }
}


private void inicializarListasVacias() {
    listaReservas = new ArrayList<>();
    listaHabitaciones = new ArrayList<>();
    habitaciones = new ArrayList<>();
    habitacionesFiltradas = new ArrayList<>();
    listaUsuarios = new ArrayList<>();
}


    public void cargarReservas() {
        try {
            listaReservas = reservaDAO.listar();
        } catch (SQLException e) {
            listaReservas = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las reservas."));
        }
    }

    public void listarReservasDelUsuarioLogueado() {
        try {
            if (usuarioLogueado != null) {
                listarPorUsuario = reservaDAO.listarPorUsuario(usuarioLogueado.getIdUsuario());
            } else {
                listarPorUsuario = new ArrayList<>();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay un usuario logueado."));
            }
        } catch (SQLException e) {
            listarPorUsuario = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las reservas del usuario."));
            e.printStackTrace();
        }
    }

    public String guardarComoCliente() {
        try {
            if (!validarFechasBasicas(true)) {
                return null;
            }
            reserva.setUsuario(usuarioLogueado);

            Habitacion habitacion = obtenerHabitacionPorId(habitacionIdSeleccionada);
            if (habitacion == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar una habitación."));
                return null;
            }
            reserva.setHabitacion(habitacion);

            if (reserva.getNombreCliente() == null || reserva.getNombreCliente().isEmpty()) {
                reserva.setNombreCliente(usuarioLogueado.getNombre());
            }
            if (reserva.getEmail() == null || reserva.getEmail().isEmpty()) {
                reserva.setEmail(usuarioLogueado.getEmail());
            }
            if (reserva.getTelefono() == null || reserva.getTelefono().isEmpty()) {
                reserva.setTelefono(usuarioLogueado.getTelefono());
            }

            if (reserva.getFechaReserva() == null) {
                reserva.setFechaReserva(LocalDateTime.now());
            }

            reserva.setEstado(EnumEstadoReserva.ACTIVA);
            reservaDAO.reservaHuespd(reserva);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva registrada correctamente."));
            limpiarFormulario();
            return "HomeHuesped?faces-redirect=true";

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la reserva."));
            return null;
        }
    }

    public void cargarReservaPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Reserva reservaEncontrada = reservaDAO.buscar(id);
                if (reservaEncontrada != null) {
                    this.reserva = reservaEncontrada;
                    habitacionIdSeleccionada = (reserva.getHabitacion() != null)
                            ? reserva.getHabitacion().getIdHabitacion() : null;
                    usuarioIdSeleccionado = (reserva.getUsuario() != null)
                            ? reserva.getUsuario().getIdUsuario() : null;
                    refrescarOcupacionesHabitacion();
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "La reserva no existe."));
                }
            } catch (NumberFormatException | SQLException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar la reserva."));
            }
        }
    }

    public String guardar() {
        try {
            if (!validarFechasBasicas(true)) {
                return null;
            }

            if (!asignarRelaciones()) {
                return null;
            }

            if (!validarDisponibilidadFechas(true)) {
                return null;
            }

            if (reserva.getFechaReserva() == null) {
                reserva.setFechaReserva(LocalDateTime.now());
            }

            reservaDAO.agregarReserva(reserva);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva registrada correctamente."));
            limpiarFormulario();
            return "Reservas?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo registrar la reserva."));
            return null;
        }
    }

    public String actualizar() {
        try {
            if (!validarFechasBasicas(true)) {
                return null;
            }

            if (!asignarRelaciones()) {
                return null;
            }

            if (!validarDisponibilidadFechas(true)) {
                return null;
            }

            reservaDAO.actualizar(reserva);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva actualizada correctamente."));
            return "Reservas?faces-redirect=true";
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la reserva."));
            return null;
        }
    }

    public void eliminar(Reserva reservaSeleccionada) {
        try {
            reservaDAO.eliminar(reservaSeleccionada.getIdReserva());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Reserva eliminada correctamente."));
            cargarReservas();
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la reserva."));
        }
    }

    private boolean asignarRelaciones() {
        Habitacion habitacion = obtenerHabitacionPorId(habitacionIdSeleccionada);
        Usuario usuario = obtenerUsuarioPorId(usuarioIdSeleccionado);

        if (habitacion == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar una habitación."));
            return false;
        }

        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar un huésped."));
            return false;
        }

        reserva.setHabitacion(habitacion);
        reserva.setUsuario(usuario);

        if (reserva.getNombreCliente() == null || reserva.getNombreCliente().isEmpty()) {
            reserva.setNombreCliente(usuario.getNombre());
        }

        if (reserva.getEmail() == null || reserva.getEmail().isEmpty()) {
            reserva.setEmail(usuario.getEmail());
        }

        if (reserva.getTelefono() == null || reserva.getTelefono().isEmpty()) {
            reserva.setTelefono(usuario.getTelefono());
        }
        return true;
    }

    public void validarFechasAjax() {
        refrescarOcupacionesHabitacion();

        if (reserva.getCheckin() == null || reserva.getCheckout() == null || habitacionIdSeleccionada == null) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();

        if (!validarFechasBasicas(false)) {
            context.validationFailed();
            return;
        }

        if (!validarDisponibilidadFechas(false)) {
            context.validationFailed();
        }
    }

    private boolean validarFechasBasicas(boolean mostrarMensajeCamposIncompletos) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (reserva.getCheckin() == null || reserva.getCheckout() == null) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Información incompleta", "Debe indicar las fechas de check-in y check-out."));
            }
            return false;
        }

        if (!reserva.getCheckin().isBefore(reserva.getCheckout())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Fechas inválidas", "La fecha de check-out debe ser posterior al check-in."));
            return false;
        }

        return true;
    }

    private boolean validarDisponibilidadFechas(boolean mostrarMensajeCamposIncompletos) {
        FacesContext context = FacesContext.getCurrentInstance();

        Integer habitacionId = habitacionIdSeleccionada;
        if ((habitacionId == null || habitacionId == 0)) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Habitación requerida", "Seleccione una habitación para verificar disponibilidad."));
            }
            return false;
        }

        if (reserva.getCheckin() == null || reserva.getCheckout() == null) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Información incompleta", "Debe indicar las fechas de la reserva."));
            }
            return false;
        }

        try {
            Integer reservaId = reserva != null && reserva.getIdReserva() > 0 ? reserva.getIdReserva() : null;
            boolean disponible = reservaDAO.habitacionDisponible(habitacionId,
                    reserva.getCheckin(), reserva.getCheckout(), reservaId);
            if (!disponible) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Fechas no disponibles", "La habitación ya está reservada en el rango seleccionado."));
                return false;
            }
        } catch (SQLException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo verificar la disponibilidad de la habitación."));
            return false;
        }

        return true;
    }

    private Habitacion obtenerHabitacionPorId(Integer id) {
        if (id == null) {
            return null;
        }

        return listaHabitaciones.stream()
                .filter(h -> h.getIdHabitacion() == id)
                .findFirst()
                .orElseGet(() -> {
                    Habitacion habitacion = new Habitacion();
                    habitacion.setIdHabitacion(id);
                    return habitacion;
                });
    }

    private Usuario obtenerUsuarioPorId(Integer id) {
        if (id == null) {
            return null;
        }

        return listaUsuarios.stream()
                .filter(u -> u.getIdUsuario() == id)
                .findFirst()
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(id);
                    return usuario;
                });
    }

    private void limpiarFormulario() {
        reserva = new Reserva();
        habitacionIdSeleccionada = null;
        usuarioIdSeleccionado = null;
        fechasOcupadasJson = "[]";
    }

    public String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(DISPLAY_FORMATTER) : "";
    }

    public String obtenerNombreHabitacion(Reserva reserva) {
        if (reserva == null || reserva.getHabitacion() == null) {
            return "Sin asignar";
        }

        return "Habitación " + reserva.getHabitacion().getNumHabitacion();
    }

    private String toHtmlInputValue(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }

        LocalDateTime truncated = fecha.truncatedTo(ChronoUnit.MINUTES);
        return truncated.format(HTML_INPUT_FORMATTER);
    }

    public String getCheckoutMinValue() {
        return toHtmlInputValue(reserva.getCheckin());
    }

    public EnumEstadoReserva[] getEstados() {
        return EnumEstadoReserva.values();
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public List<Reserva> getListaReservas() {
        return listaReservas;
    }

    public List<Habitacion> getListaHabitaciones() {
        return listaHabitaciones;
    }

    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public Integer getUsuarioIdSeleccionado() {
        return usuarioIdSeleccionado;
    }

    public void setUsuarioIdSeleccionado(Integer usuarioIdSeleccionado) {
        this.usuarioIdSeleccionado = usuarioIdSeleccionado;
    }

    public String getFechasOcupadasJson() {
        return fechasOcupadasJson;
    }

    public void setHabitacionIdSeleccionada(Integer habitacionIdSeleccionada) {
        this.habitacionIdSeleccionada = habitacionIdSeleccionada;
        refrescarOcupacionesHabitacion();
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public void setUsuarioLogueado(Usuario usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
    }

    public List<TipoHabitacion> getListaTipoHabitaciones() {
        return listaTipoHabitaciones;
    }

    public List<Reserva> getListarPorUsuario() {
        return listarPorUsuario;
    }

    public void setListarPorUsuario(List<Reserva> listarPorUsuario) {
        this.listarPorUsuario = listarPorUsuario;
    }

    private void refrescarOcupacionesHabitacion() {
        if (habitacionIdSeleccionada == null) {
            fechasOcupadasJson = "[]";
            return;
        }

        try {
            Integer reservaId = reserva != null && reserva.getIdReserva() > 0 ? reserva.getIdReserva() : null;
            List<Reserva> ocupaciones = reservaDAO.listarOcupacionesHabitacion(habitacionIdSeleccionada, reservaId);

            if (ocupaciones.isEmpty()) {
                fechasOcupadasJson = "[]";
                return;
            }

            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < ocupaciones.size(); i++) {
                Reserva ocupacion = ocupaciones.get(i);

                if (ocupacion.getCheckin() == null || ocupacion.getCheckout() == null) {
                    continue;
                }

                if (jsonBuilder.length() > 1) {
                    jsonBuilder.append(',');
                }

                jsonBuilder.append('{')
                        .append("\"from\":\"")
                        .append(ocupacion.getCheckin().truncatedTo(ChronoUnit.MINUTES).format(HTML_INPUT_FORMATTER))
                        .append("\",\"to\":\"")
                        .append(ocupacion.getCheckout().truncatedTo(ChronoUnit.MINUTES).format(HTML_INPUT_FORMATTER))
                        .append("\"}");
            }

            jsonBuilder.append(']');
            fechasOcupadasJson = jsonBuilder.toString();
        } catch (SQLException e) {
            fechasOcupadasJson = "[]";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo consultar la disponibilidad de la habitación."));
        }
    }

    public Integer getHabitacionIdSeleccionada() {
        return habitacionIdSeleccionada;
    }

    public String getHabitacionesPorTipoJson(int tipoId) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        List<Habitacion> habitacionesFiltradas = new ArrayList<>();

        for (Habitacion habitacion : listaHabitaciones) {
            TipoHabitacion tipo = habitacion.getTipoHabitacion();
            if (tipo != null && tipo.getIdTipoHabitacion() == tipoId) {
                habitacionesFiltradas.add(habitacion);
            }
        }

        for (int i = 0; i < habitacionesFiltradas.size(); i++) {
            Habitacion habitacion = habitacionesFiltradas.get(i);
            TipoHabitacion tipo = habitacion.getTipoHabitacion();
            json.append("{")
                    .append("\"id\":").append(habitacion.getIdHabitacion()).append(",")
                    .append("\"numero\":").append(habitacion.getNumHabitacion()).append(",")
                    .append("\"tipoId\":").append(tipo.getIdTipoHabitacion()).append(",")
                    .append("\"tipoNombre\":\"").append(escaparJson(tipo.getNombre())).append("\"")
                    .append("}");
            if (i < habitacionesFiltradas.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E")
                .replace("&", "\\u0026");
    }

    public void filtrarHabitacionesPorTipo() {
        if (habitacionesFiltradas == null) {
            habitacionesFiltradas = new ArrayList<>();
        } else {
            habitacionesFiltradas.clear();
        }

        if (tipoSeleccionado == null) {
            habitacionesFiltradas.addAll(habitaciones);
            return;
        }

        for (Habitacion h : habitaciones) {
            if (h.getTipoHabitacion() != null
                    && h.getTipoHabitacion().getIdTipoHabitacion() == tipoSeleccionado.getIdTipoHabitacion()) {
                habitacionesFiltradas.add(h);
            }
        }
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public List<Habitacion> getHabitacionesFiltradas() {
        return habitacionesFiltradas;
    }

    public void setHabitacionesFiltradas(List<Habitacion> habitacionesFiltradas) {
        this.habitacionesFiltradas = habitacionesFiltradas;
    }

    public TipoHabitacion getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(TipoHabitacion tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

}
