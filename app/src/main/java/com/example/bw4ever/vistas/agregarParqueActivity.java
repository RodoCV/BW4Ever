package com.example.bw4ever.vistas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bw4ever.JavaMailAPI;
import com.example.bw4ever.PrincipalActivity;
import com.example.bw4ever.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class agregarParqueActivity extends AppCompatActivity {
    EditText txtnombre;
    ImageView foto;
    Button btngaleria;
    TextView direccion;
    Uri uri_foto;
    Double longitud, latitud; //Estas son las variables que guardan las coordenadas para luego mandarlas a la base de datos.
    String URLfoto="null";
    private SharedPreferences fotoSharedPreference; //Guarda la foto, para sacarla del proceso de Firebase.

    FirebaseAuth firebaseAuth;

    public LocationManager locationManager;
    public LocationListener locationListener = new myLocationListener();

    private boolean gpsHabilitado = false;
    private boolean redHabilitada = false;
    Geocoder geocoder;
    List<Address> miDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_parque);

        txtnombre = findViewById(R.id.txtnombreparque);
        foto = findViewById(R.id.foto_parque);
        btngaleria = findViewById(R.id.bt_galeria);
        direccion = findViewById(R.id.tv_direccion);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        obtenerUbicacion(); //En esta función se obtienen las coordenadas y la dirección.

        // --- Trabajando con SharedPreferences, para extraer valores de Métodos Firebase ---
        fotoSharedPreference = getSharedPreferences("Parque Sugerido", Context.MODE_PRIVATE); //Nombre de "BD" y el modo de trabajo.
        SharedPreferences.Editor editor = fotoSharedPreference.edit(); //Mediante este objeto, se edita el SharedPreferences.
        editor.clear(); //Limpia el SharedPreferences.
        editor.commit();
        // --- ---


        btngaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //Intent que permite trabajar con elementos externos en base a URI.
                startActivityForResult(intent, PrincipalActivity.CODE_GALLERY); //Desde Principal, obtengo el código que determina usar Galería.
            }
        });

    }

    public void registrarParque(View view) throws Exception {
        final String nombre = txtnombre.getText().toString();
        final String foto = fotoSharedPreference.getString("Uri", "NULL"); // Busca el valor según la key, sino encuentra nada, setea el valor "NULL".
        if(nombre.isEmpty()){
            Toast.makeText(this, "Debe ingresar un Nombre!", Toast.LENGTH_SHORT).show();
        }else{
            if(foto.equalsIgnoreCase("NULL")){ //Aqui deberia verificarse si se cargó una imagen
                Toast.makeText(this, "Debe cargar una fotografía!", Toast.LENGTH_SHORT).show();
            }
            else{
                URLfoto=foto;
                sendMail(); //Acá se manda el correo, despues de esto deberia cerrar y cambiar a la vista de mapas nuevamente.
            }
        }
    }

    // --- Método que recibirá al cargar una foto ---
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // --- Parámetros de Conexión al Storage ---
        StorageReference storage = FirebaseStorage.getInstance().getReference(); //Inicializa el Storage de Firebase.
        StorageReference folder = storage.child("IMG_ParquesSugeridos"); //Crea carpeta donde se guardará la imagen. NO SE CREARÁ EN ESTE ACTIVITY.
        StorageReference photo = folder.child(txtnombre.getText().toString()+"_"+new Date().toString().trim()); //Crea el nombre de la imagen. NO SE GUARDARÁ EN ESTE ACTIVITY.
        // --- ---

        switch (requestCode) {
            // --- Caso Usuario accede a la Galería ---
            case PrincipalActivity.CODE_GALLERY:
                if (data != null) {
                    uri_foto = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_foto);
                        foto.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // --- Proceso de Rescatar la URI de la imagen, ACÁ SE PUEDE GUARDAR LA FOTO EN FIREBASE STORAGE ---
                    photo.putFile(uri_foto).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadUri = uriTask.getResult();

                            SharedPreferences.Editor editor = fotoSharedPreference.edit(); //Mediante este objeto, se edita el SharedPreferences.
                            editor.putString("Uri", downloadUri.toString()); //Guarda la Uri de la imagen en una "variable Uri", en la "BD".
                            editor.commit();
                            // --- ---
                        }
                    });
                    // --- Fin Proceso de Rescatar la URI de la imagen ---
                }
                break;
            // --- Fin Caso Usuario accede a la Galería ---
        }
    }
    // --- ---

    class myLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                locationManager.removeUpdates(locationListener);
                latitud = location.getLatitude();
                longitud = location.getLongitude();

                geocoder = new Geocoder(agregarParqueActivity.this, Locale.getDefault());
                try {
                    miDireccion = geocoder.getFromLocation(latitud, longitud,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address= miDireccion.get(0).getAddressLine(0);
                direccion.setText(address);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public void obtenerUbicacion() {
        try {
            gpsHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            redHabilitada = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {

        }

        if (!gpsHabilitado && !redHabilitada) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(agregarParqueActivity.this);
            builder.setTitle("Aviso");
            builder.setMessage("Ubicación no disponible. Por favor activa el servicio de ubicación.");
            builder.create().show();
        }
        if (gpsHabilitado) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

        if(redHabilitada){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

    }

    private void sendMail(){
        firebaseAuth = FirebaseAuth.getInstance();
        String correousuario= firebaseAuth.getCurrentUser().getEmail();
        String mail = "bw4ever.parks@gmail.com";
        String mensaje = "Sugerido por: "+correousuario + "\n\n\n"+
                "\"CREARIDNUEVO\" : {\n\t" +
                "\"latitud\" : " +latitud+",\n\t" +
                "\"longitud\" : " +longitud+",\n\t"+
                "\"direccion\" : " +"\""+direccion.getText().toString()+"\""+",\n\t" +
                "\"nombre\" : " +"\""+txtnombre.getText().toString()+"\""+",\n\t" +
                "\"url_foto\" : " +"\""+URLfoto+"\""+"\n}";

        String asunto = "Parque Sugerido por "+ correousuario;

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail, asunto, mensaje);
        javaMailAPI.execute();
    }
}
