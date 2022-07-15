package sebastian.Controladoras;

import io.javalin.Javalin;
import sebastian.Entidades.CarroCompra;
import sebastian.Entidades.ProdComprado;
import sebastian.Entidades.Producto;
import sebastian.Entidades.VentasProductos;
import sebastian.Servicios.ProdCompradoS;
import sebastian.Servicios.ProductoS;
import sebastian.Servicios.VentasS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarritoC {
    private Javalin app;

    public CarritoC(Javalin app) {
        this.app = app;
    }

    public void AplicarRutas(){
        app.before(ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            if(carrito == null){
                carrito = new CarroCompra();
            }
            ctx.sessionAttribute("carrito",carrito);
        });
        app.get("/", ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            List<Producto> productos = ProductoS.getInstance().findProd(0, 10);

            Map<String, Object> modelo = new HashMap<>();
            modelo.put("productos",productos);
            modelo.put("cantidad",carrito.getProductos().size());
            List<String> paginas = Paginacion();
            modelo.put("paginas",paginas);
            ctx.render("/publico/catalogo.vm", modelo);
        });

        app.get("/comprar/:id", ctx -> {
            int pos = ctx.pathParam("id", Integer.class).get() * 10;
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            List<Producto> productos = ProductoS.getInstance().findProd(pos, pos+10);
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("productos",productos);
            modelo.put("cantidad",carrito.getProductos().size());
            List<String> paginas = Paginacion();
            modelo.put("paginas",paginas);
            ctx.render("/publico/catalogo.vm", modelo);
        });


        app.post("/comprar", ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");

            Producto temp = carrito.getProductosPorID(ctx.formParam("id",Integer.class).get());
            if(temp == null){
                temp = ProductoS.getInstance().find(ctx.formParam("id", Integer.class).get());
                temp.setCantidad(ctx.formParam("cantidad",Integer.class).get() );
                carrito.addProducto(temp);
                ctx.sessionAttribute("carrito",carrito);
                ctx.redirect("/comprar");
            }else{
                int pos = carrito.getPos(ctx.formParam("id",Integer.class).get());
                temp.setCantidad(ctx.formParam("cantidad",Integer.class).get() + temp.getCantidad());
                carrito.cambiarProducto(temp,pos);
            }

            ctx.sessionAttribute("carrito",carrito);
            ctx.redirect("/comprar");
        });

        app.get("/comprar", ctx -> {
            ctx.redirect("/");
        });

        app.get("/limpiar", ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            carrito.borrarProductos();

            ctx.redirect("/comprar");
        });

        app.get("/carrito", ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            if(carrito == null){
                carrito = new CarroCompra();
            }
            ctx.sessionAttribute("carrito",carrito);
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("productos",carrito.getProductos());
            modelo.put("cantidad",carrito.getProductos().size());
            ctx.render("/publico/carrito.vm",modelo);
        });

        app.get("/eliminar/:id", ctx -> {
            int id = ctx.pathParam("id", Integer.class).get();
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            carrito.eliminarProductoPorId(id);

            ctx.sessionAttribute("carrito",carrito);
            ctx.redirect("/carrito");
        });

        app.post("/procesar",ctx -> {
            CarroCompra carrito = ctx.sessionAttribute("carrito");
            if(carrito.getProductos().size() < 1){
                ctx.redirect("/carrito");
            }
            String nombre = ctx.formParam("nombre");
            VentasProductos venta = new VentasProductos(nombre);
            List<ProdComprado> list = ProdCompradoS.getInstance().convertProd(carrito.productos,venta.getId());
            venta.setListaProductos(list);
            VentasS.getInstance().create(venta);
            carrito.borrarProductos();
            ctx.sessionAttribute("carrito",carrito);
            ctx.redirect("/comprar");
        });
    }

    private static List<String> Paginacion() {
        int pag = ProductoS.getInstance().pag();
        List<String> list = new ArrayList<String>();
        for(int i = 0; i <= pag; i++){
            String aux = "<a class=\"page-link\" href=\"/comprar/"+i+"\">"+(i+1)+"</a>";
            list.add(aux);
        }
        return list;
    }
}


