
package lopez.sanchez.fitzonetfg;

public class Ejercicio {
    private String nombreEjercicio;
    private String trabajoEjercicio;
    private String repeticionesEjercicio;
    private String url_img;

    public Ejercicio() {
        // Constructor vacío requerido para Firestore
    }
    public Ejercicio(String nombreEjercicio) {
        this.nombreEjercicio = nombreEjercicio;
    }
    public Ejercicio(String nombreEjercicio, String trabajoEjercicio, String repeticionesEjercicio, String url_img) {
        this.nombreEjercicio = nombreEjercicio;
        this.trabajoEjercicio = trabajoEjercicio;
        this.repeticionesEjercicio = repeticionesEjercicio;
        this.url_img = url_img;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public void setNombreEjercicio(String nombreEjercicio) {
        this.nombreEjercicio = nombreEjercicio;
    }

    public String getTrabajoEjercicio() {
        return trabajoEjercicio;
    }

    public void setTrabajoEjercicio(String trabajoEjercicio) {
        this.trabajoEjercicio = trabajoEjercicio;
    }

    public String getRepeticionesEjercicio() {
        return repeticionesEjercicio;
    }

    public void setRepeticionesEjercicio(String repeticionesEjercicio) {
        this.repeticionesEjercicio = repeticionesEjercicio;
    }

    public String getUrl_img() {
        return url_img;
    }

    public void setUrl_img(String url_img) {
        this.url_img = url_img;
    }
}
