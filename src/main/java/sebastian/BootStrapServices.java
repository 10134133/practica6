package sebastian;


import org.h2.tools.Server;
import java.sql.SQLException;


public class BootStrapServices {
    private static BootStrapServices instancia;
    private static Server server;
    public static BootStrapServices getInstancia(){
        if(instancia == null){
            instancia=new BootStrapServices();
        }
        return instancia;
    }
    public static void startDB() {
        try {
            server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers", "-ifNotExists").start();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void stopDB() throws SQLException {
        if(server != null){
            server.stop();
        }
    }



}