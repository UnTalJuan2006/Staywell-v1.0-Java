package Controlador;

import DAO.EspacioDAO;
import DAO.EventoDAO;
import DAO.UsuarioDAO;
import Modelo.Espacio;
import Modelo.Evento;
import Modelo.Usuario;
import Modelo.EnumRoles;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import Modelo.EnumEstadoEspacio;
import Modelo.EnumEstadoEvento;
import java.util.Date;
import util.ExcelUtil;
import util.PdfUtil;

@ManagedBean
@ViewScoped
public class EventoBean implements Serializable {

    private final EventoDAO eventoDAO = new EventoDAO();
    private final EspacioDAO espacioDAO = new EspacioDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private List<Espacio> listaEspacios = new ArrayList<>();
    private List<Evento> listaEventos = new ArrayList<>();
    private List<Evento> eventosFiltrados = new ArrayList<>();
    private List<Usuario> listaUsuarios = new ArrayList<>();
    private List<Evento> listarPorUsuario = new ArrayList<>();

    private Evento evento = new Evento();
    private Integer espacioIdSeleccionado;
    private Integer usuarioIdSeleccionado;
    private String fechasOcupadasJson = "[]";

    private Date filtroFechaInicio;
    private Date filtroFechaFin;

    private String filtroBusqueda = "";

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HTML_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private Usuario usuarioLogueado;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("[DEBUG] Iniciando EventoBean.init()");

