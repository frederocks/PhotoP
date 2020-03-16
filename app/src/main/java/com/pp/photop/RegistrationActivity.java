package com.pp.photop;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pp.photop.databinding.ActivityRegistrationBinding;

import java.util.HashMap;
import java.util.Map;

import static com.pp.photop.MainActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private DatabaseReference mGeoFireDatabase;

    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectId = binding.radioGroup.getCheckedRadioButtonId();
                if( selectId == -1 ) {
                    return;
                }

                int selectedChildIndex = -1;
                for( int i = 0; i < binding.radioGroup.getChildCount() && selectedChildIndex == -1; i++ ) {
                    if( selectId == binding.radioGroup.getChildAt(i).getId() ) {
                        selectedChildIndex = selectId;
                    }
                }
                if( selectedChildIndex == -1 ) {
                    return;
                }
                final RadioButton radioButton = (RadioButton) binding.radioGroup.getChildAt(selectedChildIndex);

                final String email = binding.email.getText().toString().trim();
                final String password = binding.password.getText().toString();
                final String name = binding.name.getText().toString();
                final String phone = binding.phone.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                        }else{
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
                            mGeoFireDatabase = FirebaseDatabase.getInstance().getReference().child("GeoFire");
                            final GeoFire geoFire = new GeoFire(mGeoFireDatabase);
//                            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                            final GeoLocation geoLocation;
//                            final Location location;
//
//                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            //DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
//                            Double lat = location.getLatitude();
//                            Double lng = location.getLongitude();
                            Double lat = 43.544327;
                            Double lng = -96.730641;


                            Map userInfo = new HashMap<>();
                            userInfo.put("name", name);
                            userInfo.put("userStatus", radioButton.getText().toString());
                            userInfo.put("profileImageUrl", "default");
                            userInfo.put("distance", "5");
                            userInfo.put("lat", lat);
                            userInfo.put("lng", lng);
                            userInfo.put("phone", phone);
                            userInfo.put("glutenfree", "false");
                            userInfo.put("chinese", "false");
                            userInfo.put("vegan", "false");
                            userInfo.put("mexican", "false");
                            userInfo.put("brunch", "false");
                            userInfo.put("italian", "false");
                            userInfo.put("pizza", "false");
                            userInfo.put("dessert", "false");

                            currentUserDb.updateChildren(userInfo);
                            geoFire.setLocation(userId, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
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
                                ActivityCompat.requestPermissions(RegistrationActivity.this,
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


