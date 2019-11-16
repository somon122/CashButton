package com.worldtechpoints.cashbutton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adfendo.sdk.ads.AdFendo;
import com.adfendo.sdk.ads.AdFendoInterstitialAd;
import com.adfendo.sdk.ads.BannerAd;
import com.adfendo.sdk.interfaces.BannerAdListener;
import com.adfendo.sdk.interfaces.InterstitialAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String uId;


    private AdFendoInterstitialAd mAdFendoInterstitialAd;
    private BannerAd bannerAd;





    String interstitialAd = "ad-unit-814771104~689079288";

    ImageButton imageButton;
    Button homeButton,reademButton,walledButton,helpButton;
    TextView mainPointTV,showCountTV,istructionTv;
    private int showScore;
    ProgressBar progressBar;
    private ControlClass controlClass;
    private MediaPlayer player;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar_id);
        setSupportActionBar(toolbar);


        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");




        // Sample App ID : "test-app-146514415"
        AdFendo.initialize(getString(R.string.mainAppUnitId));

        // Interstitial sample ad unit id: "test-ad-unit-id-146514415~9142051414"
        mAdFendoInterstitialAd = new AdFendoInterstitialAd(this, interstitialAd);


        //Sample AdFendo AdUnitID: test-ad-unit-id-146514415~9142051415
        bannerAd = findViewById(R.id.bannerAd);
        bannerAd = new BannerAd(this,getString(R.string.mainBanner_ad_unit_id) );

        controlClass = new ControlClass(this);

        imageButton = findViewById(R.id.imageButton_id);

        homeButton = findViewById(R.id.homeButton_id);
        helpButton = findViewById(R.id.helpButton_id);
        walledButton = findViewById(R.id.walledButton_id);
        reademButton = findViewById(R.id.reedamButton_id);

        mainPointTV = findViewById(R.id.mainPoints_id);
        showCountTV = findViewById(R.id.showCount_id);

        istructionTv = findViewById(R.id.instruction_id);
        progressBar = findViewById(R.id.progressBar_id);

        progressBar.setVisibility(View.GONE);
        istructionTv.setVisibility(View.GONE);


        if (user != null){
            uId = user.getUid();
            balanceControl();
        }


        helpButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        walledButton.setOnClickListener(this);
        reademButton.setOnClickListener(this);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sound();
                imageButton.setVisibility(View.GONE);
                mAdFendoInterstitialAd.requestAd();
                progressBar.setVisibility(View.VISIBLE);
                istructionTv.setVisibility(View.VISIBLE);


            }
        });



        mAdFendoInterstitialAd.setInterstitialAdListener(new InterstitialAdListener() {
            @Override
            public void onClosed() {

                gameOver();
                // Code to be executed when an ad closed.
            }
            @Override
            public void onFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }
            @Override
            public void isLoaded(boolean isLoaded) {

                if (mAdFendoInterstitialAd.isLoaded()){
                    stopPlayer();
                    progressBar.setVisibility(View.GONE);
                    istructionTv.setVisibility(View.GONE);
                    mAdFendoInterstitialAd.showAd();
                }
                // Code to be executed when an ad finishes loading.
            }
            @Override
            public void onImpression() {
                // Code to be executed when the ad is shown.
            }
        });


        bannerAd.setOnBannerAdListener(new BannerAdListener() {
            @Override
            public void onRequest(boolean isSuccessful) {
                // Code to be executed when an ad is requested.
            }
            @Override
            public void onClosed() {
                // Code to be executed when an ad closed.
            }
            @Override
            public void onFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }
            @Override
            public void isLoaded(boolean isLoaded) {
                // Code to be executed when an ad finishes loading.
            }
            @Override
            public void onImpression() {

            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view.getId()==R.id.homeButton_id){

            stopPlayer();
            imageButton.setVisibility(View.VISIBLE);

        }
        if (view.getId()==R.id.helpButton_id){
            startActivity(new Intent(MainActivity.this,HelpActivity.class));


        }
        if (view.getId()==R.id.reedamButton_id){

            startActivity(new Intent(MainActivity.this,WithdrawActivity.class));


        }
        if (view.getId()==R.id.walledButton_id){

            startActivity(new Intent(MainActivity.this,WithdrawActivity.class));

        }


    }

    private void sound(){

        if (player == null){
            player = MediaPlayer.create(MainActivity.this,R.raw.tapsound);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayer();
                }
            });
        }
        player.start();
    }

    private void stopPlayer() {
        if (player != null){
            player.release();
            player=null;
        }
    }


    private void balanceControl() {

        myRef.child(uId).child("MainPoints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String value = dataSnapshot.getValue(String.class);
                    mainPointTV.setText("Your Points : "+value);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    @Override
    protected void onStart() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(MainActivity.this,LogInActivity.class));

        }else {
            blockConfirm();
        }

        super.onStart();


    }



    private void blockConfirm (){

        myRef.child("blockCounter").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String value = dataSnapshot.getValue(String.class);
                    int blockUservalue = Integer.parseInt(value);

                    checkUserBlock(blockUservalue);

                    }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void checkUserBlock(int blockUservalue) {

        if (blockUservalue >=1){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,LogInActivity.class));
            Toast.makeText(MainActivity.this, "block is still", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {
        appFinishedAlert();
    }


    private void appFinishedAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Alert..!")
                .setMessage("Are you sure ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finishAffinity();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void gameOver(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setMessage("Congratulation..!"+
                "\n\n"+" Click Ok For Continue Game ..." +
                "\n")
                .setCancelable(false)
                .setPositiveButton(" Ok ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (showScore >= 25){

                            Intent intent = new Intent(MainActivity.this,ClickActivity.class);
                            intent.putExtra("interstitialAd",interstitialAd);
                            startActivity(intent);
                            finish();

                        }else {

                            showScore = showScore+1;
                            showCountTV.setText(""+showScore);
                           dialog.dismiss();
                           imageButton.setVisibility(View.VISIBLE);



                        }

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id==R.id.favourite_id){

            Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show();

        }

        if (id ==R.id.logOut_id){
           alert();

        }

        if (id==R.id.share_id){
            shareApp();

        }


        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareBody = "App link : https://youtu.be/eGn-2tGoG6s";
        String shareSub = "Make Money by Android App";
        intent.putExtra(Intent.EXTRA_SUBJECT,shareSub);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(intent,"Earning App"));

    }


    private void alert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you Sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainActivity.this, "Successfully LogOut ", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),LogInActivity.class));
                        finish();



                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
                Toast.makeText(MainActivity.this, "Thank You for Staying...", Toast.LENGTH_SHORT).show();



            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

}
