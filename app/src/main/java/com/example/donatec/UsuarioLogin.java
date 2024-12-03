package com.example.donatec;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class UsuarioLogin {
    // atributos
    private String correo;
    private String contrasena;

    // constructor parametrizado
    public UsuarioLogin(String correo, String contrasena) {
        setCorreo(correo);
        setContrasena(contrasena);
    }

    // metodo para validar si los campos son valores aceptables
    public boolean validarCampos() {
        return correo != null && contrasena != null;
    }

    // metodo para obtener los datos del usuario en formato JSON
    public JSONObject getJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", this.correo);
            jsonObject.put("password", this.contrasena);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    // metodos de acceso get y set
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

    private String validarCampo(String campo, int minLength, int maxLength) {
        return (campo != null && !campo.isBlank() && campo.length() >= minLength && campo.length() <= maxLength) ? campo : null;
    }
}
