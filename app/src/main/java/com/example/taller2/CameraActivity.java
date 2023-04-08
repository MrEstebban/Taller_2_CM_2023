package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taller2.databinding.ActivityCameraBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class CameraActivity extends AppCompatActivity {

    private Logger logger = Logger.getLogger(MainActivity.class.getName());
    private ActivityCameraBinding binding;
    private final int CAMERA_PERMISSION_ID = 101;
    private final int GALLERY_PERMISSION_ID = 102;
    String cameraPerm = Manifest.permission.CAMERA;

    ImageView imageView;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        logger.info("Se va a solicitar el permiso");
        requestPermission(this, cameraPerm, "Permiso para utiliza la camara", CAMERA_PERMISSION_ID);
        initView();
        imageView = binding.imageView;
    }

    public void requestPermission(Activity context, String permission, String justification, int id) {
        // Se verifica si no hay permisos
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            // ¿Deberiamos mostrar una explicación?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, cameraPerm)) {
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            // Solicitar el permiso
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    private void initView() {
        if (ContextCompat.checkSelfPermission(this, cameraPerm) != PackageManager.PERMISSION_GRANTED) {
            logger.warning("Failed to getting the permission");
        } else {
            logger.info("Success getting the permission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_ID) {
            initView();
        }
    }

    public void startCamera(View view) {
        if (ContextCompat.checkSelfPermission(this, cameraPerm) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            logger.warning("Failed to getting the permission :(");
        }
    }

    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegurarse de que hay una actividad de camara para manejar el intent
        if (takePictureIntent != null) {
            //Crear el archivo donde debería ir la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                logger.warning(ex.getMessage());
            }
            //Continua solo el archivo ha sido exitosamente creado
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_PERMISSION_ID);
            }
        }
    }

    private File createImageFile() throws IOException {
        //Crear un nombre dde archivo de imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("IMG",".jpg", storageDir);

        // Guardar un archivo: Ruta para usar con ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        logger.info("Ruta: "+currentPhotoPath);
        return image;
    }

    public void startGallery(View view){
        Intent pickGalleryImage = new Intent(Intent.ACTION_PICK);
        pickGalleryImage.setType("image/*");
        startActivityForResult(pickGalleryImage, GALLERY_PERMISSION_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int rresultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, rresultCode, data);
        if (rresultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case CAMERA_PERMISSION_ID:
                    imageView.setImageURI(Uri.parse(currentPhotoPath));
                    logger.info("Image capture successfully.");
                    break;
                case GALLERY_PERMISSION_ID:
                    Uri imageUri = data.getData();
                    imageView.setImageURI(imageUri);
                    logger.info("Image loaded successfully");
                    break;
            }
        }
    }
}