package com.pp.photop;

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
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pp.photop.databinding.ActivityUploadBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.pp.photop.MainActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class UploadActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final int CAMERA_PIC_REQUEST = 2;
    double lat = 43.547302;
    double lng = -96.728333;
    //List<String> LatLng1 = new ArrayList<String>();

    String[] mLatLng = {Double.toString(lat), Double.toString(lng)};
    //LatLng mFoodLatLng = new Array(lat, lng);
    private FirebaseAuth mAuth;
    private DatabaseReference mUploadsDatabase, mUserDatabase, mGeoFireDatabase;
    private Upload mUploadObj;
    private Uri resultUri;
    private Bitmap image;

    ActivityUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mUploadObj = new Upload();
        mUploadObj.userId = mAuth.getCurrentUser().getUid();
        mUploadObj.rating = (float) 1.0;

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mUploadObj.userId);
        mUploadsDatabase = FirebaseDatabase.getInstance().getReference().child("Uploads");
        mGeoFireDatabase = FirebaseDatabase.getInstance().getReference().child("GeoFire");

        binding.rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> mUploadObj.rating = rating);

        binding.glutenfree.setOnCheckedChangeListener(this);
        binding.vegan.setOnCheckedChangeListener(this);
        binding.pizza.setOnCheckedChangeListener(this);
        binding.chinese.setOnCheckedChangeListener(this);
        binding.italian.setOnCheckedChangeListener(this);
        binding.dessert.setOnCheckedChangeListener(this);
        binding.brunch.setOnCheckedChangeListener(this);
        binding.mexican.setOnCheckedChangeListener(this);

        binding.foodPhoto.setOnClickListener(this);
        binding.confirm.setOnClickListener(this);
        binding.back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.foodPhoto:
                //to choose from file
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
                //to choose camera activity
//                Intent photo= new Intent("android.media.action.IMAGE_CAPTURE");
//                startActivityForResult(photo, CAMERA_PIC_REQUEST);
//                photo.putExtra(MediaStore.EXTRA_OUTPUT, photo.getData());
                break;

            case R.id.confirm:
                Log.d("saving", "starting Save");
                saveFoodInformation();
                break;

            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()) {
            case R.id.glutenfree:
                mUploadObj.glutenfree = String.valueOf(buttonView.isChecked());
                break;

            case R.id.vegan:
                mUploadObj.vegan = String.valueOf(buttonView.isChecked());
                break;

            case R.id.pizza:
                mUploadObj.pizza = String.valueOf(buttonView.isChecked());
                break;

            case R.id.chinese:
                mUploadObj.chinese = String.valueOf(buttonView.isChecked());
                break;

            case R.id.italian:
                mUploadObj.italian = String.valueOf(buttonView.isChecked());
                break;

            case R.id.dessert:
                mUploadObj.dessert = String.valueOf(buttonView.isChecked());
                break;

            case R.id.brunch:
                mUploadObj.brunch = String.valueOf(buttonView.isChecked());
                break;

            case R.id.mexican:
                mUploadObj.mexican = String.valueOf(buttonView.isChecked());
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void saveFoodInformation() {
        mUploadObj.name = binding.name.getText().toString();
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("name").getValue() != null){
                        mUploadObj.uploadUserName = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.child("phone").getValue() != null){
                        mUploadObj.phone = dataSnapshot.child("phone").getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        //glutenfree = mGlutenFree.
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
            //image for camera image
        //if (image != null){
            final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("foodImageUrl").child(foodSaveUTC);
            Bitmap bitmap = null;

            //for gallery image
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            //image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            //image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            //prevent memory leak
            //image.recycle();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(e -> {
                Log.d("failuer", e.toString());
                finish();
            });
            uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
//                            Map newImage = new HashMap();
//                            newImage.put("foodImageUrl", uri.toString());
//                            mUserDatabase.child("uploads").updateChildren(newImage);
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                mUploadObj.lat = String.valueOf(lat);
                mUploadObj.lng = String.valueOf(lng);
                mUploadObj.no = 1L;
                mUploadObj.yes = 1L;
                newUploadRef.setValue(mUploadObj);

                geoFire.setLocation(newUploadRef.getKey(), new GeoLocation(lat, lng), (key, error) -> {});
                //mUserDatabase.updateChildren(newImage);

                finish();
            }).addOnFailureListener(e -> {
                Log.d("failuer", e.toString());
                finish();
            }));
        }else{
            finish();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            //getting food from photo
//            image = (Bitmap) data.getExtras().get("data");
//            ImageView imageView = (ImageView) findViewById(R.id.foodPhoto);
//            imageView.setImageBitmap(image);

            //getting from from phone gallery
            resultUri = data.getData();
            binding.foodPhoto.setImageURI(resultUri);
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