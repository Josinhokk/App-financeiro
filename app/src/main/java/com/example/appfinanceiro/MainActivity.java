package com.example.appfinanceiro;

import android.os.Bundle;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = host.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        // Os ids do menu batem com os das destinations, então o NavigationUI
        // cuida de troca de aba, seleção e back stack por conta própria.
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Conteúdo respeita status bar; a barra inferior estende até a borda e
        // só afasta os itens da navigation bar do sistema.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            bottomNav.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }
}
