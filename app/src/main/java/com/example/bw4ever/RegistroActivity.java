package com.example.bw4ever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bw4ever.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    EditText txtNombre, txtCorreo, txtPassword, txtConf;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser usuario;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        txtNombre = findViewById(R.id.txt_registroNombre);
        txtCorreo = findViewById(R.id.txt_registroMail);
        txtPassword = findViewById(R.id.txt_registroPassword);
        txtConf = findViewById(R.id.txt_registroConfirmarPass);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    public boolean checkEmail (String _correo){
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(_correo);
        if(mat.matches()){
            return true;
        }
        else{
            return false;
        }
    }

    public void registrarUsuario(View view) {
        esconderTeclado(this, view);

        final String nombre = txtNombre.getText().toString();
        final String correo = txtCorreo.getText().toString();
        final String password = txtPassword.getText().toString();
        final String confPass = txtConf.getText().toString();

        if(!checkEmail(correo)){
            Toast.makeText(this, "¡ Debe ingresar un correo válido !", Toast.LENGTH_SHORT).show();
        }
        else {
            if (!password.equals(confPass)) {
                Toast.makeText(this, "¡ Las contraseñas deben coincidir !", Toast.LENGTH_SHORT).show();
            } else {
                if (correo.isEmpty() || nombre.isEmpty() || password.isEmpty() || password.length() < 6) {
                    Toast.makeText(this, "¡ Verifique datos ingresados !", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setMessage("Conectando con la Base de Datos ...");
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(correo, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    boolean registroCorrecto = false;
                                    if (task.isSuccessful()) {
                                        usuario=firebaseAuth.getInstance().getCurrentUser();
                                        String id =usuario.getUid();

                                        setDisplayName();
                                        guardarUsuario(id, nombre, correo, password);

                                        Toast.makeText(RegistroActivity.this, "¡ Te has registrado con éxito !", Toast.LENGTH_SHORT).show();
                                        registroCorrecto = true;
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(RegistroActivity.this, "Este correo ya se encuentra registrado!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(RegistroActivity.this, "Ocurrió un error!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    progressDialog.dismiss();

                                    // --- Si el Usuario se registró, se devolverá a la Pantalla de Login ---
                                    if (registroCorrecto == true){
                                        onBackPressed();
                                        finish();
                                    }
                                    // --- ---
                                }
                            });
                }
            }
        }
    }

    public void setDisplayName(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(txtNombre.getText().toString())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TESTING", "User profile updated.");
                            }
                        }
                    });
        }
    }

    private void guardarUsuario(String userId, String name, String email, String password) {
        Usuario user = new Usuario();
        user.setId(userId);
        user.setNombre(name);
        user.setCorreo(email);
        user.setPassword(password);
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Usuarios").child(userId).setValue(user);
    }

    public void esconderTeclado(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
