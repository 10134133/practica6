package sebastian;

import sebastian.Controladoras.AdministrarProdC;
import sebastian.Controladoras.CarritoC;
import sebastian.Controladoras.Registrar;
import sebastian.Controladoras.UsuarioC;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinVelocity;


/* Sebastián Sanchez 20180032 | 10134133
Para acceder a administrador y a la base de datos de h2 es user: admin   passw: ADMIN (asi en mayuscula)*/
public class Main {

    private static String Conexion = "";

    public static void main(String[] args){
        if(args.length >= 1){
            Conexion = args[0];
            System.out.println("Modo de Operacion: "+Conexion);
        }
        if(Conexion.isEmpty()){
            BootStrapServices.startDB();
        }

        Javalin app = Javalin.create().start(getHerokuAssignedPort());
        JavalinRenderer.register(JavalinVelocity.INSTANCE, ".vm");
        //Todas las rutas controladoras;
        new Registrar(app).AplicarRutas();
        new UsuarioC(app).AplicarRutas();
        new CarritoC(app).AplicarRutas();
        new AdministrarProdC(app).AplicarRutas();

    }

    public static String getConnection(){

        return Conexion;
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 7000; //Retorna el puerto por defecto en caso de no estar en Heroku.
    }

}