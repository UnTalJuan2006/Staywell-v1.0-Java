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

    // ========================== Campos para envíos desde la UI ==========================
    private String asunto;
    private String contmensaje;
    private String plantillaSeleccionada;
    private List<String> dest = new ArrayList<>();
    private List<Usuario> listaUsr = new ArrayList<>();

    @PostConstruct
    public void init() {
        listarUsuarios();
    }

    // ========================== Listar usuarios para selección ==========================
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

    // ========================== Envío de correo desde UI ==========================
    public void enviarCorreo() {
        final String user = "juanmanuelrojasj@gmail.com";  // Cambiar a tu correo
        final String pass = "lwtu nzug ctar yyke";        // Contraseña de aplicación

        if (dest == null || dest.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Debe seleccionar al menos un destinatario"));
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true"); // Para debug

        Session sesion = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(user));

            InternetAddress[] dests = new InternetAddress[dest.size()];
            int i = 0;
            for (String d : dest) {
                dests[i++] = new InternetAddress(d);
            }
            mensaje.setRecipients(Message.RecipientType.TO, dests);
            mensaje.setSubject(asunto);
            mensaje.setContent(contmensaje, "text/html; charset=utf-8");

            Transport.send(mensaje);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Correos enviados exitosamente"));

        } catch (MessagingException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error enviando correos: " + e.getMessage()));
        }
    }

    // ========================== Métodos estáticos para envíos automáticos ==========================
    // 🔹 Enviar correo de bienvenida al registrarse
    public static void enviarCorreoBienvenida(String email, String nombre) {
        new Thread(() -> {
            final String user = "juanmanuelrojasj@gmail.com";
            final String pass = "lwtu nzug ctar yyke";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.debug", "true");

            Session sesion = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            try {
                Message mensaje = new MimeMessage(sesion);
                mensaje.setFrom(new InternetAddress(user));
                mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                mensaje.setSubject("¡Bienvenido a Great Viggio Hotel!");

                String contenidoHTML = "<!DOCTYPE html>"
                        + "<html lang='es'>"
                        + "<head>"
                        + "<meta charset='UTF-8'>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>"
                        + "  body {margin:0;padding:0;font-family:'Arial', sans-serif;background-color:#f5f5f5;color:#131318;}"
                        + "  .container {max-width:600px;margin:30px auto;padding:0;background:rgba(255,255,255,0.9);"
                        + "    border-radius:20px;box-shadow:0 10px 25px rgba(0,0,0,0.2);overflow:hidden;}"
                        + "  .header {background:linear-gradient(90deg,#1a7c8f,#145f6d);color:#fbf6f5;padding:40px 20px;text-align:center;}"
                        + "  .header h1 {margin:0;font-family:'Times New Roman', serif;font-size:28px;letter-spacing:2px;}"
                        + "  .body {padding:40px 30px;text-align:left;}"
                        + "  .body h2 {color:#1a7c8f;margin-top:0;font-size:22px;}"
                        + "  .body p {line-height:1.6;color:#333333;font-size:16px;}"
                        + "  .btn {display:inline-block;margin-top:25px;padding:14px 30px;background-color:#1a7c8f;color:#fbf6f5;"
                        + "    text-decoration:none;border-radius:10px;font-weight:bold;transition:all 0.3s ease;}"
                        + "  .btn:hover {background:linear-gradient(45deg,#1a7c8f,#145f6d);transform:scale(1.05);}"
                        + "  .footer {background-color:#f0f0f0;color:#555555;text-align:center;padding:20px;font-size:12px;}"
                        + "  @media screen and (max-width: 640px) {"
                        + "    .container {margin:20px 10px;padding:0;}"
                        + "    .header {padding:30px 15px;}"
                        + "    .body {padding:30px 15px;}"
                        + "    .header h1 {font-size:24px;}"
                        + "    .body h2 {font-size:20px;}"
                        + "    .body p {font-size:15px;}"
                        + "    .btn {padding:12px 25px;}"
                        + "  }"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div class='container'>"
                        + "  <div class='header'>"
                        + "    <h1>Great Viggio Hotel</h1>"
                        + "  </div>"
                        + "  <div class='body'>"
                        + "    <h2>¡Bienvenido, " + nombre + "!</h2>"
                        + "    <p>Nos complace darte la bienvenida a nuestra exclusiva familia de viajeros. Gracias por registrarte en nuestro sistema y confiar en Great Viggio Hotel para tus próximas experiencias</p>"
                        + "    <p>Desde este momento podrás realizar reservas de habitaciones, espacios para eventos y disfrutar de todos nuestros servicios premium desde la comodidad de nuestra plataforma.</p>"
                        + "    <a href='https://www.greatviggio.com/login' class='btn'>Ir al login</a>"
                        + "  </div>"
                        + "  <div class='footer'>"
                        + "    &copy; 2025 Great Viggio Hotel. Todos los derechos reservados.<br>"
                        + "    Síguenos en nuestras redes para ofertas exclusivas."
                        + "  </div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

                mensaje.setContent(contenidoHTML, "text/html; charset=utf-8");

                Transport.send(mensaje);
                System.out.println("✅ Correo de bienvenida enviado a: " + email);

            } catch (MessagingException e) {
                System.err.println("❌ Error enviando correo de bienvenida a " + email + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // 🔹 Enviar correo de confirmación de reserva
    public static void enviarCorreoReserva(String email, String nombre, String numeroReserva) {
        new Thread(() -> {
            final String user = "juanmanuelrojasj@gmail.com";
            final String pass = "lwtu nzug ctar yyke";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.debug", "true");

            Session sesion = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            try {
                Message mensaje = new MimeMessage(sesion);
                mensaje.setFrom(new InternetAddress(user));
                mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                mensaje.setSubject("Confirmación de su reserva en Great Viggio Hotel 🏨✨");

                String contenidoHTML = "<!DOCTYPE html>"
                        + "<html><body style='font-family:Arial;'>"
                        + "<h2>Estimado/a " + nombre + ",</h2>"
                        + "<p>Su reserva ha sido confirmada exitosamente.</p>"
                        + "<p><strong>Número de reserva:</strong> " + numeroReserva + "</p>"
                        + "<p>Gracias por elegir Great Viggio Hotel.</p>"
                        + "</body></html>";

                mensaje.setContent(contenidoHTML, "text/html; charset=utf-8");
                Transport.send(mensaje);

                System.out.println("✅ Correo de confirmación de reserva enviado a: " + email);

            } catch (MessagingException e) {
                System.err.println("❌ Error enviando correo de reserva a " + email + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // ========================== Plantillas para envío manual ==========================
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
                asunto = "¡Bienvenido a Great Viggio Hotel!";
                contmensaje = "<html><body><h2>¡Bienvenido, [Nombre del huésped]!</h2><p>Gracias por registrarte.</p></body></html>";
                break;
            case "reserva":
                asunto = "Confirmación de su reserva en Great Viggio Hotel 🏨✨";
                contmensaje = "<html><body><p>Su reserva ha sido confirmada.</p></body></html>";
                break;
            case "evento":
                asunto = "Confirmación de su evento";
                contmensaje = "<html><body><p>Hemos confirmado su inscripción.</p></body></html>";
                break;
            default:
                asunto = "";
                contmensaje = "";
                break;
        }
    }

    // ========================== Getters y Setters ==========================
    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getContmensaje() {
        return contmensaje;
    }

    public void setContmensaje(String contmensaje) {
        this.contmensaje = contmensaje;
    }

    public String getPlantillaSeleccionada() {
        return plantillaSeleccionada;
    }

    public void setPlantillaSeleccionada(String plantillaSeleccionada) {
        this.plantillaSeleccionada = plantillaSeleccionada;
    }

    public List<String> getDest() {
        return dest;
    }

    public void setDest(List<String> dest) {
        this.dest = dest;
    }

    public List<Usuario> getListaUsr() {
        return listaUsr;
    }

    public void setListaUsr(List<Usuario> listaUsr) {
        this.listaUsr = listaUsr;
    }
}
