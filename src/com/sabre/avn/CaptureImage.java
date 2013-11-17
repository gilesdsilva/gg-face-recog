package com.sabre.avn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CaptureImage extends Activity {
    final static String TAG = "MakePhotoActivity";
    private static File outputMediaFile;
    private Camera camera;
    private CameraPreview cameraPreview;
    private int cameraId = 0;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String MASHAPE_KEY = "LZwzdMocj8512ta4kZRsMs3GuVoNmFHT";
    private File fileToRecognize = null;
    private FrameLayout framePreview;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createCameraPreview();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    private void createCameraPreview() {
        PackageManager packageManager = getPackageManager();
        if (packageManager!=null && !packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front or back facing camera found.", Toast.LENGTH_LONG).show();
            } else {
                camera = Camera.open(cameraId);
            }
        }
        cameraPreview = new CameraPreview(this, camera);
//        getSupportedPreviewFramesPerSecond();
        framePreview = (FrameLayout) findViewById(R.id.camera_preview);
        framePreview.addView(cameraPreview);
    }

    private void getSupportedPreviewFramesPerSecond() {
        Camera.Parameters parameters = camera.getParameters();
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        StringBuilder stringBuilder = new StringBuilder();
        int maxFps = Integer.MIN_VALUE;
        int min = 0;
        int max = 0;
        if(supportedPreviewFpsRange!=null) {
            for (int[] fps : supportedPreviewFpsRange) {
                if(fps[0]*fps[1] > maxFps){
                    min = fps[0];
                    max = fps[1];
                }
            }
        }
        Toast.makeText(this, "min:" + min + " max:" + max, Toast.LENGTH_LONG).show();
    }

    public void onClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); // start the image capture Intent
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            onCameraClick(resultCode);
        }
        else if (resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "Image Cancelled", Toast.LENGTH_LONG).show();
            camera = Camera.open(cameraId);
            Camera.Parameters param;
            param = camera.getParameters();
            //modify parameter
            param.setPreviewFrameRate(50);
//                param.setPictureSize(320,240);
            camera.setParameters(param);
            cameraPreview = new CameraPreview(this, camera);
            framePreview = (FrameLayout) findViewById(R.id.camera_preview);
            framePreview.removeAllViews();
            framePreview.addView(cameraPreview);
        }
        else
        {
            Toast.makeText(this, "Failed" + resultCode, Toast.LENGTH_LONG).show();
        }
    }

    private void onCameraClick(int resultCode) {
        if (resultCode == RESULT_OK)
        {
            //todo:commented out for testing
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Toast.makeText(this, "Image accepted", Toast.LENGTH_LONG).show();
            String photoPath = null;
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, MediaStore.Images.Media.DATE_ADDED, null, "date_added ASC");
            if(cursor != null && cursor.moveToFirst())
            {
                Uri uri;
                do {
                    uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                    photoPath = uri.toString();
                }while(cursor.moveToNext());
                cursor.close();
                fileToRecognize = new File(uri.toString());
                //todo:commented out for testing
//                new HttpThread(this).execute("");
                Intent intentMain = new Intent(CaptureImage.this , RecognitionActivity.class);
                CaptureImage.this.startActivity(intentMain);
            }
        }
        else if (resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "Image Cancelled", Toast.LENGTH_LONG).show();
            camera = Camera.open(cameraId);
            Camera.Parameters param;
            param = camera.getParameters();
            //modify parameter
            param.setPreviewFrameRate(50);
//                param.setPictureSize(320,240);
            camera.setParameters(param);
            cameraPreview = new CameraPreview(this, camera);
            framePreview = (FrameLayout) findViewById(R.id.camera_preview);
            framePreview.removeAllViews();
            framePreview.addView(cameraPreview);
        }
        else
        {
            Toast.makeText(this, "No idea" + resultCode, Toast.LENGTH_LONG).show();
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT || info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private static Uri getOutputMediaFileUri(int type){
        outputMediaFile = getOutputMediaFile(type);
        return Uri.fromFile(outputMediaFile);
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    class HttpThread extends AsyncTask<String,String,String>
    {
        private final CaptureImage captureImage;

        HttpThread(CaptureImage captureImage) {
            this.captureImage = captureImage;
        }

        public String executeMultiPartRequest(String urlString, File fileToUse) {
            HttpPut httpPut = new HttpPut(urlString);
            String responseString = "";
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
                BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
                basicHttpEntity.setContent(new FileInputStream(fileToUse));
                basicHttpEntity.setContentLength(fileToUse.length());
                basicHttpEntity.setContentType("image/jpeg");
                httpPut.setEntity(basicHttpEntity);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CaptureImage.this, "Sending image for recognition", Toast.LENGTH_LONG).show();
                    }
                });
                HttpResponse response = httpClient.execute(httpPut);
                HttpEntity result = response.getEntity();

                if (result != null) {
                    InputStream inputStream = result.getContent();

                    byte[] buffer = new byte[1024];
                    while(inputStream.read(buffer) > 0)
                        responseString += new String(buffer);

                    result.consumeContent();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CaptureImage.this, "Image recognition completed ", Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (UnsupportedEncodingException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CaptureImage.this, "UnsupportedEncodingException", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CaptureImage.this, "IOException", Toast.LENGTH_LONG).show();
                    }
                });
            }
            if(responseString.equalsIgnoreCase("no match"))
                responseString = "No match found;     ; ; ";
            return responseString;
        }


        @Override
        protected String doInBackground(String... strings)  {

            return executeMultiPartRequest("http://sabrehackday-sscompetitions.rhcloud.com/upload", fileToRecognize);
        }

        @Override
        protected void onPostExecute(String responseFromImageRecog) {
            Intent intentMain = new Intent(CaptureImage.this , ScreenSlideActivity.class);
            intentMain.putExtra("info", responseFromImageRecog);
            CaptureImage.this.startActivity(intentMain);
        }
    }

}

