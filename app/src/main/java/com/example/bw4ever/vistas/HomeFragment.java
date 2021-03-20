package com.example.bw4ever.vistas;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bw4ever.R;
import com.example.bw4ever.adapter.ParqueAdapter;
import com.example.bw4ever.modelo.Parque;
import com.example.bw4ever.modelo.ParqueService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    RecyclerView rc;
    EditText txtnombre, txtdireccion, txtbuscar;
    ImageView foto;
    ImageButton buscar, limpiar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rc = view.findViewById(R.id.rcparques);
        txtnombre = view.findViewById(R.id.item_nombreparque);
        txtdireccion = view.findViewById(R.id.item_direccion);
        txtbuscar = view.findViewById(R.id.txt_buscarNombre);
        foto = view.findViewById(R.id.foto_parque);
        buscar = view.findViewById(R.id.bt_buscar);
        limpiar = view.findViewById(R.id.bt_limpiar_parques);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(RecyclerView.VERTICAL);
        rc.setLayoutManager(lm);

        ParqueAdapter adapter = new ParqueAdapter(ParqueService.parqueList, R.layout.parque, getActivity());
        rc.setAdapter(adapter);

        //cargaParquesFirebase();

        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esconderTeclado(getContext(), getView());

                if (txtbuscar.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Debe Ingresar un nombre para buscar", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Parques");
                    Query query = reference.orderByChild("nombre").equalTo(txtbuscar.getText().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Parque p = ds.getValue(Parque.class);
                                p.setId(ds.getKey());
                                if (!ParqueService.parqueList.contains(p)) {
                                    ParqueService.addParque(p);
                                    rc.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getContext(), "Búsqueda terminada", Toast.LENGTH_SHORT).show();
                    clear();
                }
            }
        });

        limpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParqueService.parqueList.clear();
                rc.getAdapter().notifyDataSetChanged();
                //Toast.makeText(getContext(), "¡ Lista vaciada !", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void esconderTeclado(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void clear() {
        txtbuscar.setText("");
    }

    public void cargaParquesFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Parques");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Parque parque = dataSnapshot.getValue(Parque.class);
                parque.setId(dataSnapshot.getKey());

                if (!ParqueService.parqueList.contains(parque)) {
                    ParqueService.addParque(parque);
                }

                rc.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Parque parque = dataSnapshot.getValue(Parque.class);
                parque.setId(dataSnapshot.getKey());

                if (ParqueService.parqueList.contains(parque)) {
                    ParqueService.updateParque(parque);
                }

                rc.getAdapter().notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Parque parque = dataSnapshot.getValue(Parque.class);
                parque.setId(dataSnapshot.getKey());

                if (ParqueService.parqueList.contains(parque)) {
                    ParqueService.removeParque(parque);
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
}