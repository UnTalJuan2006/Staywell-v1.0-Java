package Controlador;

import DAO.TipoHabitacionDAO;
import Modelo.TipoHabitacion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;

@ManagedBean
@ViewScoped
public class TipoHabitacionBean {

    private TipoHabitacion tipoHabitacion = new TipoHabitacion();
    private TipoHabitacionDAO tipoHabitacionDAO = new TipoHabitacionDAO();
    private Part imagen;
    private List<TipoHabitacion> listaTipoHabitaciones;


    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
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
       // Getter
  

    // Setter
    public void setListaTipoHabitaciones(List<TipoHabitacion> listaTipoHabitaciones) {
        this.listaTipoHabitaciones = listaTipoHabitaciones;
    }
    
    @PostConstruct
    public void init(){
        tipoHabitacion = new TipoHabitacion();
        tipoHabitacionDAO = new TipoHabitacionDAO();
        getListaTipoHabitaciones();
    }


    public List<TipoHabitacion> getListaTipoHabitaciones() {
        try {
            return tipoHabitacionDAO.listar();
        } catch (SQLException e) {
            System.out.println("Erro al listar tipos");
            return null;
        }
    }

   

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
            try (InputStream in = imagen.getInputStream();
                 FileOutputStream out = new FileOutputStream(archivoDestino)) {
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


}
