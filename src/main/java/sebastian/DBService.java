package sebastian;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class DBService<T> {

    private static EntityManagerFactory e;
    private Class<T> clase;

    public DBService(Class<T> clase) {
        if(e == null) {
            if(Main.getConnection().equalsIgnoreCase("Heroku")){
                e = getConfiguracionBaseDatosHeroku();
            }else{
                e = Persistence.createEntityManagerFactory("MiUnidadPersistencia");
            }
        }
        this.clase = clase;
    }

    private EntityManagerFactory getConfiguracionBaseDatosHeroku(){
        //Leyendo la información de la variable de ambiente de Heroku
        String databaseUrl = System.getenv("DATABASE_URL");
        StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
        //Separando las información del conexión.
        String dbVendor = st.nextToken();
        String userName = st.nextToken();
        String password = st.nextToken();
        String host = st.nextToken();
        String port = st.nextToken();
        String databaseName = st.nextToken();
        //creando la jbdc String
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, databaseName);
        //pasando las propiedades.
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", jdbcUrl );
        properties.put("javax.persistence.jdbc.user", userName );
        properties.put("javax.persistence.jdbc.password", password );
        //
        return Persistence.createEntityManagerFactory("Heroku", properties);
    }
    public EntityManager getEntityManager() {
        return e.createEntityManager();
    }

    public T create(T entity) throws IllegalArgumentException, PersistenceException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entity;
    }

    public T edit(T entity) throws PersistenceException {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            em.merge(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entity;
    }

    public T find(Object id) throws PersistenceException {
        EntityManager em = getEntityManager();

        try {
            return em.find(clase, id);
        } finally {
            em.close();
        }

    }
}