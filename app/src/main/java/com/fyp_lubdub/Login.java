package com.fyp_lubdub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText mail, pass;
    private Button login;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        mail = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);

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
                                        Intent i = new Intent(Login.this,MainActivity.class);
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
    }
}
