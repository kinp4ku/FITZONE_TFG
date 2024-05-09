package lopez.sanchez.fitzonetfg;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitzone.db";
    private static final int DATABASE_VERSION = 1;

    // CREACIÓN TABLA USUARIOS:
    private static final String CREATE_TABLE_USUARIOS = "CREATE TABLE Usuarios ( " +
            "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nombre TEXT, " +
            "apellidos TEXT, " +
            "dni TEXT PRIMARY KEY, " +
            "email TEXT, " +
            "contraseña TEXT, " +
            "sexo TEXT, " +
            "telefono_movil TEXT, " +
            "tipo TEXT )";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de Usuarios
        db.execSQL(CREATE_TABLE_USUARIOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si se realiza una actualización de la base de datos, aquí se especifican las acciones necesarias
        db.execSQL("DROP TABLE IF EXISTS Usuarios");
        onCreate(db);
    }
}