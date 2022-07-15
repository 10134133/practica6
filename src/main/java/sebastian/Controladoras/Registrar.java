package sebastian.Controladoras;

import sebastian.BootStrapServices;
import sebastian.Entidades.*;
import sebastian.BootStrapServices;

import sebastian.Servicios.*;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinVelocity;
import io.javalin.http.NotFoundResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Registrar {

    private  Javalin app;

    public Registrar(Javalin app) {
        this.app = app;
    }


    public void AplicarRutas(){
        app.post("/registrar", ctx -> {
            String nombre = ctx.formParam("nombre");
            int precio = ctx.formParam("precio",Integer.class).get();
            String desc = ctx.formParam("desc");
            List<Foto> fotos = new ArrayList<Foto>();
            ctx.uploadedFiles("img").forEach(uploadedFile -> {
                try {
                    byte[] bytes = uploadedFile.getContent().readAllBytes();
                    String encodedString = Base64.getEncoder().encodeToString(bytes);
                    Foto foto = new Foto(uploadedFile.getFilename(), uploadedFile.getContentType(), encodedString);
                    FotoS.getInstancia().create(foto);
                    fotos.add(foto);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Producto temp = new Producto(nombre,precio,desc);
            temp.setFotos(fotos);
            ProductoS.getInstance().create(temp);
            ctx.redirect("/productos");
        });
    }


}
