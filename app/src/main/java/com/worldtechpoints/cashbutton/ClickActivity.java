package com.worldtechpoints.cashbutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.adfendo.sdk.ads.AdFendo;
import com.adfendo.sdk.ads.AdFendoInterstitialAd;
import com.adfendo.sdk.interfaces.InterstitialAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClickActivity extends AppCompatActivity {

    private TextView showScore;
    private Button clickButton;

    private AdFendoInterstitialAd mAdFendoInterstitialAd;

    private String interstitialAd;
    private int adCount;


    private CountDownTimer countDownTimer;
    private long timeLeft = 60000;
    private boolean timeRunning;
    private String timeText;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String uId;
    int lastPoints;

    private MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);




        showScore = findViewById(R.id.clickScoreShow_id);
        clickButton = findViewById(R.id.clickButton_id);

        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            interstitialAd = bundle.getString("interstitialAd");

        }


        if (user != null){
            uId = user.getUid();
            balanceControl();
        }

        AdFendo.initialize(getString(R.string.mainAppUnitId));

        mAdFendoInterstitialAd = new AdFendoInterstitialAd(this, interstitialAd);



        clickButton.setVisibility(View.GONE);



        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mAdFendoInterstitialAd.isLoaded()){
                    stopPlayer();
                    mAdFendoInterstitialAd.showAd();
                }else {

                    Toast.makeText(ClickActivity.this, " Try Again.. Ok! ", Toast.LENGTH_SHORT).show();

                }


            }
        });


        mAdFendoInterstitialAd.setInterstitialAdListener(new InterstitialAdListener() {
            @Override
            public void onClosed() {

                if (adCount >=1){

                    stopPlayer();
                    gameOver();

                }else {
                    mAdFendoInterstitialAd.requestAd();
                    Toast.makeText(ClickActivity.this, " Try Again.. Ok! ", Toast.LENGTH_SHORT).show();

                }
                // Code to be executed when an ad closed.
            }
            @Override
            public void onFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }
            @Override
            public void isLoaded(boolean isLoaded) {

                clickButton.setVisibility(View.VISIBLE);
                if (player == null){
                    player = MediaPlayer.create(ClickActivity.this,R.raw.click);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            stopPlayer();
                        }
                    });
                }
                player.start();



                // Code to be executed when an ad finishes loading.
            }
            @Override
            public void onImpression() {
                startTimer();
                // Code to be executed when the ad is shown.
            }
        });

        mAdFendoInterstitialAd.requestAd();




    }

    private void stopPlayer() {
        if (player != null){
            player.release();
            player=null;
        }
    }

    private void gameOver(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ClickActivity.this);

        builder.setMessage("Congratulation..!"+
                "\n\n"+" You got 20 points " +
                "\n")
                .setCancelable(false)
                .setPositiveButton(" Ok ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int addPoints = lastPoints+20;
                        String finalPoints = String.valueOf(addPoints);

                        myRef.child(uId).child("MainPoints").setValue(finalPoints).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    ControlClass controlClass = new ControlClass(ClickActivity.this);
                                    controlClass.Delete();
                                    startActivity(new Intent(ClickActivity.this,MainActivity.class));
                                    finish();

                                }else {

                                    Toast.makeText(ClickActivity.this, "Internet problem ", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });




                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


    }



    private void balanceControl() {


        myRef.child(uId).child("MainPoints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String value = dataSnapshot.getValue(String.class);
                    lastPoints = Integer.parseInt(value);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void startTimer() {
        if (timeRunning){
            stopTime();
        }else {
            startTime();
        }

    }


    private void startTime() {
        countDownTimer = new CountDownTimer(timeLeft,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft =millisUntilFinished;
                updateTimer();

            }

            @Override
            public void onFinish() {
                adCount++;
                if (player == null){
                    player = MediaPlayer.create(ClickActivity.this,R.raw.success);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            stopPlayer();
                        }
                    });
                }
                player.start();



            }
        }.start();
        timeRunning = true;
        //startBtn.setText("Pause");

    }

    private void updateTimer() {

        int minutes = (int) (timeLeft /60000);
        int seconds = (int) (timeLeft % 60000 /1000);
        timeText = ""+minutes;
        timeText += ":";
        if (seconds <10)timeText += "0";
        timeText +=seconds;

    }

    private void stopTime() {
        countDownTimer.cancel();
        timeRunning = false;
        // startBtn.setText("Start");



    }




}
