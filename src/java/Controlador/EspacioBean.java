package Controlador;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;

import Modelo.Espacio;
import Modelo.EnumEstadoEspacio;
import Modelo.EnumTipoEspacios;
import DAO.EspacioDAO;

@ManagedBean
@ViewScoped
public class EspacioBean implements Serializable {

    private Espacio espacio;
    private EspacioDAO espacioDAO;
    private List<Espacio> listaEspacios;
    private Part imagen; // para subir foto temporal

    @PostConstruct
    public void init() {
        espacio = new Espacio();
        espacioDAO = new EspacioDAO();
        cargarListaEspacios();
    }

    // Método para cargar la lista de espacios
    public void cargarListaEspacios() {
        try {
            listaEspacios = espacioDAO.listar();
        } catch (SQLException e) {
            System.out.println("Error al listar espacios: " + e.getMessage());
        }
    }

    // ======= Getters y Setters =======
    public Espacio getEspacio() {
        return espacio;
    }

    public void setEspacio(Espacio espacio) {
        this.espacio = espacio;
    }

    public Part getImagen() {
        return imagen;
    }

    public void setImagen(Part imagen) {
        this.imagen = imagen;
    }

    public List<Espacio> getListaEspacios() {
        return listaEspacios;
    }

    public EnumEstadoEspacio[] getEstado() {
        return EnumEstadoEspacio.values();
    }

    public EnumTipoEspacios[] getTipos() {
        return EnumTipoEspacios.values();
    }

    // ======= Agregar espacio =======
    public String agregar() throws IOException {
        try {
            espacio.setFechaActualizacion(LocalDateTime.now());

            // Guardar imagen en carpeta /img del proyecto (dentro de webapp)
            if (imagen != null) {
                // Obtener el contexto del servidor
                ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                        .getExternalContext().getContext();

                // Ruta física a la carpeta img/
                String rutaCarpeta = sc.getRealPath("/img/");

                // Crear la carpeta si no existe
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {
                    carpeta.mkdirs();
                }

                // Crear un nombre único para la imagen
                String nombreArchivo = espacio.getNombre().replaceAll("\\s+", "_")
                        + "_" + System.currentTimeMillis() + ".png";

                // Guardar la imagen físicamente
                File archivoDestino = new File(carpeta, nombreArchivo);
                try (InputStream in = imagen.getInputStream(); FileOutputStream out = new FileOutputStream(archivoDestino)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                // Guardar solo el nombre del archivo (no la ruta completa) en la base de datos
                espacio.setImagen(nombreArchivo);
            }

            // Guardar el espacio en la base de datos
            espacioDAO.agregar(espacio);

            // Refrescar la lista de espacios
            cargarListaEspacios();

            // Limpiar campos
            espacio = new Espacio();
            imagen = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Espacio registrado correctamente."));

        } catch (SQLException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudo registrar el espacio: " + e.getMessage()));
            return null;
        }

        return "Espacios?faces-redirect=true";
    }

    public void cargarEspacioPorId() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Espacio espacioEncontrado = espacioDAO.buscar(id);

                if (espacioEncontrado != null) {
                    this.espacio = espacioEncontrado;
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Espacio cargado", "Se cargó el espacio con ID: " + id));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "No encontrado", "El espacio solicitado no existe."));
                }
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Identificador de espacio inválido."));
            }
        }
    }

    public String actualizar() {
        try {
            espacio.setFechaActualizacion(LocalDateTime.now());

            if (imagen != null) {
                String nombreArchivo = guardarImagen(imagen, espacio.getNombre());
                espacio.setImagen(nombreArchivo);
            }

            espacioDAO.actualizar(espacio);
            cargarListaEspacios();
            espacio = new Espacio();
            imagen = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Espacio actualizado correctamente."));
            return "Espacios?faces-redirect=true";

        } catch (SQLException | IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el espacio."));
            return null;
        }
    }

    public String eliminar(Espacio p) {
        try{
            EspacioDAO espacioDAO = new EspacioDAO();
            espacioDAO.eliminar(p);
            cargarListaEspacios();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Espacio eliminado correctamente", null));
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al eliminar Espacio: " + e.getMessage(), null));
        }

        return "Espacios?faces-redirect=true";
    }

    public void refrescarLista() {
        cargarListaEspacios();
    }

    public EnumEstadoEspacio[] getEstados() {
        return EnumEstadoEspacio.values();
    }

    private String guardarImagen(Part archivo, String nombreBase) throws IOException {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        String rutaCarpeta = sc.getRealPath("/img/");
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        String nombreArchivo = nombreBase.replaceAll("\\s+", "_")
                + "_" + System.currentTimeMillis() + ".png";

        File archivoDestino = new File(carpeta, nombreArchivo);
        try (InputStream in = archivo.getInputStream(); FileOutputStream out = new FileOutputStream(archivoDestino)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        return nombreArchivo;
    }

}
