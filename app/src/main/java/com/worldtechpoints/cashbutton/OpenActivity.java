package com.worldtechpoints.cashbutton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class OpenActivity extends AppCompatActivity {

    private int progress;
    private ProgressBar progressBar;
    private MediaPlayer player;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);


        if (haveNetwork()) {
            progressBar = findViewById(R.id.progressBar2);

            final Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {

                    sound();
                    progress = progress + 3;
                    progressBar.setProgress(progress);
                    progressBar.setMax(99);

                    if (progress == 99) {
                        timer.cancel();
                        stopPlayer();
                        startApp();
                    }

                }
            };
            timer.schedule(timerTask, 0, 99);


        } else {
            Toast.makeText(this, "Please Connect your Internet..", Toast.LENGTH_LONG).show();
        }


    }

    private void sound(){

        if (player == null){
            player = MediaPlayer.create(OpenActivity.this,R.raw.progressbarsoundeffect);
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

    private void startApp() {

        finish();
        startActivity(new Intent(OpenActivity.this,MainActivity.class));


    }


    private boolean haveNetwork ()
    {
        boolean have_WiFi = false;
        boolean have_Mobile = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

        for (NetworkInfo info : networkInfo){

            if (info.getTypeName().equalsIgnoreCase("WIFI"))
            {
                if (info.isConnected())
                {
                    have_WiFi = true;
                }
            }
            if (info.getTypeName().equalsIgnoreCase("MOBILE"))

            {
                if (info.isConnected())
                {
                    have_Mobile = true;
                }
            }

        }
        return have_WiFi || have_Mobile;

    }


}