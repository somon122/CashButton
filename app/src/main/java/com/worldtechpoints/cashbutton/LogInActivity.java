package com.worldtechpoints.cashbutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {

    Button signUpButton, logInButton;
    EditText emailEditText, passwordEditText;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog dialog;

    int blockUservalue;
    FirebaseDatabase database;
    DatabaseReference myRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        signUpButton = findViewById(R.id.gotoSignUpPageId);
        logInButton = findViewById(R.id.logInId);
        emailEditText = findViewById(R.id.logInEmailId);
        passwordEditText = findViewById(R.id.logInPasswordId);


        Bundle bundle = getIntent().getExtras();

        if (bundle != null){

            emailVerificationAlert();

        }

        dialog = new ProgressDialog(this);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLogIn();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LogInActivity.this,SignUpActivity.class));
            }
        });
        

    }


    private void emailVerificationAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);

        builder.setTitle("Email Verification Alert")
                .setMessage("Please check your email and confirm email for access this app ")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      dialog.dismiss();

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }


    private void isLogIn() {

        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Please Enter Valid Email Address");
        } else if (password.isEmpty()) {
            passwordEditText.setError("Please Enter Valid Password");
        } else {

            dialog.show();
            dialog.setMessage("LogIn is progressing ...");

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                               /* String uId = firebaseUser.getUid();
                                blockUser(uId);*/

                                blockUserValue();


                            } else {
                                dialog.dismiss();
                                Toast.makeText(LogInActivity.this, "Email and Password is not match", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LogInActivity.this, "Something missing", Toast.LENGTH_SHORT).show();
                }
            });

        }


    }

    private void checkEmailVerification(int value){

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Boolean isVerified = firebaseUser.isEmailVerified();


        if (isVerified){

            if (value >= 1){
                Toast.makeText(this, "Your account is block for Invalid Click", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(LogInActivity.this, LogInActivity.class));

            }else {
                dialog.dismiss();
                Toast.makeText(LogInActivity.this, "LogIn is successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LogInActivity.this, MainActivity.class));
                finish();
            }



        }else {
            Toast.makeText(this, "Please verify your email first ", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(LogInActivity.this, LogInActivity.class));
        }

    }

    private void blockUser(String uId){

        String value = String.valueOf(2);

        myRef.child("blockCounter").child(uId).setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                if (task.isSuccessful()){

                    Toast.makeText(LogInActivity.this, "value add successfully", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

 private void blockUserValue( ){

     FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
     String uId = firebaseUser.getUid();

     myRef.child("blockCounter").child(uId).addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

             if (dataSnapshot.exists()){

                  String value = dataSnapshot.getValue(String.class);
                  int blockUservalue = Integer.parseInt(value);

                  checkEmailVerification(blockUservalue);

             }else {
                 checkEmailVerification(0);
             }


         }



         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
