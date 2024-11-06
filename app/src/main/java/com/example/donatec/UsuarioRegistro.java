package com.example.donatec;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class UsuarioRegistro {
    // Atributos
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String fechaNacimiento;
    private String municipio;
    private String estado;
    private int sexoID;
    private String username;
    private String descripcion;
    private String correo;
    private String contrasena;
    private String numeroTelefono;

    // Constructor parametrizado
    public UsuarioRegistro(String nombre, String apellidoPaterno, String apellidoMaterno, String fechaNacimiento, String municipio, String estado, int sexoID, String username, String descripcion, String correo, String contrasena, String numeroTelefono) {
        setNombre(nombre);
        setApellidoPaterno(apellidoPaterno);
        setApellidoMaterno(apellidoMaterno);
        setFechaNacimiento(fechaNacimiento);
        setMunicipio(municipio);
        setEstado(estado);
        setSexoID(sexoID);
        setUsername(username);
        setDescripcion(descripcion);
        setCorreo(correo);
        setContrasena(contrasena);
        setNumeroTelefono(numeroTelefono);
    }

    // Método para validar si todos los campos están llenos
    public boolean validarCampos() {
        return nombre != null && apellidoPaterno != null && apellidoMaterno != null && fechaNacimiento != null && municipio != null && estado != null && sexoID != 0 && username != null && descripcion != null && correo != null && contrasena != null && numeroTelefono != null;
    }

    // Método para obtener toda la información en formato JSON
    public JSONObject getUsuarioAsJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", this.nombre);
            jsonObject.put("paternal_surname", this.apellidoPaterno);
            jsonObject.put("maternal_surname", this.apellidoMaterno);
            jsonObject.put("birthdate", this.fechaNacimiento);
            jsonObject.put("municipality_name", this.municipio);
            jsonObject.put("state_name", this.estado);
            jsonObject.put("gender_id", this.sexoID);
            jsonObject.put("username", this.username);
            jsonObject.put("description", this.descripcion);
            jsonObject.put("email", this.correo);
            jsonObject.put("password", this.contrasena);
            jsonObject.put("phone_number", this.numeroTelefono);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    // Métodos de acceso con validación de longitud
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = validarCampo(nombre, 3, 20);
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = validarCampo(apellidoPaterno, 3, 20);
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = validarCampo(apellidoMaterno, 3, 20);
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        // Validar que la fecha no esté vacía y tenga una longitud de 10 caracteres
        if (fechaNacimiento == null || fechaNacimiento.isBlank() || fechaNacimiento.length() != 10) {
            this.fechaNacimiento = null;  // Asignar null si está vacía o no tiene el tamaño correcto
            return;
        }

        // Validar que la fecha esté en el formato "yyyy/MM/dd" (año/mes/día)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateFormat.setLenient(false);  // Asegura que solo fechas válidas se procesen

        try {
            Date fecha = dateFormat.parse(fechaNacimiento);
            if (esMayorDeEdad(fecha)) {
                this.fechaNacimiento = fechaNacimiento;  // Asigna la fecha si es válida y mayor de 18 años
            } else {
                this.fechaNacimiento = null;  // Asigna null si no es mayor de 18 años
            }
        } catch (ParseException e) {
            this.fechaNacimiento = null;  // Asigna null si la fecha tiene un formato incorrecto
        }
    }

    // Método para verificar si la fecha indica una edad mayor o igual a 18 años
    private boolean esMayorDeEdad(Date fechaNacimiento) {
        Calendar fechaActual = Calendar.getInstance();
        Calendar fechaNacimientoCal = Calendar.getInstance();
        fechaNacimientoCal.setTime(fechaNacimiento);

        int edad = fechaActual.get(Calendar.YEAR) - fechaNacimientoCal.get(Calendar.YEAR);

        // Si el mes y día de nacimiento aún no han ocurrido este año, resta 1 de la edad
        if (fechaActual.get(Calendar.MONTH) < fechaNacimientoCal.get(Calendar.MONTH) ||
                (fechaActual.get(Calendar.MONTH) == fechaNacimientoCal.get(Calendar.MONTH) && fechaActual.get(Calendar.DAY_OF_MONTH) < fechaNacimientoCal.get(Calendar.DAY_OF_MONTH))) {
            edad--;
        }

        return edad >= 18;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = validarCampo(municipio, 3, 60);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = validarCampo(estado, 3, 32);
    }

    public int getSexoID() {
        return sexoID;
    }

    public void setSexoID(int sexoID) {
        this.sexoID = sexoID > 0 ? sexoID : 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = validarCampo(username, 3, 32);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = validarCampo(descripcion, 3, 60);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        // Patrón para validar el formato de correo electrónico
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        if (!correo.isBlank() && correo != null && pattern.matcher(correo).matches() && correo.length() >= 3 && correo.length() <= 22) {
            this.correo = correo;
        } else {
            this.correo = null;
        }
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = validarCampo(contrasena, 3, 30);
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = validarCampo(numeroTelefono, 3, 10);
    }

    // Método de utilidad para validar los campos de texto
    private String validarCampo(String campo, int minLength, int maxLength) {
        return (campo != null && !campo.isBlank() && campo.length() >= minLength && campo.length() <= maxLength) ? campo : null;
    }
}
