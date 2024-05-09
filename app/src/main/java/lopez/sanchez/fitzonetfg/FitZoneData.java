package lopez.sanchez.fitzonetfg;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class FitZoneData {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;


    // Constructor
    public FitZoneData(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Método para abrir la base de datos
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Método para cerrar la base de datos
    public void close() {
        dbHelper.close();
    }

    // Método para agregar un nuevo usuario
    public long añadirUsuario(String nombre, String apellidos, String dni, String email, String contraseña, String sexo, String telefonoMovil, String tipo, byte[] imagen) {


    ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("apellidos", apellidos);
        values.put("dni", dni);
        values.put("email", email);
        values.put("contraseña", contraseña);
        values.put("sexo", sexo);
        values.put("telefono_movil", telefonoMovil);
        values.put("tipo", tipo);
        values.put("imagen", imagen); // Añadir la imagen como un array de bytes

        // Insertar fila en la tabla de Usuarios
        return database.insert("Usuarios", null, values);
    }
}
