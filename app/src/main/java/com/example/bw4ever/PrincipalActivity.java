package com.example.bw4ever;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.bw4ever.vistas.AcercaDeActivity;
import com.example.bw4ever.vistas.EditarPerfilActivity;
import com.example.bw4ever.vistas.HomeFragment;
import com.example.bw4ever.vistas.OpcionesFragment;
import com.example.bw4ever.vistas.RutinasFragment;
import com.example.bw4ever.vistas.agregarParqueActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class PrincipalActivity extends AppCompatActivity {
    // --- Elementos del Layout ---
    Toolbar toolbar;
    TabLayout tabLayout;
    ImageView usuario;
    ViewPager viewPager;
    AdapterPager adapterPager;
    // --- ---

    // --- Fragments del Layout ---
    private RutinasFragment rutinasFragment;
    // --- ---
    public static final int CODE_GALLERY= 1; //Constante de Acción para acceder a galería, al agregar Rutinas.

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        toolbar = findViewById(R.id.tool_bar);
        tabLayout = findViewById(R.id.tab_bar);
        viewPager = findViewById(R.id.view_pager);
        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);
        adapterPager = new AdapterPager(getSupportFragmentManager());
        viewPager.setAdapter(adapterPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_map);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_rutinas);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_opciones);

        setTitle("BW4Ever - "+ firebaseAuth.getCurrentUser().getDisplayName() ); //Coloca título.
    }

    public void cerrarSesion(View view) {
        firebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void agregarParque(View view) {
        startActivity(new Intent(this, agregarParqueActivity.class));
    }

    public void editarPerfil(View view) {
        startActivity(new Intent(this, EditarPerfilActivity.class));
    }

    public void acercaDe(View view) {
        startActivity(new Intent(this, AcercaDeActivity.class));
    }

    // --- Clase que Genera cada Fragment ---
    public class AdapterPager extends FragmentPagerAdapter{

        public AdapterPager(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    HomeFragment h = new HomeFragment();
                    return h;
                case 1:
                    MapsActivity m = new MapsActivity();
                    return m;
                case 2:
                    RutinasFragment r = new RutinasFragment();
                    return r;
                case 3:
                    OpcionesFragment o = new OpcionesFragment();
                    return o;
            }
            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Home";
                case 1:
                    return "Mapa";
                case 2:
                    return "Rutinas";
                case 3:
                    return "Opciones";
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
    // --- Fin Clase que Genera cada Fragment ---

    // *** Fragments al cargar Foto de Perfil pierden el foco, por eso se requiere éste método acá ***
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case CODE_GALLERY: //Caso Acceder a la Galería.
                rutinasFragment.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    // --- ---
}