package Controlador;

import DAO.TipoHabitacionDAO;
import Modelo.TipoHabitacion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import util.ExcelUtil;
import util.PdfUtil;

@ManagedBean
@ViewScoped
public class TipoHabitacionBean {

    private TipoHabitacion tipoHabitacion = new TipoHabitacion();
    private TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
    private Part imagen;
    private List<TipoHabitacion> listaTipoHabitaciones;
    private String filtroNombre;

    public String getFiltroNombre() {
        return filtroNombre;
    }

    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }

    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
    }
    private List<TipoHabitacion> listaOriginal; // NUEVO: lista completa

    // --- FILTRO CON SELECT ---
    private String filtroTipo; // ← este lo usará el <p:selectOneMenu>

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    @PostConstruct
    public void init() {
        try {
            listaOriginal = tipoHabitacionDAO.listar();       // carga completa
            listaTipoHabitaciones = new ArrayList<>(listaOriginal); // copia para mostrar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<TipoHabitacion> getListaTipoHabitaciones() {
        return listaTipoHabitaciones;
    }

    public void aplicarFiltro() {
        if (filtroTipo == null || filtroTipo.trim().isEmpty()) {
            listaTipoHabitaciones = new ArrayList<>(listaOriginal);
        } else {
            listaTipoHabitaciones = listaOriginal.stream()
                    .filter(t -> t.getNombre() != null
                    && t.getNombre().equalsIgnoreCase(filtroTipo))
                    .collect(Collectors.toList());
        }
    }

    // === SELECT ITEMS ===
    public List<TipoHabitacion> getListaTipos() {
        return listaOriginal; // usa todos los tipos para llenar el select
    }

    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public Part getImagen() {
        return imagen;
    }

    public void setImagen(Part imagen) {
        this.imagen = imagen;
    }

    public void setListaTipoHabitaciones(List<TipoHabitacion> listaTipoHabitaciones) {
        this.listaTipoHabitaciones = listaTipoHabitaciones;
    }

//    public List<TipoHabitacion> getListaTipoHabitaciones() {
//        try {
//            return tipoHabitacionDAO.listar();
//        } catch (SQLException e) {
//            System.out.println("Erro al listar tipos");
//            return null;
//        }
    public String agregar() throws IOException {
        try {
            // Verificamos si se subió una imagen
            if (imagen != null) {
                ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                        .getExternalContext().getContext();

                // Carpeta donde se guardarán las imágenes dentro del proyecto
                String rutaCarpeta = sc.getRealPath("/img/");
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {
                    carpeta.mkdirs();
                }

                // Nombre único para el archivo
                String nombreArchivo = tipoHabitacion.getNombre().replaceAll("\\s+", "_")
                        + "_" + System.currentTimeMillis() + ".png";

                // Guardar físicamente la imagen
                File archivoDestino = new File(carpeta, nombreArchivo);
                try (InputStream in = imagen.getInputStream(); FileOutputStream out = new FileOutputStream(archivoDestino)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                // Guardamos el nombre del archivo en el modelo
                tipoHabitacion.setImagen(nombreArchivo);
            }

            // Llamamos al DAO para insertar el registro
            tipoHabitacionDAO.agregar(tipoHabitacion);

            // Mensaje de éxito en la interfaz
            FacesContext.getCurrentInstance().addMessage(null,
                    new javax.faces.application.FacesMessage("Tipo de habitación agregado correctamente."));

            // Limpiamos el formulario
            tipoHabitacion = new TipoHabitacion();
            imagen = null;

            // Redirigir a la lista (opcional)
            return "TipoHabitacion.xhtml?faces-redirect=true";

        } catch (SQLException e) {
            System.out.println("Error al registrar tipo: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new javax.faces.application.FacesMessage(
                            javax.faces.application.FacesMessage.SEVERITY_ERROR,
                            "Error al registrar tipo de habitación",
                            e.getMessage()
                    ));
            return null;
        }
    }

    public void cargarTipoPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                TipoHabitacion tipoHabitacionEncontrada = tipoHabitacionDAO.buscarPorId(id);

                if (tipoHabitacionEncontrada != null) {
                    this.tipoHabitacion = tipoHabitacionEncontrada;

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "TipoHabitación cargada correctamente",
                                    "Se cargó el tipo  con ID: " + id));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Advertencia",
                                    "El Tipohabitación no existe."));
                }
            } catch (NumberFormatException | SQLException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "No se pudo cargar el tipohabitación."));
                e.printStackTrace();
            }
        }
    }

    public String eliminar(TipoHabitacion t) {
        try {

            TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
            tipoHabitacionDAO.eliminar(t);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Tipo eliminado correctamente", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar habitación: " + e.getMessage(), null));
        }
        return "TipoHabitacion?faces-redirect=true";
    }

    public String actualizar() {
        try {
            // Verificamos si el usuario subió una nueva imagen
            if (imagen != null && imagen.getSize() > 0) {
                ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                        .getExternalContext().getContext();

                String rutaCarpeta = sc.getRealPath("/img/");
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {
                    carpeta.mkdirs();
                }

                // Crear un nombre único para la nueva imagen
                String nombreArchivo = tipoHabitacion.getNombre().replaceAll("\\s+", "_")
                        + "_" + System.currentTimeMillis() + ".png";

                // Guardar físicamente la nueva imagen
                File archivoDestino = new File(carpeta, nombreArchivo);
                try (InputStream in = imagen.getInputStream(); FileOutputStream out = new FileOutputStream(archivoDestino)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                // Actualizamos el nombre de la imagen en el objeto
                tipoHabitacion.setImagen(nombreArchivo);
            }

            // Llamamos al DAO para actualizar los datos (nombre, descripción, etc.)
            tipoHabitacionDAO.actualizar(tipoHabitacion);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Tipo de habitación actualizado correctamente", null));

            return "TipoHabitacion.xhtml?faces-redirect=true";

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al actualizar tipo de habitación: " + e.getMessage(), null));
            return null;
        }
    }

    public int getTotalTipos() {
        try {
            return tipoHabitacionDAO.totalTipos();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getCapacidadMaxima() {
        try {
            return tipoHabitacionDAO.capacidadMaxima();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public float getPrecioPromedio() {
        try {
            return tipoHabitacionDAO.precioPromedio();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0f;
        }
    }

    public void exportarExcelTipoHabitaciones() {
        try {
            List<TipoHabitacion> lista = tipoHabitacionDAO.listar();
            String[] headers = {
                "ID", "Nombre", "Capacidad", "Precio"
            };

            List<Object[]> datos = lista.stream()
                    .map(t -> new Object[]{
                t.getIdTipoHabitacion(),
                t.getNombre(),
                t.getCapacidad(),
                t.getPrecio()
            })
                    .collect(java.util.stream.Collectors.toList());

            ExcelUtil.generarExcel("Catalogohabitaciones", "TipoHabitaciones", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportarPdfHabitaciones() {
        try {
            List<TipoHabitacion> lista = tipoHabitacionDAO.listar();
            String[] headers = {
                "ID", "Nombre", "Capacidad", "Precio"
            };

            List<Object[]> datos = lista.stream()
                    .map(t -> new Object[]{
                 t.getIdTipoHabitacion(),
                t.getNombre(),
                t.getCapacidad(),
                t.getPrecio()
            })
                    .collect(java.util.stream.Collectors.toList()); 

               PdfUtil.generarPdf("habitaciones", headers, datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
