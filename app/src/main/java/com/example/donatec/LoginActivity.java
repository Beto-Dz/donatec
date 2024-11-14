package com.example.donatec;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText email, password;
    private Button btnLogin, btnRegister;
    private SessionManager sessionManager;
    private RequestQueue queue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa el SessionManager
        sessionManager = new SessionManager(this);

        // Inicializar la cola de solicitudes de Volley
        queue = Volley.newRequestQueue(this);

        // Verifica si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            // Redirigir a MainActivity si ya está logueado
            startActivity(new Intent(this, MainActivity.class));
            // Cerrar la actividad actual
            finish();
        }

        // Inicializar los elementos de la vista relacionados con el login
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.ingresoButton);
        btnRegister = findViewById(R.id.registroButton);

        // Configurar el evento de clic en los botones
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        // Configurar el evento de clic en el botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirigir a RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    // Método para realizar la solicitud de inicio de sesión
    private void login() {
        // Aquí se debe realizar la solicitud a la API
        String url = getString(R.string.api_base_url) + "login";

        // Crear un objeto UsuarioLogin con los datos ingresados por el usuario
        UsuarioLogin usuarioLogin = new UsuarioLogin(email.getText().toString(), password.getText().toString());

        // Validar los campos ingresados por el usuario
        if (!usuarioLogin.validarCampos()) {
            Toast.makeText(this, "Llena los campos correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, usuarioLogin.getJSON(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Procesar la respuesta
                        try {
                            // Obtener el código de estado de la respuesta
                            int codigo = response.getInt("code");
                            if (codigo == 200) {

                                JSONObject user = response.getJSONObject("user");
                                int id_person = user.getInt("id_person");
                                int id_account = user.getInt("id_account");
                                String name = user.getString("name");
                                String paternal_surname = user.getString("paternal_surname");
                                String maternal_surname = user.getString("maternal_surname");
                                String state_user = user.getString("state_user");
                                int municipality_id = user.getInt("municipality_id");
                                String username = user.getString("username");
                                String email = user.getString("email");
                                String token = response.getString("token");

                                sessionManager.createLoginSession(true, id_person, id_account, name, paternal_surname, maternal_surname, state_user, municipality_id, username, email, token);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }
}
