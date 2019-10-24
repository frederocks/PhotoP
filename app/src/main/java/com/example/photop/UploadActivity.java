package com.example.photop;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.type.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.photop.MainActivity.MY_PERMISSIONS_REQUEST_LOCATION;

class Upload {
    public String name;

    public String userId;
    public String uploadUri;
    public GeoLocation geoLocation;

    public Upload(String name, String userId, String uploadUri) {
        this.name = name;

        this.userId = userId;
        this.uploadUri = uploadUri;

    }
}

public class UploadActivity extends AppCompatActivity {
    private EditText mNameField;
    private ImageView mFoodPhoto;
    double lat = 43.547302;
    double lng = -96.728333;
    //List<String> LatLng1 = new ArrayList<String>();

    String[] mLatLng = {Double.toString(lat), Double.toString(lng)};
    //LatLng mFoodLatLng = new Array(lat, lng);

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mUploadsDatabase, mUserDatabase, mGeoFireDatabase;
    private String userId, name, location, foodImageUrl;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mNameField = findViewById(R.id.name);
        mFoodPhoto = findViewById(R.id.foodPhoto);
        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUploadsDatabase = FirebaseDatabase.getInstance().getReference().child("Uploads");

        mGeoFireDatabase = FirebaseDatabase.getInstance().getReference().child("GeoFire");

        mFoodPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Log.d("saving", "starting Save");
                saveFoodInformation();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void saveFoodInformation() {
        name = mNameField.getText().toString();
        Log.d("name", name);
        String foodSaveUTC = String.valueOf(System.currentTimeMillis());
        final DatabaseReference newUploadRef = mUploadsDatabase.push();
        String newID = newUploadRef.getKey();
        final GeoFire geoFire = new GeoFire(mGeoFireDatabase);
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final GeoLocation geoLocation;
        final Location location;

        //if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            Log.d("location", "location not set");

            checkLocationPermission();
            //return;
        }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Log.d("gettingImage", "starting upload");
            if (resultUri != null){

                final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("foodImageUrl").child(foodSaveUTC);
                Bitmap bitmap = null;

                try {
                    bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("failuer", e.toString());
                        finish();
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
//                            Map newImage = new HashMap();
//                            newImage.put("foodImageUrl", uri.toString());
//                            mUserDatabase.child("uploads").updateChildren(newImage);
                                newUploadRef.setValue(new Upload(name, userId, uri.toString() ));
                                Log.d("where am i ", location.toString());
                                geoFire.setLocation(newUploadRef.getKey(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                    }
                                });
                                //mUserDatabase.updateChildren(newImage);

                                finish();
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("failuer", e.toString());
                                finish();
                                return;
                            }
                        });
                    }
                });
            }else{
                finish();
            }
            return;
        //}



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            Log.d("imageURI", imageUri.toString());
            resultUri = imageUri;
            mFoodPhoto.setImageURI(resultUri);
        }
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(this)
//                        .setTitle(R.string.title_location_permission)
//                        .setMessage(R.string.text_location_permission)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                new AlertDialog.Builder(this)
                        .setTitle("Please give permission")
                        .setMessage("To allow to find you")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(UploadActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
