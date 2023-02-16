package com.example.musicplayer2;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class playinterface extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_main);
    }

}

