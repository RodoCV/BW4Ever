package com.example.bw4ever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText txtcorreo, txtpassword;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtcorreo = findViewById(R.id.txt_loginMail);
        txtpassword = findViewById(R.id.txt_loginPassword);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if (firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, PrincipalActivity.class));
            finish();
        }
    }

    public void iniciarSesion(View view) {
        final String mail = txtcorreo.getText().toString();
        final String pass = txtpassword.getText().toString();

        if (mail.isEmpty() || pass.isEmpty()){
            Toast.makeText(this,"Verifique Datos Ingresados", Toast.LENGTH_SHORT).show();
        }
        else{
            progressDialog.setMessage("Conectando con la Base de datos");
            progressDialog.show();
            firebaseAuth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                actualizarPasswordFB(user.getUid(), pass);
                                startActivity(new Intent(LoginActivity.this, PrincipalActivity.class));
                            }else{
                                Toast.makeText(LoginActivity.this, "El correo o la contraseña ingresada son incorrectos!", Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    public void cargarRegistro(View view) {
        startActivity(new Intent(this, RegistroActivity.class));
    }

    public void resetPassword(View view) {
        final EditText resetMail = new EditText(view.getContext());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
        passwordResetDialog.setTitle("¿Deseas restablecer la contraseña?");
        passwordResetDialog.setMessage("Ingresa tu correo para recibir las instrucciones para restablecerla.");
        passwordResetDialog.setView(resetMail);
        passwordResetDialog.setPositiveButton("Sí, Confirmo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String correo = resetMail.getText().toString();
                firebaseAuth.sendPasswordResetEmail(correo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LoginActivity.this, "El correo para restablecer la contraseña ha sido enviado.", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Error. El correo no ha sido enviado." + e.getMessage() ,Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        passwordResetDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        passwordResetDialog.create().show();
    }

    public void actualizarPasswordFB(String userid, String password){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Usuarios");
        DatabaseReference userRef = ref.child(userid);
        Map<String, Object> Updates = new HashMap<>();
        Updates.put("password", password);
        userRef.updateChildren(Updates);
    }
}
