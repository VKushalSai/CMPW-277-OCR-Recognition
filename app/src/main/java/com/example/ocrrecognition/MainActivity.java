package com.example.ocrrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private TextView textView;
    private Button selectButton;
    private Button analyzeButton;
    private Bitmap selectedImageBitmap;
    private String subscriptionKey = System.getenv("COMPUTER_VISION_SUBSCRIPTION_KEY");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        selectButton = findViewById(R.id.button);
        analyzeButton = findViewById(R.id.button2);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageBitmap != null) {
                    analyzeImage(selectedImageBitmap);
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(selectedImageBitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void analyzeImage(Bitmap imageBitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = callComputerVisionAPI(imageBitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(result);
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String callComputerVisionAPI(Bitmap imageBitmap) throws IOException, JSONException {
        String endpoint = "https://ocr-cmpe-277.cognitiveservices.azure.com/computervision/imageanalysis:analyze?api-version=2023-02-01-preview&features=caption%2Cread&language=en&gender-neutral-caption=False";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", "13972fc377b9447e90a503fe1f71ac64");
        connection.setDoOutput(true);
        subscriptionKey = System.getenv("COMPUTER_VISION_SUBSCRIPTION_KEY");;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        connection.getOutputStream().write(data);

        InputStream inputStream = connection.getInputStream();
        String response = readResponse(inputStream);
        return response;
    }

    private String readResponse(InputStream inputStream) throws IOException, JSONException {
        StringBuilder stringBuilder = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != -1) {
            stringBuilder.append((char) c);
        }
        String response =  stringBuilder.toString();

        JSONObject jsonResponse = new JSONObject(response);
        JSONObject description = jsonResponse.getJSONObject("readResult");
        String content = description.getString("content");

        return content;
    }
}
/*import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        Button selectButton = findViewById(R.id.button);
        Button scanButton = findViewById(R.id.button2);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getDrawable() == null) {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                } else {
                    // Get the Bitmap from the ImageView
                    Bitmap selectedImage = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    // Convert Bitmap to byte array
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    final byte[] imageBytes = outputStream.toByteArray();
                    // Perform OCR using Computer Vision API
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String subscriptionKey = "c7d56f62dba043a4963b4754c47c6c55";
                                String uriBase = "https://ocr-cmpe-277.cognitiveservices.azure.com/";

                                URL url = new URL(uriBase);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/octet-stream");
                                connection.setRequestProperty("c7d56f62dba043a4963b4754c47c6c55", subscriptionKey);
                                connection.setDoOutput(true);

                                // Write the image data to the request body
                                connection.getOutputStream().write(imageBytes);

                                // Get the response
                                InputStream responseStream = connection.getInputStream();
                                StringBuilder responseBuilder = new StringBuilder();
                                int bytesRead;
                                byte[] buffer = new byte[1024];
                                while ((bytesRead = responseStream.read(buffer)) != -1) {
                                    responseBuilder.append(new String(buffer, 0, bytesRead));
                                }

                                String response = responseBuilder.toString();
                                JSONObject jsonResponse = new JSONObject(response);

                                // Extract and display the detected text
                                final StringBuilder detectedText = new StringBuilder();
                                JSONArray lines = jsonResponse.getJSONObject("analyzeResult").getJSONArray("readResults").getJSONObject(0).getJSONArray("lines");
                                for (int i = 0; i < lines.length(); i++) {
                                    detectedText.append(lines.getJSONObject(i).getString("text")).append("\n");
                                }

                                // Update UI in the main thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView.setText(detectedText.toString());
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                // Get the URI of the selected image
                InputStream imageStream = getContentResolver().openInputStream(data.getData());
                // Decode the InputStream into a Bitmap
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                // Set the Bitmap to the ImageView
                imageView.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}*/
