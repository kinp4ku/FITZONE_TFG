package lopez.sanchez.fitzonetfg;

public class listElement {
    public int color;
    public String name;
    public String horaInicio;
    public String horaFin;

    public listElement(int color, String name, String horaInicio, String horaFin) {
        this.color = color;
        this.name = name;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}