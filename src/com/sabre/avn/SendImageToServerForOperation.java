package com.sabre.avn;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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


public class SendImageToServerForOperation extends AsyncTask<String,String,String>
{
    private final File fileToRecognize;
    private final Context context;
    private final CaptureImage captureImage;

    SendImageToServerForOperation(CaptureImage captureImage, Context context, File fileToRecognize, int recognize) {
        this.context = context;
        this.fileToRecognize = fileToRecognize;
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

            captureImage.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Sending image for recognition", Toast.LENGTH_LONG).show();
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
            captureImage.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Image recognition completed ", Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (UnsupportedEncodingException e) {
            captureImage.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "UnsupportedEncodingException", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            captureImage.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "IOException", Toast.LENGTH_LONG).show();
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
        Intent intentMain = new Intent(context , ScreenSlideActivity.class);
        intentMain.putExtra("info", responseFromImageRecog);
        context.startActivity(intentMain);
    }
}

