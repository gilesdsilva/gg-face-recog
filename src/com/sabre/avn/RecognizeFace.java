package com.sabre.avn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecognizeFace extends Activity {

    private static final int SPEECH_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition);
    }

    public void onClick(View view) {
//        Intent intentMain = new Intent(RecognizeFace.this , ScreenSlideActivity.class);
//        RecognizeFace.this.startActivity(intentMain);
        startVoiceRecognitionActivity();
    }

    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Who is this?");
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            StringBuilder fullName = null;
            if (data!=null) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                fullName = new StringBuilder();
                for (String match : matches) {
                    fullName.append(match);
                }
            }
            ((TextView) findViewById(R.id.recognitionLabel)).setText(fullName.toString());
        }
    super .onActivityResult(requestCode, resultCode, data);
    }
}
