package sebastian.Controladoras;

import io.javalin.Javalin;
import org.jasypt.util.text.AES256TextEncryptor;
import sebastian.Entidades.*;
import sebastian.Servicios.ComentarioS;
import sebastian.Servicios.ProductoS;
import sebastian.Servicios.UsuarioS;
import sebastian.Servicios.VentasS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdministrarProdC {
    private Javalin app;

    public AdministrarProdC(Javalin app) {
        this.app = app;
    }

    public void AplicarRutas(){
        app.get("/ver/:id",ctx -> {
            int id = ctx.pathParam("id", Integer.class).get();
            Producto temp = ProductoS.getInstance().find(id);
            List<Comentario> comments = ComentarioS.getInstancia().findComments(id);
            Map<String, Object> modelo = new HashMap<>();
            String user = ctx.cookie("usuario");
            modelo.put("temp",temp);
            modelo.put("comments",comments);
            modelo.put("user",user);
            ctx.render("/publico/verprod.vm",modelo);
        });

        app.get("/registrar", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("accion","/registrar");
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            modelo.put("cantidad",carrito.getProductos().size());
            ctx.render("/publico/productoCE.vm",modelo);
        });

        app.get("/remover/:id", ctx -> {
            ProductoS.getInstance().deleteProducto(ctx.pathParam("id",Integer.class).get());
            ctx.redirect("/productos");
        });

        app.get("/editar/:id", ctx -> {
            Producto temp = ProductoS.getInstance().find(ctx.pathParam("id", Integer.class).get());
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("producto",temp);
            modelo.put("accion","/editar/"+ctx.pathParam("id", Integer.class).get());

            CarroCompra carrito = ctx.sessionAttribute("carrito");
            modelo.put("cantidad",carrito.getProductos().size());
            ctx.render("/publico/productoCE.vm",modelo);
        });


        app.post("/editar/:id", ctx -> {
            String nombre = ctx.formParam("nombre");
            int precio = ctx.formParam("precio",Integer.class).get();
            String desc = ctx.formParam("desc");
            Producto temp = new Producto(nombre,precio,desc);
            temp.setId(ctx.pathParam("id",Integer.class).get());
            ProductoS.getInstance().edit(temp);

            ctx.redirect("/productos");
        });

        app.get("/ventas", ctx -> {
            if( ctx.cookie("usuario") == null || ctx.cookie("mist")== null){
                ctx.redirect("/autenti/ventas");
                return;
            } else{
                AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
                textEncryptor.setPassword("myEncryptionPassword");
                String mist = textEncryptor.decrypt(ctx.cookie("mist"));
                Usuario aux = new Usuario(ctx.cookie("usuario"),mist);
                if(!UsuarioS.autentificarUsuario(aux).equalsIgnoreCase("ADM")){
                    ctx.redirect("/autenti/ventas");
                    return;
                }
            }
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            List<VentasProductos> ventas = VentasS.getInstance().getVentas();
            for (VentasProductos venta: ventas) {
                System.out.println(venta.getId());
            }
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("ventas",ventas);
            modelo.put("cantidad",carrito.getProductos().size());

            ctx.render("/publico/ventasrealizadas.vm",modelo);
        });


        app.get("/productos", ctx -> {
            if( ctx.cookie("usuario") == null || ctx.cookie("mist")== null){
                ctx.redirect("/autenti/productos");
                return;
            } else{
                AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
                textEncryptor.setPassword("myEncryptionPassword");
                String mist = textEncryptor.decrypt(ctx.cookie("mist"));
                Usuario aux = new Usuario(ctx.cookie("usuario"),mist);
                if(!UsuarioS.autentificarUsuario(aux).equalsIgnoreCase("ADM")){
                    ctx.redirect("/autenti/ventas");
                    return;
                }
            }
            List<Producto> productos = ProductoS.getInstance().findProd(0,0);
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("productos",productos);
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            modelo.put("cantidad",carrito.getProductos().size());
            ctx.render("/publico/administrarproductos.vm",modelo);
        });
    }
}
