package com.example.bw4ever.vistas;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bw4ever.R;
import com.example.bw4ever.adapter.RutinaAdapter;
import com.example.bw4ever.modelo.ParqueService;
import com.example.bw4ever.modelo.Rutina;
import com.example.bw4ever.modelo.RutinaService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RutinasFragment extends Fragment {

    public RutinasFragment() {
        // Required empty public constructor
    }

    // --- Elementos del Fragment ---
    RecyclerView rc;
    EditText txtnombre;
    Spinner spinnerdificultad;
    ImageButton btnbuscarnombre, btnlimpiarrutinas;
    Button btncancelar;
    // --- ---

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas, container, false);
        rc = view.findViewById(R.id.rc);

        // --- Inicializar los controles ---
        txtnombre = view.findViewById(R.id.txtnombre);
        spinnerdificultad = view.findViewById(R.id.spinnerdificultad);
        btnbuscarnombre = view.findViewById(R.id.bt_buscar_rutina_nombre);
        //btnbuscardificultad = view.findViewById(R.id.bt_buscar_rutina_dificultad);
        btnlimpiarrutinas = view.findViewById(R.id.bt_limpiar_rutinas);
        btncancelar = view.findViewById(R.id.btncancelarfiltro);
        btncancelar.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.opciones, R.layout.spinner_dificultad_rutina);
        spinnerdificultad.setAdapter(spinnerAdapter);
        // --- ---

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(RecyclerView.VERTICAL);
        rc.setLayoutManager(lm);

        RutinaAdapter adapter = new RutinaAdapter(RutinaService.rutinaList, R.layout.item,getActivity());
        rc.setAdapter(adapter);

        //cargaRutinasFirebase();


        // --- Botón Buscar Rutina por nombre ---
        btnbuscarnombre.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                esconderTeclado(getContext(), getView());

                if (txtnombre.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "¡ No ha ingresado un nombre de rutina !", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Rutinas");
                    Query query = reference.orderByChild("nombre").equalTo(txtnombre.getText().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Rutina r = ds.getValue(Rutina.class);
                                r.setId(ds.getKey());
                                if(!RutinaService.rutinaList.contains(r)){
                                    RutinaService.addRutina(r);
                                    rc.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getContext(), "¡ Búsqueda terminada !", Toast.LENGTH_SHORT).show();
                    clear();
                }
            }
        });
        // --- ---

        // --- Botón Buscar Rutina por dificultad ---

        spinnerdificultad.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                esconderTeclado(getContext(), getView());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Rutinas");
                Query query = reference.orderByChild("dificultad").equalTo(spinnerdificultad.getSelectedItem().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Rutina r = ds.getValue(Rutina.class);
                            r.setId(ds.getKey());
                            if(!RutinaService.rutinaList.contains(r)){
                                RutinaService.addRutina(r);
                                    rc.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    if( position>0){
                        btncancelar.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "¡Búsqueda terminada!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        RutinaService.rutinaList.clear();
                        rc.getAdapter().notifyDataSetChanged();
                    }
                    clear();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                esconderTeclado(getContext(), getView());
            }

        });

        btncancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RutinaService.rutinaList.clear();
                rc.getAdapter().notifyDataSetChanged();
                spinnerdificultad.setSelection(0);
            }
        });


     /*
     btnbuscardificultad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                esconderTeclado(getContext(), getView());

                if (spinnerdificultad.getSelectedItemPosition() == 0){
                    Toast.makeText(getContext(), "¡ No ha seleccionado una dificultad !", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Rutinas");
                    Query query = reference.orderByChild("dificultad").equalTo(spinnerdificultad.getSelectedItem().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Rutina r = ds.getValue(Rutina.class);
                                r.setId(ds.getKey());
                                if(!RutinaService.rutinaList.contains(r)){
                                    RutinaService.addRutina(r);
                                    rc.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getContext(), "¡Búsqueda terminada!", Toast.LENGTH_SHORT).show();
                    clear();
                }
            }
        });
        // --- ---*/

        // --- Botón Limpiar Listado ---
        btnlimpiarrutinas.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                RutinaService.rutinaList.clear();
                rc.getAdapter().notifyDataSetChanged();
                clear();
            }
        });
        // --- ---

        return view;
    }

    public void cargaRutinasFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Rutinas");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Rutina rutina = dataSnapshot.getValue(Rutina.class);
                rutina.setId(dataSnapshot.getKey());

                if (!RutinaService.rutinaList.contains(rutina)) {
                    RutinaService.addRutina(rutina);
                }

                rc.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Rutina rutina = dataSnapshot.getValue(Rutina.class);
                rutina.setId(dataSnapshot.getKey());

                if (RutinaService.rutinaList.contains(rutina)) {
                    RutinaService.updateRutina(rutina);
                }

                rc.getAdapter().notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Rutina rutina = dataSnapshot.getValue(Rutina.class);
                rutina.setId(dataSnapshot.getKey());

                if (RutinaService.rutinaList.contains(rutina)) {
                    RutinaService.removeRutina(rutina);
                }

                rc.getAdapter().notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // --- ---

    public void esconderTeclado(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void clear() {
        txtnombre.setText("");
    }
}