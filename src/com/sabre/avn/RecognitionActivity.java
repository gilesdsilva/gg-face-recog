package com.sabre.avn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

public class RecognitionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition);
    }

    public void onClick(View view) {
        Intent intentMain = new Intent(RecognitionActivity.this , ScreenSlideActivity.class);
        RecognitionActivity.this.startActivity(intentMain);
    }

}
