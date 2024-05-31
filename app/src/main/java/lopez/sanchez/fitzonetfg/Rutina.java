package lopez.sanchez.fitzonetfg;

import java.util.ArrayList;

public class Rutina {
    private String nombreRutina;
    private ArrayList<Ejercicio> ejercicios;

    public Rutina() {
        // Constructor vacío requerido para Firestore
    }

    public Rutina(String nombreRutina, ArrayList<Ejercicio> ejercicios) {
        this.nombreRutina = nombreRutina;
        this.ejercicios = ejercicios;
    }

    public String getNombreRutina() {
        return nombreRutina;
    }

    public ArrayList<Ejercicio> getEjercicios() {
        return ejercicios;
    }
}
