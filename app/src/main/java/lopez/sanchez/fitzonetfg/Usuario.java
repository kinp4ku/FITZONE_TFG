package lopez.sanchez.fitzonetfg;

import java.io.Serializable;

public class Usuario  {
    private String nombre;
    private String apellidos;
    private String dni;
    private String correo;
    private String contraseña;
    private String sexo;
    private String tipo;

    //constructor vacio:
    public Usuario(){

    }
    public Usuario(String nombre){
        this.nombre=nombre;
    }
    // Constructor
    public Usuario(String nombre, String apellidos, String dni, String correo, String contraseña, String sexo, String tipo) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.dni = dni;
        this.correo = correo;
        this.contraseña = contraseña;
        this.sexo = sexo;
        this.tipo = tipo;
    }

    // Getters y setters (puedes generarlos automáticamente en Android Studio)
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}