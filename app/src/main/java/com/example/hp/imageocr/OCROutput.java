package com.example.hp.imageocr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OCROutput extends AppCompatActivity {

    private TextView tw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocroutput);
        String output=getIntent().getStringExtra("Result");
        tw=(TextView)findViewById(R.id.textView);
        tw.setText(output);
    }
}
