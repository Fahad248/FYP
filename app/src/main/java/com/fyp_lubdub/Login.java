package com.fyp_lubdub;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private EditText mail, pass;
    private Button login,signUP;
    private FirebaseAuth auth;
    private Dialog dialog;
    private boolean show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        mail = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signUP = findViewById(R.id.signUp);

        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Mail = mail.getText().toString(), Pass = pass.getText().toString();
                if (Mail=="" || Pass==""){
                    Toast.makeText(Login.this, "Empty Fields!!!", Toast.LENGTH_LONG).show();
                }else if(!Mail.contains("@") || !Mail.contains(".com")){
                    Toast.makeText(Login.this, "Invalid Email!!!", Toast.LENGTH_LONG).show();
                }else if(Pass.length() < 6){
                    Toast.makeText(Login.this, "Password too short!!!", Toast.LENGTH_LONG).show();
                }else{
                    auth.signInWithEmailAndPassword(Mail,Pass)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Intent i = new Intent(Login.this,Profile.class);
                                        startActivity(i);
                                        finish();
                                    }else{
                                        Toast.makeText(Login.this, "Authentication Error!!!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
            }
        });

        signUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(Login.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.signup);
                final EditText Ename,Email,Epass,ECpass;
                Button Proceed;

                Ename = dialog.findViewById(R.id.Name); Email = dialog.findViewById(R.id.EMail);
                Epass = dialog.findViewById(R.id.Pass); ECpass = dialog.findViewById(R.id.CPass);
                Proceed = dialog.findViewById(R.id.proceed);



                Proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String name,mail,pass,Cpass;
                        name = Ename.getText().toString(); mail = Email.getText().toString();
                        pass = Epass.getText().toString(); Cpass = ECpass.getText().toString();
                        if (pass.equals(Cpass)){
                            auth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Account Created!!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();

                                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                                     //   db.child(auth.getUid()).push().setValue("Profile");
                                        db.child(auth.getUid()+"/Profile/").setValue(new Credentials(name));
                                        db.child(auth.getUid()+"/Signals/").setValue(new Credentials(name));
                                    }
                                    else {

                                        task.addOnFailureListener(Login.this, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            });
                        }else{
                            Toast.makeText(Login.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
                show = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(show)
            dialog.dismiss();
    }
}

class Credentials{
    String Name;
    Credentials(String name){
        this.Name = name;
    }
}