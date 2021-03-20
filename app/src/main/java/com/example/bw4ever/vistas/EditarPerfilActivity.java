package com.example.bw4ever.vistas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bw4ever.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditarPerfilActivity extends AppCompatActivity {

    EditText txtnombre, txtcorreo, txtcontraseña, txtconfcontraseña;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        txtnombre=findViewById(R.id.txt_editNombre);
        txtcorreo=findViewById(R.id.txt_editMail);
        txtcontraseña=findViewById(R.id.txt_editPass);
        txtconfcontraseña=findViewById(R.id.txt_confirmPass);

        firebaseAuth = FirebaseAuth.getInstance();
        user=firebaseAuth.getInstance().getCurrentUser();
        txtnombre.setHint(user.getDisplayName());
        txtcorreo.setHint(user.getEmail());

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

    public void reauthenticate() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final EditText passwd= new EditText(EditarPerfilActivity.this);
        AlertDialog.Builder passwordDialog = new AlertDialog.Builder(EditarPerfilActivity.this);
        passwordDialog.setTitle("¿Confirmas que deseas actualizar tus datos?");
        passwordDialog.setMessage("Escribe tu contraseña actual");
        passwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordDialog.setView(passwd);
        passwordDialog.setPositiveButton("Si, confirmo.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String contraseña = passwd.getText().toString();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(txtcorreo.getHint().toString(), contraseña);

                // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(EditarPerfilActivity.this, "Contraseña verificada. Ahora puede cambiar sus datos." ,Toast.LENGTH_SHORT).show();
                                Log.d("TESTING", "User re-authenticated.");
                            }
                        });
                    }
                });
                passwordDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordDialog.create().show();
    }

    public void actualizarMail(View view) {
        final String nuevoCorreo = txtcorreo.getText().toString();
        if(checkEmail(nuevoCorreo)){
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updateEmail(nuevoCorreo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                actualizarMailFB(user.getUid(), nuevoCorreo);
                                Toast.makeText(EditarPerfilActivity.this, "Correo Actualizado!" ,Toast.LENGTH_SHORT).show();
                                Log.d("TESTING", "User email address updated.");
                            }
                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                reauthenticate();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(EditarPerfilActivity.this, "Debe ingresar un correo válido" ,Toast.LENGTH_LONG).show();
        }
    }

    public void resetPassword(View view) {
        final String nuevaContraseña = txtcontraseña.getText().toString();
        String confContraseña = txtconfcontraseña.getText().toString();
        if(nuevaContraseña.equals(confContraseña)){
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String newPassword = nuevaContraseña;

            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                actualizarPasswordFB(user.getUid(), nuevaContraseña);
                                Toast.makeText(EditarPerfilActivity.this, "Contraseña Actualizada!" ,Toast.LENGTH_LONG).show();
                                Log.d("TESTING", "User password updated.");
                            }
                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                reauthenticate();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(EditarPerfilActivity.this, "Las Contraseñas deben coincidir!" ,Toast.LENGTH_LONG).show();
        }
    }

    public void guardarCambios(View view) {
        final String nuevoNombre=txtnombre.getText().toString();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nuevoNombre)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            actualizarNombreFB(user.getUid(), nuevoNombre);
                            Toast.makeText(EditarPerfilActivity.this, "Nombre de usuario actualizado!" ,Toast.LENGTH_LONG).show();
                            Log.d("TESTING", "User profile updated.");
                        }
                    }
                });
    }

    public void actualizarMailFB(String userid, String correo){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Usuarios");
        DatabaseReference userRef = ref.child(userid);
        Map<String, Object> Updates = new HashMap<>();
        Updates.put("correo", correo);
        userRef.updateChildren(Updates);
    }

    public void actualizarPasswordFB(String userid, String password){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Usuarios");
        DatabaseReference userRef = ref.child(userid);
        Map<String, Object> Updates = new HashMap<>();
        Updates.put("password", password);
        userRef.updateChildren(Updates);
    }

    public void actualizarNombreFB(String userid, String nombre){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Usuarios");
        DatabaseReference userRef = ref.child(userid);
        Map<String, Object> Updates = new HashMap<>();
        Updates.put("nombre", nombre);
        userRef.updateChildren(Updates);
    }
}

