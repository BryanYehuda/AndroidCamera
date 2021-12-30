package com.example.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    Button button, btnUpload, btnChoose;
    ImageView imageview;
    String namaFile;
    Bitmap bitmap;

    private static final int kodekamera = 222;
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 223;
    final int CODE_GALLERY_REQUEST = 999;
    String url = "https://192.168.0.127/AndroidUploadImage/uploadimage.php";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        askWritePermission();
        setContentView(R.layout.activity_main);

        btnUpload = findViewById(R.id.btn_upload);
        btnChoose = findViewById(R.id.pilih_foto);
        button = findViewById(R.id.button);
        imageview = findViewById(R.id.imageView);

        button.setOnClickListener(view ->
        {
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File imagesFolder = new File(Environment.getExternalStorageDirectory(), "HasilFoto");
            imagesFolder.mkdirs();
            Date d = new Date();
            CharSequence s  = DateFormat.format("yyyyMMdd-hh-mm-ss", d.getTime());
            namaFile = imagesFolder + File.separator+  s.toString() + ".jpg";
            File image = new File(namaFile);

            Uri uriSavedImage = Uri.fromFile(image);
            it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            startActivityForResult(it, kodekamera);
        });

        btnChoose.setOnClickListener(view -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_GALLERY_REQUEST));
        btnUpload.setOnClickListener(view -> uploadImage());
    }

    private void askWritePermission()
    {
        if (android.os.Build.VERSION.SDK_INT >= 23)
        {
            int cameraPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED)
            {
                this.requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CODE_GALLERY_REQUEST)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), CODE_GALLERY_REQUEST);
            }else
                {
                    Toast.makeText(getApplicationContext(), "You don't have permission to access gallery!", Toast.LENGTH_LONG).show();
                }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == kodekamera) {
                try
                {
                    prosesKamera();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == CODE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Uri path = data.getData();
            try
            {
                InputStream inputStream = getContentResolver().openInputStream(path);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageview.setImageBitmap(bitmap);
                imageview.setVisibility(View.VISIBLE);
                btnUpload.setVisibility(View.VISIBLE);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            Toast.makeText(MainActivity.this, "Foto yang dipilih telah Terupload di ImageView", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage()
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response ->
        {
            try
            {
                JSONObject jsonObject = new JSONObject(response);
                String Response = jsonObject.getString("response");
                Toast.makeText(getApplicationContext(), Response, Toast.LENGTH_LONG).show();
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(getApplicationContext(), "error: " + error.toString(), Toast.LENGTH_LONG).show())
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                String gambar = imagetoString(bitmap);
                params.put("foto", gambar);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private String imagetoString(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageType = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageType, Base64.DEFAULT);
    }

    private void prosesKamera() throws IOException
    {
        Bitmap bm;
        BitmapFactory.Options options;
        options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        bm = BitmapFactory.decodeFile(namaFile,options);
        imageview.setImageBitmap(bm);
        Toast.makeText(this,"Foto Telah Terupload ke ImageView dan Tersimpan dengan nama " +namaFile, Toast.LENGTH_SHORT).show();
    }

}
