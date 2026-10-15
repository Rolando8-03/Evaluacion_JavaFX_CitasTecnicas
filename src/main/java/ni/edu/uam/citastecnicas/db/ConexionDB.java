package ni.edu.uam.citastecnicas.db;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public final class ConexionDB {
    private ConexionDB() {}
    public static Connection conectar() throws SQLException {
        Properties archivo=new Properties();
        File ruta=new File("conf/db.properties");
        if (ruta.exists()) {
            try (InputStreamReader entrada=new InputStreamReader(new FileInputStream(ruta), "UTF-8")) { archivo.load(entrada); }
            catch (IOException e) { throw new SQLException("No se pudo leer conf/db.properties.", "CONFIG", e); }
        }
        String url=valor("DB_URL", archivo, "db.url", "jdbc:postgresql://localhost:5432/gestion_citas");
        String usuario=valor("DB_USER", archivo, "db.user", "postgres");
        String clave=valor("DB_PASSWORD", archivo, "db.password", "");
        if (!url.startsWith("jdbc:postgresql:")) throw new SQLException("Usá una URL JDBC de PostgreSQL.", "CONFIG");
        if (usuario.trim().isEmpty()) throw new SQLException("El usuario de PostgreSQL es obligatorio.", "CONFIG");
        Properties opciones=new Properties();
        opciones.setProperty("user", usuario); opciones.setProperty("password", clave);
        opciones.setProperty("connectTimeout", "5"); opciones.setProperty("socketTimeout", "10");
        opciones.setProperty("ApplicationName", "Evaluacion_JavaFX_CitasTecnicas");
        return DriverManager.getConnection(url, opciones);
    }
    private static String valor(String entorno, Properties p, String clave, String defecto) {
        String v=System.getenv(entorno);
        return v != null ? v : p.getProperty(clave, defecto);
    }
    // SQL fijo sin entradas: cumple el requisito de Statement.
    public static boolean comprobar() throws SQLException {
        try (Connection c=conectar(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT 1")) {
            return r.next() && r.getInt(1)==1;
        }
    }
}
