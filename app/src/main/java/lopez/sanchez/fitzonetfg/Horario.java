package lopez.sanchez.fitzonetfg;

import java.util.Date;

public class Horario {
    private String id;
    private String url_img;
    private String horaInicio;
    private String horaFin;
    private String nombreEjercicio;
    private int plazasDisponibles;
    private String claseReserva;
    private Date fechaInicio;

    private int maximoPlazas;

    public Horario(String id, String url_img, String horaInicio, String horaFin, String nombreEjercicio, int plazasDisponibles, String claseReserva) {
        this.id = id;
        this.url_img = url_img;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombreEjercicio = nombreEjercicio;
        this.plazasDisponibles = plazasDisponibles;
        this.claseReserva = claseReserva;
    }

    public Horario(String url_img, String horaInicio, String horaFin, String nombreEjercicio, int plazasDisponibles, String claseReserva) {
        this(null, url_img, horaInicio, horaFin, nombreEjercicio, plazasDisponibles, claseReserva);
    }

    // Constructor vacío
    public Horario() {
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl_img() {
        return url_img;
    }

    public void setUrl_img(String url_img) {
        this.url_img = url_img;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }
    public Date getFechaInicio() {
        return fechaInicio;
    }



    public int getMaximoPlazas() {
        return maximoPlazas;
    }
    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public void setNombreEjercicio(String nombreEjercicio) {
        this.nombreEjercicio = nombreEjercicio;
    }

    public int getPlazasDisponibles() {
        return plazasDisponibles;
    }

    public void setPlazasDisponibles(int plazasDisponibles) {
        this.plazasDisponibles = plazasDisponibles;
    }

    public String getClaseReserva() {
        return claseReserva;
    }

    public void setClaseReserva(String claseReserva) {
        this.claseReserva = claseReserva;
    }

    public void decrementarPlazas() {
        this.plazasDisponibles--;
    }

    public void incrementarPlazas() {
        this.plazasDisponibles++;
    }

    public boolean isClickable() {
        return plazasDisponibles > 0;
    }
}
