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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.taller2.adapters.ContactsAdapter;
import com.example.taller2.databinding.ActivityContactsBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;

    public static final int CONTACTS_PERM_ID = 5;

    String permContacts = Manifest.permission.READ_CONTACTS;

    String[] mProjection;
    Cursor mCursor;
    ContactsAdapter mContactsAdapter;
    ListView mContactListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContactListView = binding.contactlist;
        mProjection = new String[]{
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
        };

        // El Cursor está en null hasta que el usuario acepte el permiso
        mContactsAdapter = new ContactsAdapter(this, null, 0);
        mContactListView.setAdapter(mContactsAdapter);

        requestPermission(this, permContacts, "Se requiere acceso a los contactos para desplegar la lsta.", CONTACTS_PERM_ID);
        initView();
    }

    private void initView() {
        if(ContextCompat.checkSelfPermission(this, permContacts) == PackageManager.PERMISSION_GRANTED) {
            mCursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null);
            mContactsAdapter.changeCursor(mCursor);
        }
    }

    private void requestPermission(Activity context, String permission, String justification, int id) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permContacts)) {
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            // Request the perrmission
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERM_ID) {
            initView();
        }
    }
}