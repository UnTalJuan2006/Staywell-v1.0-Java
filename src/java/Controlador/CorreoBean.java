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
                        + "  * {margin:0;padding:0;box-sizing:border-box;}"
                        + "  body {margin:0;padding:20px;font-family:'Segoe UI',Arial,sans-serif;background:linear-gradient(135deg,#f5f7fa 0%,#e8f5f3 100%);}"
                        + "  .email-wrapper {max-width:650px;margin:0 auto;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 20px 60px rgba(26,124,143,0.15);}"
                        + "  .header {background:linear-gradient(135deg,#1a7c8f 0%,#156b7d 100%);padding:50px 30px;text-align:center;position:relative;}"
                        + "  .header::before {content:'';position:absolute;top:0;left:0;right:0;height:4px;background:linear-gradient(90deg,#1a7c8f,#2dd4bf,#1a7c8f);}"
                        + "  .logo {font-size:36px;font-weight:800;color:#ffffff;letter-spacing:1px;margin:0;text-shadow:2px 2px 4px rgba(0,0,0,0.2);}"
                        + "  .tagline {color:#e0f2f1;font-size:14px;margin-top:8px;letter-spacing:2px;text-transform:uppercase;opacity:0.9;}"
                        + "  .content {padding:50px 40px;background:#ffffff;}"
                        + "  .welcome-badge {display:inline-block;background:linear-gradient(135deg,rgba(26,124,143,0.1),rgba(26,124,143,0.15));color:#1a7c8f;padding:8px 20px;border-radius:25px;font-size:13px;font-weight:700;letter-spacing:1px;text-transform:uppercase;margin-bottom:25px;border:2px solid rgba(26,124,143,0.3);}"
                        + "  .greeting {color:#1a7c8f;font-size:28px;font-weight:700;margin:0 0 20px 0;line-height:1.3;}"
                        + "  .intro-text {color:#555;font-size:16px;line-height:1.8;margin-bottom:20px;}"
                        + "  .features-box {background:linear-gradient(135deg,#f8fcfd 0%,#e8f4f6 100%);border-left:4px solid #1a7c8f;padding:25px;border-radius:12px;margin:30px 0;}"
                        + "  .feature-item {display:flex;align-items:start;margin-bottom:15px;}"
                        + "  .feature-item:last-child {margin-bottom:0;}"
                        + "  .feature-icon {background:#1a7c8f;color:#fff;width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:bold;font-size:14px;margin-right:15px;flex-shrink:0;}"
                        + "  .feature-text {color:#555;font-size:15px;line-height:1.6;}"
                        + "  .cta-container {text-align:center;margin:40px 0 30px 0;}"
                        + "  .cta-button {display:inline-block;background:linear-gradient(135deg,#1a7c8f 0%,#156b7d 100%);color:#ffffff;text-decoration:none;padding:16px 45px;border-radius:30px;font-weight:700;font-size:16px;letter-spacing:0.5px;box-shadow:0 8px 20px rgba(26,124,143,0.3);transition:all 0.3s ease;}"
                        + "  .cta-button:hover {transform:translateY(-3px);box-shadow:0 12px 30px rgba(26,124,143,0.4);}"
                        + "  .divider {height:2px;background:linear-gradient(90deg,transparent,#e8f4f6,transparent);margin:35px 0;}"
                        + "  .note {background:#fffbeb;border-left:4px solid #f59e0b;padding:15px 20px;border-radius:8px;margin:25px 0;}"
                        + "  .note-text {color:#92400e;font-size:14px;line-height:1.6;margin:0;}"
                        + "  .footer {background:linear-gradient(135deg,#f8f9fa 0%,#e9ecef 100%);padding:35px 40px;text-align:center;border-top:1px solid #e0e0e0;}"
                        + "  .footer-logo {color:#1a7c8f;font-size:20px;font-weight:700;margin-bottom:15px;}"
                        + "  .footer-text {color:#777;font-size:13px;line-height:1.8;margin-bottom:20px;}"
                        + "  .social-links {margin:20px 0;}"
                        + "  .social-icon {display:inline-block;width:36px;height:36px;background:#1a7c8f;color:#fff;border-radius:50%;text-decoration:none;margin:0 6px;line-height:36px;font-weight:bold;transition:all 0.3s ease;}"
                        + "  .social-icon:hover {background:#156b7d;transform:scale(1.1);}"
                        + "  .copyright {color:#999;font-size:12px;margin-top:20px;padding-top:20px;border-top:1px solid #ddd;}"
                        + "  @media screen and (max-width:640px) {"
                        + "    body {padding:10px;}"
                        + "    .header {padding:35px 20px;}"
                        + "    .logo {font-size:28px;}"
                        + "    .content {padding:35px 25px;}"
                        + "    .greeting {font-size:24px;}"
                        + "    .intro-text {font-size:15px;}"
                        + "    .features-box {padding:20px;}"
                        + "    .cta-button {padding:14px 35px;font-size:15px;}"
                        + "    .footer {padding:30px 25px;}"
                        + "  }"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div class='email-wrapper'>"
                        + "  <div class='header'>"
                        + "    <h1 class='logo'>Great Viaggio</h1>"
                        + "    <p class='tagline'>Tu experiencia de lujo comienza aquí</p>"
                        + "  </div>"
                        + "  <div class='content'>"
                        + "    <span class='welcome-badge'>✨ Nuevo Miembro</span>"
                        + "    <h2 class='greeting'>¡Bienvenido, " + nombre + "!</h2>"
                        + "    <p class='intro-text'>Nos complace enormemente darte la bienvenida a nuestra exclusiva comunidad de viajeros. Gracias por confiar en <strong>Great Viaggio Hotel</strong> para tus próximas aventuras y experiencias inolvidables.</p>"
                        + "    <div class='features-box'>"
                        + "      <div class='feature-item'>"
                        + "        <span class='feature-icon'>🏨</span>"
                        + "        <span class='feature-text'><strong>Reservas Inteligentes:</strong> Gestiona tus habitaciones con total flexibilidad desde nuestra plataforma</span>"
                        + "      </div>"
                        + "      <div class='feature-item'>"
                        + "        <span class='feature-icon'>🎉</span>"
                        + "        <span class='feature-text'><strong>Eventos Exclusivos:</strong> Accede a espacios premium para tus celebraciones especiales</span>"
                        + "      </div>"
                        + "      <div class='feature-item'>"
                        + "        <span class='feature-icon'>⭐</span>"
                        + "        <span class='feature-text'><strong>Servicios VIP:</strong> Disfruta de atención personalizada y beneficios únicos</span>"
                        + "      </div>"
                        + "    </div>"
                        + "    <div class='divider'></div>"
                        + "    <p class='intro-text' style='text-align:center;'>Estamos emocionados de acompañarte en cada paso de tu viaje. Tu satisfacción es nuestra prioridad.</p>"
                        + "    <div class='cta-container'>"
                        + "      <a href='https://www.greatviaggio.com/login' class='cta-button'>Acceder a mi cuenta →</a>"
                        + "    </div>"
                        + "  </div>"
                        + "  <div class='footer'>"
                        + "    <div class='footer-logo'>Great Viaggio</div>"
                        + "    <p class='footer-text'>"
                        + "      Calle131a#58D-20 , Bogotá, Colombia<br>"
                        + "      +57 3123466149 | contacto@greatviaggio.com"
                        + "    </p>"
                        + "    <div class='social-links'>"
                        + "      <a href='#' class='social-icon'>f</a>"
                        + "      <a href='#' class='social-icon'>𝕏</a>"
                        + "      <a href='#' class='social-icon'>in</a>"
                        + "      <a href='#' class='social-icon'>📷</a>"
                        + "    </div>"
                        + "    <p class='copyright'>"
                        + "      &copy; 2025 Great Viaggio Hotel. Todos los derechos reservados.<br>"
                        + "      Este correo fue enviado a tu dirección porque te registraste en nuestra plataforma."
                        + "    </p>"
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
    
    public static void enviarCorreoConfirmacionEvento(String email, String nombre, String numeroEvento) {
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
            mensaje.setSubject("🎉 Confirmación de Evento - Great Viaggio Hotel");

            String contenidoHTML = "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "  * {margin:0;padding:0;box-sizing:border-box;}"
                + "  body {margin:0;padding:20px;font-family:'Segoe UI',Arial,sans-serif;background:linear-gradient(135deg,#f5f7fa 0%,#e8f5f3 100%);}"
                + "  .email-wrapper {max-width:650px;margin:0 auto;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 20px 60px rgba(26,124,143,0.15);}"
                + "  .header {background:linear-gradient(135deg,#1a7c8f 0%,#156b7d 100%);padding:50px 30px;text-align:center;position:relative;}"
                + "  .header::before {content:'';position:absolute;top:0;left:0;right:0;height:4px;background:linear-gradient(90deg,#1a7c8f,#2dd4bf,#1a7c8f);}"
                + "  .logo {font-size:36px;font-weight:800;color:#ffffff;letter-spacing:1px;margin:0;text-shadow:2px 2px 4px rgba(0,0,0,0.2);}"
                + "  .tagline {color:#e0f2f1;font-size:14px;margin-top:8px;letter-spacing:2px;text-transform:uppercase;opacity:0.9;}"
                + "  .content {padding:50px 40px;background:#ffffff;}"

                + "  .success-badge {display:inline-block;background:linear-gradient(135deg,rgba(139,92,246,0.1),rgba(139,92,246,0.15));color:#6b21a8;padding:10px 24px;border-radius:25px;font-size:14px;font-weight:700;letter-spacing:1px;text-transform:uppercase;margin-bottom:25px;border:2px solid rgba(139,92,246,0.3);}"
                + "  .success-icon {font-size:24px;margin-right:8px;}"
                + "  .greeting {color:#1a7c8f;font-size:26px;font-weight:700;margin:0 0 20px 0;line-height:1.3;}"
                + "  .intro-text {color:#555;font-size:16px;line-height:1.8;margin-bottom:30px;}"

                + "  .event-card {background:linear-gradient(135deg,#faf5ff 0%,#f3e8ff 100%);border:2px solid #a855f7;border-radius:16px;padding:30px;margin:30px 0;box-shadow:0 4px 12px rgba(168,85,247,0.15);}"
                + "  .event-title {color:#6b21a8;font-size:18px;font-weight:700;margin:0 0 20px 0;text-transform:uppercase;letter-spacing:1px;text-align:center;}"
                + "  .event-number {background:linear-gradient(135deg,#a855f7 0%,#9333ea 100%);color:#ffffff;padding:15px;border-radius:10px;text-align:center;margin-bottom:25px;}"
                + "  .event-number-label {font-size:12px;text-transform:uppercase;letter-spacing:1px;opacity:0.9;margin-bottom:5px;}"
                + "  .event-number-value {font-size:28px;font-weight:800;letter-spacing:2px;}"

                + "</style>"
                + "</head>"
                + "<body>"

                + "<div class='email-wrapper'>"
                + "  <div class='header'>"
                + "    <h1 class='logo'>Great Viaggio</h1>"
                + "    <p class='tagline'>Eventos memorables, momentos inolvidables</p>"
                + "  </div>"

                + "  <div class='content'>"
                + "    <span class='success-badge'><span class='success-icon'>🎉</span> Evento Confirmado</span>"
                + "    <h2 class='greeting'>¡Estimado/a " + nombre + "!</h2>"

                + "    <p class='intro-text'>Nos complace confirmar que su reserva de evento ha sido procesada exitosamente. Gracias por confiar en <strong>Great Viaggio Hotel</strong>.</p>"

                + "    <div class='event-card'>"
                + "      <h3 class='event-title'>🎊 Detalles de su Evento</h3>"
                + "      <div class='event-number'>"
                + "        <div class='event-number-label'>Código de Confirmación</div>"
                + "        <div class='event-number-value'>" + numeroEvento + "</div>"
                + "      </div>"
                + "    </div>"

                + "    <div style='text-align:center;margin-top:30px;'>"
                + "      <a href='https://www.greatviaggio.com/mis-eventos' style='display:inline-block;background:linear-gradient(135deg,#a855f7,#9333ea);color:#fff;text-decoration:none;padding:14px 36px;border-radius:30px;font-weight:700;'>Ver Mi Evento →</a>"
                + "    </div>"

                + "  </div>"

                + "  <div class='footer' style='padding:30px;text-align:center;color:#777;font-size:13px;'>"
                + "    &copy; 2025 Great Viaggio Hotel - Departamento de Eventos"
                + "  </div>"

                + "</div>"
                + "</body>"
                + "</html>";

            mensaje.setContent(contenidoHTML, "text/html; charset=utf-8");
            Transport.send(mensaje);

            System.out.println("✅ Correo de confirmación de evento enviado a: " + email);

        } catch (MessagingException e) {
            System.err.println("❌ Error enviando correo de evento a " + email + ": " + e.getMessage());
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
                        + "<html lang='es'>"
                        + "<head>"
                        + "<meta charset='UTF-8'>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>"
                        + "  * {margin:0;padding:0;box-sizing:border-box;}"
                        + "  body {margin:0;padding:20px;font-family:'Segoe UI',Arial,sans-serif;background:linear-gradient(135deg,#f5f7fa 0%,#e8f5f3 100%);}"
                        + "  .email-wrapper {max-width:650px;margin:0 auto;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 20px 60px rgba(26,124,143,0.15);}"
                        + "  .header {background:linear-gradient(135deg,#1a7c8f 0%,#156b7d 100%);padding:50px 30px;text-align:center;position:relative;}"
                        + "  .header::before {content:'';position:absolute;top:0;left:0;right:0;height:4px;background:linear-gradient(90deg,#1a7c8f,#2dd4bf,#1a7c8f);}"
                        + "  .logo {font-size:36px;font-weight:800;color:#ffffff;letter-spacing:1px;margin:0;text-shadow:2px 2px 4px rgba(0,0,0,0.2);}"
                        + "  .tagline {color:#e0f2f1;font-size:14px;margin-top:8px;letter-spacing:2px;text-transform:uppercase;opacity:0.9;}"
                        + "  .content {padding:50px 40px;background:#ffffff;}"
                        + "  .success-badge {display:inline-block;background:linear-gradient(135deg,rgba(34,197,94,0.1),rgba(34,197,94,0.15));color:#15803d;padding:10px 24px;border-radius:25px;font-size:14px;font-weight:700;letter-spacing:1px;text-transform:uppercase;margin-bottom:25px;border:2px solid rgba(34,197,94,0.3);}"
                        + "  .success-icon {font-size:24px;margin-right:8px;}"
                        + "  .greeting {color:#1a7c8f;font-size:26px;font-weight:700;margin:0 0 20px 0;line-height:1.3;}"
                        + "  .intro-text {color:#555;font-size:16px;line-height:1.8;margin-bottom:30px;}"
                        + "  .reservation-card {background:linear-gradient(135deg,#f8fcfd 0%,#e8f4f6 100%);border:2px solid #1a7c8f;border-radius:16px;padding:30px;margin:30px 0;box-shadow:0 4px 12px rgba(26,124,143,0.1);}"
                        + "  .reservation-title {color:#1a7c8f;font-size:18px;font-weight:700;margin:0 0 20px 0;text-transform:uppercase;letter-spacing:1px;text-align:center;}"
                        + "  .reservation-number {background:#1a7c8f;color:#ffffff;padding:15px;border-radius:10px;text-align:center;margin-bottom:25px;}"
                        + "  .reservation-number-label {font-size:12px;text-transform:uppercase;letter-spacing:1px;opacity:0.9;margin-bottom:5px;}"
                        + "  .reservation-number-value {font-size:28px;font-weight:800;letter-spacing:2px;}"
                        + "  .info-grid {display:grid;gap:15px;}"
                        + "  .info-row {display:flex;justify-content:space-between;padding:12px 15px;background:#ffffff;border-radius:8px;border-left:3px solid #1a7c8f;}"
                        + "  .info-label {color:#777;font-size:14px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;}"
                        + "  .info-value {color:#333;font-size:15px;font-weight:700;}"
                        + "  .highlight-box {background:linear-gradient(135deg,rgba(245,158,11,0.1),rgba(245,158,11,0.15));border-left:4px solid #f59e0b;padding:20px;border-radius:10px;margin:30px 0;}"
                        + "  .highlight-title {color:#92400e;font-size:16px;font-weight:700;margin:0 0 10px 0;}"
                        + "  .highlight-text {color:#92400e;font-size:14px;line-height:1.6;margin:0;}"
                        + "  .features-box {background:#ffffff;border:2px solid #e8f4f6;padding:25px;border-radius:12px;margin:30px 0;}"
                        + "  .feature-item {display:flex;align-items:start;margin-bottom:15px;}"
                        + "  .feature-item:last-child {margin-bottom:0;}"
                        + "  .feature-icon {background:#1a7c8f;color:#fff;width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:bold;font-size:14px;margin-right:15px;flex-shrink:0;}"
                        + "  .feature-text {color:#555;font-size:15px;line-height:1.6;}"
                        + "  .cta-container {text-align:center;margin:40px 0 30px 0;}"
                        + "  .cta-button {display:inline-block;background:linear-gradient(135deg,#1a7c8f 0%,#156b7d 100%);color:#ffffff;text-decoration:none;padding:16px 45px;border-radius:30px;font-weight:700;font-size:16px;letter-spacing:0.5px;box-shadow:0 8px 20px rgba(26,124,143,0.3);transition:all 0.3s ease;margin:0 10px 10px 0;display:inline-flex;align-items:center;gap:8px;}"
                        + "  .cta-button:hover {transform:translateY(-3px);box-shadow:0 12px 30px rgba(26,124,143,0.4);}"
                        + "  .cta-secondary {background:linear-gradient(135deg,#ffffff 0%,#f8f9fa 100%);color:#1a7c8f;border:2px solid #1a7c8f;}"
                        + "  .cta-secondary:hover {background:linear-gradient(135deg,#e8f4f6 0%,#d1f0f3 100%);}"
                        + "  .divider {height:2px;background:linear-gradient(90deg,transparent,#e8f4f6,transparent);margin:35px 0;}"
                        + "  .footer {background:linear-gradient(135deg,#f8f9fa 0%,#e9ecef 100%);padding:35px 40px;text-align:center;border-top:1px solid #e0e0e0;}"
                        + "  .footer-logo {color:#1a7c8f;font-size:20px;font-weight:700;margin-bottom:15px;}"
                        + "  .footer-text {color:#777;font-size:13px;line-height:1.8;margin-bottom:20px;}"
                        + "  .social-links {margin:20px 0;}"
                        + "  .social-icon {display:inline-block;width:36px;height:36px;background:#1a7c8f;color:#fff;border-radius:50%;text-decoration:none;margin:0 6px;line-height:36px;font-weight:bold;transition:all 0.3s ease;}"
                        + "  .social-icon:hover {background:#156b7d;transform:scale(1.1);}"
                        + "  .copyright {color:#999;font-size:12px;margin-top:20px;padding-top:20px;border-top:1px solid #ddd;}"
                        + "  @media screen and (max-width:640px) {"
                        + "    body {padding:10px;}"
                        + "    .header {padding:35px 20px;}"
                        + "    .logo {font-size:28px;}"
                        + "    .content {padding:35px 25px;}"
                        + "    .greeting {font-size:22px;}"
                        + "    .intro-text {font-size:15px;}"
                        + "    .reservation-card {padding:20px;}"
                        + "    .info-row {flex-direction:column;gap:5px;}"
                        + "    .cta-button {padding:14px 30px;font-size:15px;width:100%;margin:5px 0;justify-content:center;}"
                        + "    .footer {padding:30px 25px;}"
                        + "  }"
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div class='email-wrapper'>"
                        + "  <div class='header'>"
                        + "    <h1 class='logo'>Great Viaggio</h1>"
                        + "    <p class='tagline'>Tu experiencia de lujo comienza aquí</p>"
                        + "  </div>"
                        + "  <div class='content'>"
                        + "    <span class='success-badge'><span class='success-icon'>✓</span> Reserva Confirmada</span>"
                        + "    <h2 class='greeting'>¡Estimado/a " + nombre + "!</h2>"
                        + "    <p class='intro-text'>Nos complace confirmar que su reserva ha sido procesada exitosamente. Estamos emocionados de recibirle pronto en <strong>Great Viaggio Hotel</strong> y hacer de su estadía una experiencia inolvidable.</p>"
                        + "    <div class='reservation-card'>"
                        + "      <h3 class='reservation-title'>📋 Detalles de su Reserva</h3>"
                        + "      <div class='reservation-number'>"
                        + "        <div class='reservation-number-label'>Número de Confirmación</div>"
                        + "        <div class='reservation-number-value'>" + numeroReserva + "</div>"
                        + "      </div>"
                        + "      <div class='info-grid'>"
                        + "        <div class='info-row'>"
                        + "          <span class='info-label'>Estado</span>"
                        + "          <span class='info-value' style='color:#15803d;'>✓ Confirmada</span>"
                        + "        </div>"
                        + "        <div class='info-row'>"
                        + "          <span class='info-label'>Huésped Principal</span>"
                        + "          <span class='info-value'>" + nombre + "</span>"
                        + "        </div>"
                        + "        <div class='info-row'>"
                        + "          <span class='info-label'>Código de Reserva</span>"
                        + "          <span class='info-value'>" + numeroReserva + "</span>"
                        + "        </div>"
                        + "      </div>"
                        + "    </div>"
                        + "    <div class='highlight-box'>"
                        + "      <p class='highlight-title'>📌 Importante - Guarde este correo</p>"
                        + "      <p class='highlight-text'>Por favor, conserve este número de confirmación. Lo necesitará para hacer el check-in y para cualquier consulta relacionada con su reserva.</p>"
                        + "    </div>"
                        + "    <div class='features-box'>"
                        + "      <div class='feature-item'>"
                        + "        <span class='feature-icon'>🔔</span>"
                        + "        <span class='feature-text'><strong>Recordatorios Automáticos:</strong> Le enviaremos notificaciones antes de su llegada con información útil</span>"
                        + "      </div>"
                        + "      <div class='feature-item'>"
                        + "        <span class='feature-icon'>💬</span>"
                        + "        <span class='feature-text'><strong>Soporte 24/7:</strong> Nuestro equipo está disponible en cualquier momento para asistirle</span>"
                        + "      </div>"
                        + "    </div>"
                        + "    <div class='cta-container'>"
                        + "      <a href='https://www.greatviaggio.com/mis-reservas' class='cta-button'>Ver Mi Reserva →</a>"
                        + "      <a href='https://www.greatviaggio.com/servicios' class='cta-button cta-secondary'>Explorar Servicios</a>"
                        + "    </div>"
                        + "    <div class='divider'></div>"
                        + "    <p class='intro-text' style='text-align:center;margin-bottom:0;'>Gracias por elegir <strong>Great Viaggio Hotel</strong>. Esperamos ofrecerle una experiencia excepcional.</p>"
                        + "  </div>"
                        + "  <div class='footer'>"
                        + "    <div class='footer-logo'>Great Viaggio</div>"
                        + "    <p class='footer-text'>"
                            + "      Calle131a #58D-20, Bogotá, Colombia<br>"
                        + "      +3123466149 | reservas@greatviaggio.com<br>"
                        + "      Atención al cliente disponible 24/7"
                        + "    </p>"
                        + "    <div class='social-links'>"
                        + "      <a href='#' class='social-icon'>f</a>"
                        + "      <a href='#' class='social-icon'>𝕏</a>"
                        + "      <a href='#' class='social-icon'>in</a>"
                        + "      <a href='#' class='social-icon'>📷</a>"
                        + "    </div>"
                        + "    <p class='copyright'>"
                        + "      &copy; 2025 Great Viaggio Hotel. Todos los derechos reservados.<br>"
                        + "      Si tiene alguna pregunta sobre su reserva, no dude en contactarnos."
                        + "    </p>"
                        + "  </div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

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
