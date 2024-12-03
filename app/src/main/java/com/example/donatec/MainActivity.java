package com.example.donatec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa el SessionManager
        SessionManager sessionManager = new SessionManager(this);

        // Verifica si el usuario ha iniciado sesión
        if (!sessionManager.isLoggedIn()) {
            // Si no ha iniciado sesión, redirige a LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Termina MainActivity para evitar volver a ella al presionar "atrás"
            return; // Asegúrate de que no se ejecute el resto de onCreate
        }

        BottomNavigationView menu = findViewById(R.id.menu);
        NavHostFragment principalFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.principalFragment);

        if (principalFragment != null) {
            NavController navController = principalFragment.getNavController();
            NavigationUI.setupWithNavController(menu, navController);
        } else {
            Log.e("MainActivity", "principalFragment is null");
        }
    }
}