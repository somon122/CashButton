package com.worldtechpoints.cashbutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference myRef;


    Button signUpButton,logInButton;
    EditText emailEditText,passwordEditText,confirmPasswordET,userNameET,phoneNumberET,countryNameET;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("CasButtonUsers");

        signUpButton = findViewById(R.id.signUpButton_id);
        logInButton = findViewById(R.id.logInButton_id);

        emailEditText = findViewById(R.id.signUpUsernameId);
        passwordEditText = findViewById(R.id.signUpPasswordId);
        confirmPasswordET = findViewById(R.id.confirmPasswordId);

        userNameET = findViewById(R.id.userName_id);
        phoneNumberET = findViewById(R.id.phoneNumber_id);
        countryNameET = findViewById(R.id.countryName_id);


        dialog = new ProgressDialog(this);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRegister();


            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SignUpActivity.this,LogInActivity.class));
                finish();
                Toast.makeText(SignUpActivity.this, "call right", Toast.LENGTH_SHORT).show();

            }
        });



    }



    private void isRegister()
    {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();

        final String userName = userNameET.getText().toString();
        final String phoneNumber = phoneNumberET.getText().toString();
        final String countryName = countryNameET.getText().toString();

        if (TextUtils.isEmpty(email)){
            emailEditText.setError("Please Enter Valid Email Address");
        }
        else if (TextUtils.isEmpty(password)){
            passwordEditText.setError("Please Enter Valid Password");
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            confirmPasswordET.setError("Please enter matching Password by above");
        }
        else if (TextUtils.isEmpty(userName)){
            confirmPasswordET.setError("Please enter Name");
        }
        else if (TextUtils.isEmpty(phoneNumber)){
            confirmPasswordET.setError("Please enter valid Phone number");
        }
        else if (TextUtils.isEmpty(countryName)){
            confirmPasswordET.setError("Please enter country name");
        }
        else {

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser == null){

                if (!password.equals(confirmPassword))
                {
                    Toast.makeText(SignUpActivity.this, "Confirm Password could not match ", Toast.LENGTH_SHORT).show();

                }else {
                    dialog.show();
                    dialog.setMessage("Register is progressing ...");

                    auth.createUserWithEmailAndPassword(email,confirmPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        String uId = firebaseUser.getUid();

                                        UserInfo userInfo = new UserInfo(uId,userName,phoneNumber,countryName);

                                        myRef.child(uId).child("UserInfo").setValue(userInfo)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    
                                                    sentEmailVerification();
                                                    
                                                }else {
                                                    dialog.dismiss();
                                                    Toast.makeText(SignUpActivity.this, "Internet is slow..", Toast.LENGTH_SHORT).show();
                                                }


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });


                                    }else {
                                        dialog.dismiss();
                                        Toast.makeText(SignUpActivity.this, "Email and Password could not Valid", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }else {
                dialog.dismiss();
                Toast.makeText(SignUpActivity.this, "You have already Registered", Toast.LENGTH_SHORT).show();
            }



        }



    }

    private void sentEmailVerification(){

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){

            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "SignUp is Successfully", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        Intent intent = new Intent(SignUpActivity.this,LogInActivity.class);
                        intent.putExtra("alert","alert");
                        startActivity(intent);
                        finish();

                    }else {
                        dialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "hello", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Connection is problem", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


}