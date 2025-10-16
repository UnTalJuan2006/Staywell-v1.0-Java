package Controlador;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import Modelo.Usuario;

@ManagedBean
@ViewScoped
public class CorreoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String asunto;
    private String contmensaje;
    private String plantillaSeleccionada;
    private List<String> dest = new ArrayList<>();
    private List<Usuario> listaUsr = new ArrayList<>();

    @PostConstruct
    public void init() {
        listarUsuarios();
    }

    public void listarUsuarios() {
        listaUsr = new ArrayList<>();
        try {
            String sql = "SELECT nombre, email FROM usuario";
            PreparedStatement ps = Conexion.conectar().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario usr = new Usuario();
                usr.setNombre(rs.getString("nombre"));
                usr.setEmail(rs.getString("email"));
                listaUsr.add(usr);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void enviarCorreo() {
        final String user = "staywellgreat@gmail.com";
        final String pass = "lfda jfyq cgcg zhop"; // contraseña de aplicación

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session sesion = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(user));

            if (dest == null || dest.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Debe seleccionar al menos un destinatario"));
                return;
            }

            InternetAddress[] dests = new InternetAddress[dest.size()];
            int i = 0;
            for (String d : dest) {
                dests[i++] = new InternetAddress(d);
            }

            mensaje.setRecipients(Message.RecipientType.TO, dests);
            mensaje.setSubject(asunto);
            mensaje.setText(contmensaje);

            Transport.send(mensaje);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Correos enviados exitosamente"));

        } catch (MessagingException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error enviando correos: " + e.getMessage()));
        }
    }

    // método auxiliar para botones rápidos
    public void seleccionarPlantilla(String tipo) {
        this.plantillaSeleccionada = tipo;
        aplicarPlantilla();
    }

    public void aplicarPlantilla() {
        if (plantillaSeleccionada == null || plantillaSeleccionada.isEmpty()) {
            return;
        }
        switch (plantillaSeleccionada) {
            case "bienvenida":
                asunto = "¡Bienvenido a Great Viggio!";
                contmensaje = "Estimado/a [Nombre del huésped],\n\nEn nombre de todo el equipo...";
                break;
            case "reserva":
                asunto = "Confirmación de su reserva en Great Viggio Hotel 🏨✨";
                contmensaje = "Estimado/a [Nombre del huésped],\n\nDetalles de su reserva: ...";
                break;
            case "evento":
                asunto = "Confirmación de su evento";
                contmensaje = "Estimado/a [Nombre del participante],\n\nConfirmamos su inscripción ...";
                break;
            default:
                asunto = "";
                contmensaje = "";
                break;
        }
    }

    // Getters y setters
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getContmensaje() { return contmensaje; }
    public void setContmensaje(String contmensaje) { this.contmensaje = contmensaje; }

    public String getPlantillaSeleccionada() { return plantillaSeleccionada; }
    public void setPlantillaSeleccionada(String plantillaSeleccionada) { this.plantillaSeleccionada = plantillaSeleccionada; }

    public List<String> getDest() { return dest; }
    public void setDest(List<String> dest) { this.dest = dest; }

    public List<Usuario> getListaUsr() { return listaUsr; }
    public void setListaUsr(List<Usuario> listaUsr) { this.listaUsr = listaUsr; }
}
