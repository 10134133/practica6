package sebastian.Controladoras;

import io.javalin.Javalin;
import org.jasypt.util.text.AES256TextEncryptor;
import sebastian.Entidades.Comentario;
import sebastian.Entidades.Usuario;
import sebastian.Servicios.ComentarioS;

import java.util.HashMap;
import java.util.Map;

public class UsuarioC {
    private Javalin app;

    public UsuarioC(Javalin app) {
        this.app = app;
    }

    public void AplicarRutas(){
        app.get("/logout", ctx -> {
            if(ctx.cookie("usuario")!= null && ctx.cookie("mist")!= null){
                ctx.removeCookie("usuario");
                ctx.removeCookie("mist");
            }
            ctx.redirect("/");
        });
        app.post("/addComment/:id", ctx->{
            String comment = ctx.formParam("coment");
            int id = ctx.pathParam("id", Integer.class).get();
            Comentario temp = new Comentario(comment,id);
            ComentarioS.getInstancia().create(temp);
            ctx.redirect("/ver/"+id);
        });

        app.get("/delComent/:id/:coment", ctx ->{
            int id = ctx.pathParam("id", Integer.class).get();
            int comment = ctx.pathParam("coment",Integer.class).get();
            System.out.println("El id del comentario es: "+comment);
            ComentarioS.getInstancia().deleteComent(comment);
            ctx.redirect("/ver/"+id);
        });

        app.get("/autenti/:direc", ctx -> {
            String direc = ctx.pathParam("direc");
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("direc",direc);
            ctx.render("/publico/autentificador.vm",modelo);
        });


        app.post("/autenti/:direc",ctx -> {
            String usuario = ctx.formParam("usuario");
            String pass = ctx.formParam("password");
            String temp = ctx.pathParam("direc");
            String recordar = ctx.formParam("recordar");

            if(usuario == null || pass == null){
                ctx.redirect("/autenti/"+temp);
            }
            Usuario user = new Usuario(usuario,pass);
            AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
            textEncryptor.setPassword("myEncryptionPassword");
            pass = textEncryptor.encrypt(pass);
            if(recordar != null){
                ctx.cookie("usuario", usuario,(3600*24*7));//Una semana en segundos
                ctx.cookie("mist", pass,(3600*24*7));
            }
            //Encriptar cookie
            ctx.cookie("usuario", usuario);
            ctx.cookie("mist", pass);
            ctx.redirect("/"+temp);

        });
    }
}
