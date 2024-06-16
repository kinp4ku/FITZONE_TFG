package lopez.sanchez.fitzonetfg;

public class Usuario  {
    private String NOMBRE;
    private String APELLIDOS;
    private String dni;
    private String correo;
    private String contraseña;
    private String sexo;
    private String tipo;
    private String imgUrl;
    private String id;
    private String nombre;

    public Usuario(String NOMBRE, String APELLIDOS, String imgUrl) {
        this.NOMBRE = NOMBRE;
        this.APELLIDOS = APELLIDOS;
        this.imgUrl = imgUrl;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Usuario(){

    }
    public Usuario(String NOMBRE){
        this.NOMBRE=NOMBRE;
    }
    // Constructor
    public Usuario(String NOMBRE, String APELLIDOS, String dni, String correo, String contraseña, String sexo, String tipo) {
        this.NOMBRE = NOMBRE;
        this.APELLIDOS = APELLIDOS;
        this.dni = dni;
        this.correo = correo;
        this.contraseña = contraseña;
        this.sexo = sexo;
        this.tipo = tipo;
    }

    public String getNombre() {
        return NOMBRE;
    }

    public void setNombre(String NOMBRE) {
        this.NOMBRE = NOMBRE;
    }

    public String getApellidos() {
        return APELLIDOS;
    }

    public void setApellidos(String APELLIDOS) {
        this.APELLIDOS = APELLIDOS;
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

