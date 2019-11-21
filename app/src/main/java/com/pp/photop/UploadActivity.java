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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.pp.photop.MainActivity.MY_PERMISSIONS_REQUEST_LOCATION;

class Upload {
    public String name;
    public String uploadUserName;
    public String phone;
    public String userId;
    public String uploadUri;
    public String glutenfree;
    public String vegan;
    public String pizza;
    public String chinese;
    public String italian;
    public String dessert;
    public String brunch;
    public String mexican;
    public String lat;
    public String lng;
    public Float rating;
    public Long yes;
    public Long no;

    public Upload(String name, String userId, String uploadUri, String glutenfree, String vegan, String pizza, String chinese, String italian,
                  String dessert, String brunch, String mexican, String lat, String lng, Float rating, String uploadUserName, String phone, Long yes, Long no) {
        this.name = name;
        this.userId = userId;
        this.uploadUri = uploadUri;
        this.glutenfree = glutenfree;
        this.vegan = vegan;
        this.pizza = pizza;
        this.chinese = chinese;
        this.italian = italian;
        this.dessert = dessert;
        this.brunch = brunch;
        this.mexican = mexican;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.uploadUserName = uploadUserName;
        this.phone = phone;
        this.yes = yes;
        this.no = no;
    }
}

public class UploadActivity extends AppCompatActivity {
    public static final int CAMERA_PIC_REQUEST = 2;
    private EditText mNameField;
    private ImageView mFoodPhoto;
    double lat = 43.547302;
    double lng = -96.728333;
    //List<String> LatLng1 = new ArrayList<String>();

    String[] mLatLng = {Double.toString(lat), Double.toString(lng)};
    //LatLng mFoodLatLng = new Array(lat, lng);
    private CheckBox mGlutenFree, mVegan, mPizza, mChinese, mItalian, mDessert, mBrunch, mMexican;
    private Button mBack, mConfirm;
    private RatingBar mRating;
    private FirebaseAuth mAuth;
    private DatabaseReference mUploadsDatabase, mUserDatabase, mGeoFireDatabase;
    private String userId, name, uploadUserName, phone,glutenfree = "false", vegan= "false", pizza= "false", chinese= "false", italian= "false", dessert= "false", brunch= "false", mexican= "false";
    private Uri resultUri;
    private Bitmap image;
    private float userRating = (float) 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mNameField = findViewById(R.id.name);
        mFoodPhoto = findViewById(R.id.foodPhoto);
        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);
        mGlutenFree = findViewById(R.id.glutenfree);
        mVegan = findViewById(R.id.vegan);
        mPizza = findViewById(R.id.pizza);
        mChinese = findViewById(R.id.chinese);
        mItalian = findViewById(R.id.italian);
        mDessert = findViewById(R.id.dessert);
        mBrunch = findViewById(R.id.brunch);
        mMexican = findViewById(R.id.mexican);
        mRating = findViewById(R.id.rating);

        mRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRating = rating;
            }
        });

        mGlutenFree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mGlutenFree.isChecked()){
                    glutenfree = "true";
                }
                else glutenfree = "false";
            }
        });
        mVegan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mVegan.isChecked()){
                    vegan = "true";
                }
                else vegan = "false";
            }
        });

        mPizza.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mPizza.isChecked()){
                    pizza = "true";
                }
                else pizza = "false";
            }
        });
        mChinese.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mChinese.isChecked()){
                    chinese = "true";
                }
                else chinese = "false";
            }
        });
        mItalian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mItalian.isChecked()){
                    italian = "true";
                }
                else italian = "false";
            }
        });
        mDessert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mDessert.isChecked()){
                    dessert = "true";
                }
                else dessert = "false";
            }
        });
        mBrunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mBrunch.isChecked()){
                    brunch = "true";
                }
                else brunch = "false";
            }
        });
        mMexican.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mMexican.isChecked()){
                    mexican = "true";
                }
                else mexican = "false";
            }
        });


        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUploadsDatabase = FirebaseDatabase.getInstance().getReference().child("Uploads");

        mGeoFireDatabase = FirebaseDatabase.getInstance().getReference().child("GeoFire");

        mFoodPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to choose from file
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
                //to choose camera activity
//                Intent photo= new Intent("android.media.action.IMAGE_CAPTURE");
//                startActivityForResult(photo, CAMERA_PIC_REQUEST);
//                photo.putExtra(MediaStore.EXTRA_OUTPUT, photo.getData());

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
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("name").getValue() != null){
                        uploadUserName = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.child("phone").getValue() != null){
                        phone = dataSnapshot.child("phone").getValue().toString();
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
                                Double lat = location.getLatitude();
                                Double lng = location.getLongitude();
                                int i = 1;
                                Long no = new Long(i);
                                Long yes = new Long(i);
                                newUploadRef.setValue(new Upload(name, userId, uri.toString(), glutenfree, vegan, pizza, chinese, italian, dessert, brunch, mexican, lat.toString(), lng.toString(), userRating, uploadUserName, phone, yes, no));

                                geoFire.setLocation(newUploadRef.getKey(), new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
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
            //getting food from photo
//            image = (Bitmap) data.getExtras().get("data");
//            ImageView imageView = (ImageView) findViewById(R.id.foodPhoto);
//            imageView.setImageBitmap(image);

            //getting from from phone gallery
            final Uri imageUri = data.getData();
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
