package Controlador;

import Modelo.Usuario;
import java.io.IOException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class PerfilHuespedBean implements Serializable {

    private Usuario usuarioLogueado;

    @PostConstruct
    public void init() {
        cargarUsuarioDeSesion();
    }

    private void redirigirLogin(ExternalContext externalContext) {
        try {
            externalContext.redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarUsuarioDeSesion() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }

        ExternalContext externalContext = context.getExternalContext();
        Object usuarioEnSesion = externalContext.getSessionMap().get("usuarioLogueado");

        if (usuarioEnSesion instanceof Usuario) {
            usuarioLogueado = (Usuario) usuarioEnSesion;
        } else {
            redirigirLogin(externalContext);
        }
    }

    public Usuario getUsuarioLogueado() {
        if (usuarioLogueado == null) {
            cargarUsuarioDeSesion();
        }
        return usuarioLogueado;
    }

    public void verificarSesion() {
        if (getUsuarioLogueado() == null) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            redirigirLogin(externalContext);
        }
    }
}