        try {
            if (context == null) {
                System.out.println("⚠ No hay FacesContext activo. Posible navegación directa.");
                inicializarListasVacias();
                return;
            }

            usuarioLogueado = (Usuario) context.getExternalContext()
                    .getSessionMap().get("usuarioLogueado");

            cargarListasBasicas();
            limpiarFormulario();

            if (usuarioLogueado == null) {
                System.out.println("⚠ No hay usuario logueado en sesión.");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "Debe iniciar sesión para acceder a las reservas."));
                return;
            }

            System.out.println("[DEBUG] Usuario logueado: " + usuarioLogueado.getNombre() 
                    + " | Rol: " + usuarioLogueado.getRol());

            try {
                if (usuarioLogueado.getRol() == EnumRoles.ADMIN) {
                    cargarEventos();
                    System.out.println("[DEBUG] Eventos de ADMIN cargados: " + listaEventos.size());
                } else if (usuarioLogueado.getRol() == EnumRoles.HUESPED) {
                    listarEventosDelUsuario();
                    System.out.println("[DEBUG] Eventos del HUESPED cargados: " + listarPorUsuario.size());
                } else {
                    listaEventos = new ArrayList<>();
                    System.out.println("[DEBUG] Rol desconocido, sin eventos");
                }
            } catch (Exception e) {
                System.out.println("❌ Error al cargar eventos: " + e.getMessage());
                e.printStackTrace();
                listaEventos = new ArrayList<>();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                        "No se pudieron cargar los eventos."));
            }

            System.out.println("[DEBUG] EventoBean inicializado correctamente.");
            System.out.println("[DEBUG] Resumen - Espacios: " + listaEspacios.size() 
                    + ", Usuarios: " + listaUsuarios.size() + ", Eventos: " + listaEventos.size());

        } catch (Exception ex) {
            System.out.println("❌ Error inesperado en init(): " + ex.getMessage());
            ex.printStackTrace();
            inicializarListasVacias();
        }
    }

    private void cargarListasBasicas() {
        System.out.println("[DEBUG] Iniciando carga de listas básicas...");

        // Load spaces
        try {
            List<Espacio> espacios = espacioDAO.listar();
            listaEspacios = espacios != null ? new ArrayList<>(espacios) : new ArrayList<>();
            System.out.println("[DEBUG] Espacios cargados exitosamente: " + listaEspacios.size());
        } catch (SQLException e) {
            System.out.println("❌ Error SQL al cargar espacios: " + e.getMessage());
            e.printStackTrace();
            listaEspacios = new ArrayList<>();
        } catch (Exception e) {
            System.out.println("❌ Error inesperado al cargar espacios: " + e.getMessage());
            e.printStackTrace();
            listaEspacios = new ArrayList<>();
        }

        // Load users
        try {
            List<Usuario> usuarios = usuarioDAO.listar();
            listaUsuarios = usuarios != null ? new ArrayList<>(usuarios) : new ArrayList<>();
            System.out.println("[DEBUG] Usuarios cargados exitosamente: " + listaUsuarios.size());
        } catch (SQLException e) {
            System.out.println("❌ Error SQL al cargar usuarios: " + e.getMessage());
            e.printStackTrace();
            listaUsuarios = new ArrayList<>();
        } catch (Exception e) {
            System.out.println("❌ Error inesperado al cargar usuarios: " + e.getMessage());
            e.printStackTrace();
            listaUsuarios = new ArrayList<>();
        }
    }

    private void inicializarListasVacias() {
        System.out.println("[DEBUG] Inicializando listas vacías");
        listaEventos = new ArrayList<>();
        eventosFiltrados = new ArrayList<>();
        listaEspacios = new ArrayList<>();
        listaUsuarios = new ArrayList<>();
        listarPorUsuario = new ArrayList<>();
    }

    public void cargarEventos() {
        try {
            List<Evento> eventos = eventoDAO.listar();
            listaEventos = eventos != null ? new ArrayList<>(eventos) : new ArrayList<>();
            actualizarEventosFiltrados();
            System.out.println("[DEBUG] Eventos cargados: " + listaEventos.size());
        } catch (SQLException e) {
            System.out.println("❌ Error al cargar eventos: " + e.getMessage());
            listaEventos = new ArrayList<>();
            eventosFiltrados = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudieron cargar los eventos."));
        }
    }

    public void listarEventosDelUsuario() {
        try {
            if (usuarioLogueado != null) {
                List<Evento> eventosUsuario = eventoDAO.listarPorUsuario(usuarioLogueado.getIdUsuario());
                listarPorUsuario = eventosUsuario != null ? new ArrayList<>(eventosUsuario) : new ArrayList<>();

                // Sincronizamos las listas base y filtrada para que el filtro funcione
                // correctamente para usuarios huéspedes.
                listaEventos = new ArrayList<>(listarPorUsuario);
                actualizarEventosFiltrados();
                System.out.println("[DEBUG] Eventos del usuario " + usuarioLogueado.getIdUsuario()
                        + " cargados: " + listarPorUsuario.size());
            } else {
                listarPorUsuario = new ArrayList<>();
                System.out.println("⚠ No hay usuario logueado para listar eventos");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al cargar eventos del usuario: " + e.getMessage());
            e.printStackTrace();
            listarPorUsuario = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "No se pudieron cargar las reservas del usuario."));
        }
    }

    public String guardarComoCliente() {
        try {
            if (!validarFechaBasica(true)) {
                return null;
            }
            evento.setUsuario(usuarioLogueado);

            Espacio espacio = obtenerEspacioPorId(espacioIdSeleccionado);
            if (espacio == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                        "Debe seleccionar un espacio."));
                return null;
            }
            evento.setEspacio(espacio);

            if (evento.getNombreCliente() == null || evento.getNombreCliente().isEmpty()) {
                evento.setNombreCliente(usuarioLogueado.getNombre());
            }

            prepararAuditoria(true);
            evento.setEstado(EnumEstadoEvento.Activa);
            eventoDAO.eventoHuesped(evento);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                    "Evento registrado correctamente."));
            limpiarFormulario();
            return "HomeHuesped?faces-redirect=true";

        } catch (SQLException e) {
            System.out.println("❌ Error al guardar evento: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "No se pudo registrar el evento."));
            return null;
        }
    }

    public void cargarEventoPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Evento eventoEncontrado = eventoDAO.buscar(id);
                if (eventoEncontrado != null) {
                    this.evento = eventoEncontrado;
                    espacioIdSeleccionado = (evento.getEspacio() != null)
                            ? evento.getEspacio().getIdEspacio() : null;
                    usuarioIdSeleccionado = (evento.getUsuario() != null)
                            ? evento.getUsuario().getIdUsuario() : null;
                    refrescarOcupacionesEspacio();
                    System.out.println("[DEBUG] Evento cargado: " + id);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                            "El evento no existe."));
                }
            } catch (NumberFormatException | SQLException e) {
                System.out.println("❌ Error al cargar evento por ID: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                        "No se pudo cargar el evento."));
            }
        }
    }

    public void aplicarFiltroFechas() {
        LocalDate fechaInicioLocal = convertirFecha(filtroFechaInicio);
        LocalDate fechaFinLocal = convertirFecha(filtroFechaFin);

        if (fechaInicioLocal != null && fechaFinLocal != null && fechaFinLocal.isBefore(fechaInicioLocal)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Rango inválido",
                    "La fecha final debe ser posterior a la inicial."));
            filtroFechaFin = null;
            actualizarEventosFiltrados();
            return;
        }

        actualizarEventosFiltrados();
    }

    public void limpiarFiltrosFechas() {
        filtroFechaInicio = null;
        filtroFechaFin = null;
        actualizarEventosFiltrados();
    }

    private LocalDate convertirFecha(Date fecha) {
        if (fecha == null) {
            return null;
        }

        // Evitamos desfases por zona horaria cuando el valor proviene de java.sql.Date
        // usando su conversión directa a LocalDate. Para otros tipos, conservamos la
        // conversión habitual a través de Instant con la zona del sistema.
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }

        return fecha.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    private void actualizarEventosFiltrados() {
        List<Evento> fuente = listaEventos != null ? listaEventos : new ArrayList<>();
        LocalDate inicio = convertirFecha(filtroFechaInicio);
        LocalDate fin = convertirFecha(filtroFechaFin);
        String terminoNormalizado = normalizarTexto(filtroBusqueda);

        if (inicio == null && fin == null && terminoNormalizado.isEmpty()) {
            eventosFiltrados = new ArrayList<>(fuente);
            return;
        }

        List<Evento> filtrados = new ArrayList<>();

        for (Evento eventoActual : fuente) {
            if (eventoActual == null || eventoActual.getFechaEvento() == null) {
                continue;
            }

            LocalDate fechaEventoLocal = convertirFecha(eventoActual.getFechaEvento());

            boolean coincide = true;

            if (inicio != null) {
                coincide = coincide && (fechaEventoLocal != null && !fechaEventoLocal.isBefore(inicio));
            }

            if (fin != null) {
                coincide = coincide && (fechaEventoLocal != null && !fechaEventoLocal.isAfter(fin));
            }

            if (coincide) {
                if (!terminoNormalizado.isEmpty()) {
                    String nombreEvento = normalizarTexto(eventoActual.getNombreEvento());
                    String nombreUsuario = normalizarTexto(
                            eventoActual.getUsuario() != null ? eventoActual.getUsuario().getNombre() : null);

                    coincide = nombreEvento.contains(terminoNormalizado)
                            || nombreUsuario.contains(terminoNormalizado);
                }

                if (coincide) {
                    filtrados.add(eventoActual);
                }
            }
        }

        eventosFiltrados = filtrados;
    }

    private String normalizarTexto(String texto) {
        return texto != null ? texto.toLowerCase().trim() : "";
    }

    public void aplicarFiltroBusqueda() {
        actualizarEventosFiltrados();
    }

    public String guardar() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (usuarioLogueado == null || usuarioLogueado.getRol() != EnumRoles.ADMIN) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Acceso denegado", "Solo los administradores pueden crear eventos."));
            return null;
        }

        try {
            if (!validarFechaBasica(true)) {
                return null;
            }

            if (!asignarRelaciones()) {
                return null;
            }

            if (!validarDisponibilidadFechas(true)) {
                return null;
            }

            if (evento.getEstado() == null) {
                evento.setEstado(EnumEstadoEvento.Activa);
            }

            prepararAuditoria(true);

            int idGenerado = eventoDAO.agregarEvento(evento);
            if (idGenerado <= 0) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "No se pudo registrar el evento."));
                return null;
            }

            evento.setIdEvento(idGenerado);
            context.getExternalContext().getFlash().setKeepMessages(true);
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Evento registrado correctamente."));
            limpiarFormulario();
            cargarEventos();
            return "/Eventos.xhtml?faces-redirect=true";

        } catch (SQLException e) {
            System.out.println("❌ Error al guardar evento: " + e.getMessage());
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo registrar el evento."));
            return null;
        }
    }

    public String actualizar() {
        try {
            if (!validarFechaBasica(true)) {
                return null;
            }

            if (!asignarRelaciones()) {
                return null;
            }

            if (!validarDisponibilidadFechas(true)) {
                return null;
            }

            if (evento.getEstado() == null) {
                evento.setEstado(EnumEstadoEvento.Activa);
            }

            prepararAuditoria(false);

            eventoDAO.actualizar(evento);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Evento actualizado correctamente."));
            limpiarFormulario();
            cargarEventos();
            return "Eventos?faces-redirect=true";
            
        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar evento: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "No se pudo actualizar el evento."));
            return null;
        }
    }

    private boolean asignarRelaciones() {
        Espacio espacio = obtenerEspacioPorId(espacioIdSeleccionado);
        Usuario usuario = obtenerUsuarioPorId(usuarioIdSeleccionado);

        if (espacio == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                    "Debe seleccionar un espacio."));
            return false;
        }

        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                    "Debe seleccionar un huésped."));
            return false;
        }

        evento.setEspacio(espacio);
        evento.setUsuario(usuario);

        if (evento.getNombreCliente() == null || evento.getNombreCliente().isEmpty()) {
            evento.setNombreCliente(usuario.getNombre());
        }

        return true;
    }

    public void validarFechasAjax() {
        refrescarOcupacionesEspacio();

        if (evento.getFechaEvento() == null || espacioIdSeleccionado == null) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();

        if (!validarFechaBasica(false)) {
            context.validationFailed();
            return;
        }

        if (!validarDisponibilidadFechas(false)) {
            context.validationFailed();
            return;
        }
    }

    private boolean validarFechaBasica(boolean mostrarMensajeCamposIncompletos) {
        FacesContext context = FacesContext.getCurrentInstance();

        if (evento.getFechaEvento() == null) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Información incompleta",
                        "Debe indicar la fecha del evento"
                ));
            }
            return false;
        }

        LocalDate fechaEventoLocal = evento.getFechaEvento().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate hoy = LocalDate.now();

        if (fechaEventoLocal.isBefore(hoy)) {
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Fecha inválida",
                    "La fecha del evento no puede ser anterior a la fecha actual"
            ));
            return false;
        }

        return true;
    }

    public void eliminar(Evento eventoSeleccionado) {
        if (eventoSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                            "No se pudo identificar el evento a eliminar."));
            return;
        }

        try {
            eventoDAO.eliminar(eventoSeleccionado.getIdEvento());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Evento eliminado correctamente."));
            cargarEventos();
        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo eliminar el evento."));
        }
    }

    private boolean validarDisponibilidadFechas(boolean mostrarMensajeCamposIncompletos) {
        FacesContext context = FacesContext.getCurrentInstance();

        Integer espacioId = espacioIdSeleccionado;

        if (espacioId == null || espacioId == 0) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Espacio requerido", "Seleccione un espacio para verificar disponibilidad."));
            }
            return false;
        }

        if (evento.getFechaEvento() == null) {
            if (mostrarMensajeCamposIncompletos) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Información incompleta", "Debe indicar la fecha del evento."));
            }
            return false;
        }

        try {
            Integer eventoId = evento != null && evento.getIdEvento() > 0 ? evento.getIdEvento() : null;
            boolean disponible = eventoDAO.espacioDisponible(espacioId, evento.getFechaEvento(), eventoId);

            if (!disponible) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Fechas no disponibles", "El espacio ya está reservado en el rango seleccionado."));
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al verificar disponibilidad: " + e.getMessage());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo verificar la disponibilidad del espacio."));
            return false;
        }

        return true;
    }

    private Espacio obtenerEspacioPorId(Integer id) {
        if (id == null) {
            return null;
        }
        return listaEspacios.stream()
                .filter(e -> e.getIdEspacio() == id)
                .findFirst()
                .orElseGet(() -> {
                    Espacio espacio = new Espacio();
                    espacio.setIdEspacio(id);
                    return espacio;
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
        evento = new Evento();
        evento.setEstado(EnumEstadoEvento.Activa);
        espacioIdSeleccionado = null;
        usuarioIdSeleccionado = null;
        fechasOcupadasJson = "[]";
    }

    private void prepararAuditoria(boolean esNuevo) {
        LocalDateTime ahora = LocalDateTime.now();
        evento.setFechaActualizacion(ahora);
        if (esNuevo || evento.getFechaCreacion() == null) {
            evento.setFechaCreacion(ahora);
        }
    }

    public String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(DISPLAY_FORMATTER) : "";
    }

    public String obtenerNombreEspacio(Evento evento) {
        if (evento == null || evento.getEspacio() == null) {
            return "Sin asignar";
        }
        return evento.getEspacio().getNombre();
    }

    public EnumEstadoEvento[] getEstados() {
        return EnumEstadoEvento.values();
    }

    private void refrescarOcupacionesEspacio() {
        if (espacioIdSeleccionado == null) {
            fechasOcupadasJson = "[]";
            return;
        }

        try {
            Integer eventoId = (evento != null && evento.getIdEvento() > 0)
                    ? evento.getIdEvento()
                    : null;

            List<Evento> ocupaciones = eventoDAO.listarOcupacionesEspacio(espacioIdSeleccionado, eventoId);

            if (ocupaciones == null || ocupaciones.isEmpty()) {
                fechasOcupadasJson = "[]";
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < ocupaciones.size(); i++) {
                Evento ocu = ocupaciones.get(i);
                json.append("{\"start\":\"").append(ocu.getFechaEvento()).append("\"}");
                if (i < ocupaciones.size() - 1) json.append(",");
            }
            json.append("]");

            fechasOcupadasJson = json.toString();
            System.out.println("[DEBUG] Ocupaciones refrescadas: " + fechasOcupadasJson);

        } catch (SQLException e) {
            System.out.println("❌ Error al refrescar ocupaciones: " + e.getMessage());
            fechasOcupadasJson = "[]";
        }
    }

    // Getters and Setters
    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public List<Evento> getListaEventos() {
        return listaEventos;
    }

    public List<Evento> getEventosFiltrados() {
        return eventosFiltrados != null ? eventosFiltrados : new ArrayList<>();
    }

    public List<Espacio> getListaEspacios() {
        return listaEspacios;
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

    public void setEspacioIdSeleccionado(Integer espacioIdSeleccionado) {
        this.espacioIdSeleccionado = espacioIdSeleccionado;
        refrescarOcupacionesEspacio();
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public Date getFiltroFechaInicio() {
        return filtroFechaInicio;
    }

    public void setFiltroFechaInicio(Date filtroFechaInicio) {
        this.filtroFechaInicio = filtroFechaInicio;
    }

    public Date getFiltroFechaFin() {
        return filtroFechaFin;
    }

    public void setFiltroFechaFin(Date filtroFechaFin) {
        this.filtroFechaFin = filtroFechaFin;
    }

    public String getFiltroBusqueda() {
        return filtroBusqueda;
    }

    public void setFiltroBusqueda(String filtroBusqueda) {
        this.filtroBusqueda = filtroBusqueda;
    }

    public void setUsuarioLogueado(Usuario usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
    }

    public List<Evento> getListarPorUsuario() {
        return listarPorUsuario;
    }

    public void setListarPorUsuario(List<Evento> listarPorUsuario) {
        this.listarPorUsuario = listarPorUsuario;
    }

    public void exportarExcelEventos() {
        try {
            List<Evento> lista = eventoDAO.listar();
            if (lista == null) {
                lista = new ArrayList<>();
            } else {
                lista = new ArrayList<>(lista);
            }

            String[] headers = {
                "ID", "Nombre", "Fecha", "Hora Inicio", "Hora Fin",
                "Espacio", "Cliente", "Estado", "Creado", "Actualizado"
            };

            List<Object[]> datos = lista.stream()
                    .map(e -> new Object[]{
                e.getIdEvento(),
                e.getNombreEvento(),
                e.getFechaEvento(),
                e.getHoraInicio(),
                e.getHoraFin(),
                e.getEspacio() != null ? e.getEspacio().getNombre() : "",
                e.getUsuario() != null ? e.getUsuario().getNombre() : e.getNombreCliente(),
                e.getEstado() != null ? e.getEstado().name() : "",
                e.getFechaCreacion(),
                e.getFechaActualizacion()
            })
                    .collect(java.util.stream.Collectors.toList());

            ExcelUtil.generarExcel("eventos", "Eventos", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportarPdfEventos() {
        try {
            List<Evento> lista = eventoDAO.listar();
            if (lista == null) {
                lista = new ArrayList<>();
            } else {
                lista = new ArrayList<>(lista);
            }

            String[] headers = {
                "ID", "Nombre", "Fecha", "Hora Inicio", "Hora Fin",
                "Espacio", "Cliente", "Estado", "Creado", "Actualizado"
            };

            List<Object[]> datos = lista.stream()
                    .map(ev -> new Object[]{
                ev.getIdEvento(),
                ev.getNombreEvento(),
                ev.getFechaEvento(),
                ev.getHoraInicio(),
                ev.getHoraFin(),
                ev.getEspacio() != null ? ev.getEspacio().getNombre() : "",
                ev.getUsuario() != null ? ev.getUsuario().getNombre() : ev.getNombreCliente(),
                ev.getEstado() != null ? ev.getEstado().name() : "",
                ev.getFechaCreacion(),
                ev.getFechaActualizacion()
            })
                    .collect(java.util.stream.Collectors.toList());

            PdfUtil.generarPdf("Eventos", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getEspacioIdSeleccionado() {
        return espacioIdSeleccionado;
    }
}