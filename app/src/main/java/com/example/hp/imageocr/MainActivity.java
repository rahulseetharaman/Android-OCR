package com.example.hp.imageocr;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    Button capturePhoto;
    Button readText;
    ImageView imageView;
    private final int requestCode=20;
    private Uri imageUri;
    String datapath="";
    String OCRresult;
    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    public String Med="Crocin";
    private static final int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
               Log.d("Permissions","All Set");
            } else {
                requestPermission(); // Code for permission
            }
        }

        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available.");
        }

        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        capturePhoto=(Button)findViewById(R.id.capturePhoto);
        imageView=(ImageView)findViewById(R.id.imageView);
        readText=(Button)findViewById(R.id.readText);
        readText.setVisibility(View.INVISIBLE);

        Log.d("Filedir",getFilesDir().getAbsolutePath().toString());
        datapath=getFilesDir().toString();
        // datapath="/storage/emulated/0/";
        //String lang="eng";

        capturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File ocrDirectory= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/Camera");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                String currentTimeStamp = dateFormat.format(new Date());
                File photo=new File(ocrDirectory,currentTimeStamp);
                photoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photo));
                imageUri=Uri.fromFile(photo);
                startActivityForResult(photoCaptureIntent, requestCode);
            }
        });

        readText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap imageBitmap=null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Frame imageFrame = new Frame.Builder()
                        .setBitmap(imageBitmap)
                        .build();
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                String answer="";
                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

                    Log.i("The data is ", textBlock.getValue());
                    // Do something with value
                    answer=answer+textBlock.getValue();
                }
                Intent i=new Intent(MainActivity.this,OCROutput.class);
                i.putExtra("Result",answer);
                Log.d("The answer is ",answer);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this.requestCode == requestCode && resultCode == RESULT_OK)
        {
            try{

                imageView.setImageURI(imageUri);
            }
            catch(Exception ex)
            {
                Log.d("Exception Occurred",ex.toString());
            }
            readText.setVisibility(View.VISIBLE);

        }
    }

    public void processImage()
    {

        Bitmap image= null;
        try {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("processImage()","Exception occurred");
        }
        if(OCRresult==null)
        {
            if(image==null)
                Log.d("Image ","is null");
            Log.d("Image",image.toString());
            Log.d("String"," is null");
        }
        Log.d("Result",OCRresult);

    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }


}
