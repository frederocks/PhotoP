package com.pp.photop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.pp.photop.Cards.arrayAdapter;
import com.pp.photop.Cards.cards;
import com.pp.photop.Matches.MatchesActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    public static final GeoLocation NEW_YORK = new GeoLocation(40.730292, -73.990401);
    public static final GeoLocation Sioux_Falls = new GeoLocation(43.5555306, -96.7228278);
    private static final String TAG = "TAG";

    private cards cards_data[];
    private com.pp.photop.Cards.arrayAdapter arrayAdapter;
    private int i;


    private boolean isContinue = false;
    private boolean isGPS = false;
    private FirebaseAuth mAuth;
    GeoQuery geoQuery;
    GeoLocation geoLocation;
    Location lastKnownLocation, mCurrentLocation;
    private String currentUId;

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView txtLocation;
    private LocationRequest locationRequest;
    private GeoFire geoFire;
    private int distance = 5;

    private DatabaseReference usersDb, uploadDb, geoFireDb;
    List<String> nearbyItems;
    List<String> nearbyUsersList = new ArrayList<>();
    List<FoodProperties> foodObjects = new ArrayList<>();
    ListView listView;
    List<cards> rowItems;
    GeoQuery geoQueryNearByUser = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadActivity();

    }
    private void loadActivity(){
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);

        Places.initialize(getApplicationContext(), "AIzaSyAPp_5yCUKDL318bpIccPxvwgaXl0MMrjA");
        PlacesClient placesClient = Places.createClient(this);

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.newInstance(placeFields);
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e("tag", "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            checkLocationPermission();
        }
        // Initialize Places.


        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        uploadDb = FirebaseDatabase.getInstance().getReference().child("Uploads");
        geoFireDb = FirebaseDatabase.getInstance().getReference().child("GeoFire");
        geoFire = new GeoFire(geoFireDb);
        GeoQuery geoQueryNearby = null;
        //this.txtLocation = (TextView) findViewById(R.id.txtLocation);
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        rowItems = new ArrayList<cards>();
        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        } else {
            try {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                final GeoLocation geoLocation;
                final Location location;
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //  Block of code to try
                final String[] d = new String[1];
//                String lat = Double.toString(location.getLatitude());
//                String lng = Double.toString(location.getLongitude());
//                String latlng = lat + ", " + lng;
//                Date date = new Date(System.currentTimeMillis());
//                usersDb.child(currentUId).child("locationHistory").child(String.valueOf(date)).setValue(latlng);

                usersDb.child(currentUId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String dd = dataSnapshot.child("distance").getValue().toString();
                        if (dd != ""){
                            distance = Integer.parseInt(dd);
                        }
                        else distance = 3;
                        final String brunch = dataSnapshot.child("brunch").getValue().toString();

                        distance *= 1.6;
                        final GeoLocation geoHash = new GeoLocation(location.getLatitude(), location.getLongitude());

                        geoFire.setLocation(currentUId, geoHash, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    Log.d("MainActivity", "There was an error saving the location to GeoFire: " + error);
                                } else {
                                    Log.d("MainActivity", "Location saved on server successfully!");
                                    GeoQuery geoQuery;
                                    Log.d("query dist", String.valueOf(distance));
                                    geoQuery = geoFire.queryAtLocation(geoHash, distance);
                                    //geoQuery.removeAllListeners();
                                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                        // user has been found within the radius:
                                        @Override
                                        public void onKeyEntered(String key, GeoLocation location) {
                                            Log.d("MainActivity", key + " just entered the radius. Going to display it as a potential match!");
                                            nearbyUsersList.add(key);
                                            FoodProperties fp = new FoodProperties(key, brunch);
                                            foodObjects.add(fp);
                                        }

                                        @Override
                                        public void onKeyExited(String key) {
                                            Log.d("MainActivity", key + " just exited the radius.");
                                        }

                                        @Override
                                        public void onKeyMoved(String key, GeoLocation location) {
                                        }

                                        // all users within the radius have been identified:
                                        @Override
                                        public void onGeoQueryReady() {
                                            displayPotentialMatches();
                                        }

                                        @Override
                                        public void onGeoQueryError(DatabaseError error) {
                                        }
                                    });
                                    //getNearbyUsers();
                                }
                            }
                        });
                        final SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
                        flingContainer.setAdapter(arrayAdapter);
                        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
                            @Override
                            public void removeFirstObjectInAdapter() {
                                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                                Log.d("LIST", "removed object!");
                                rowItems.remove(0);
                                arrayAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onLeftCardExit(Object dataObject) {
                                //Do something on the left!
                                //You also have access to the original object.
                                //If you want to use it just cast it (String) dataObject
                                cards obj = (cards) dataObject;
                                String foodId = obj.getUserId();
                                usersDb.child(currentUId).child("history").child("nope").child(foodId).setValue(true);
                                Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onRightCardExit(Object dataObject) {
                                cards obj = (cards) dataObject;
                                String foodId = obj.getUserId();
                                usersDb.child(currentUId).child("history").child("yep").child(foodId).setValue(true);
                                Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                            }

                            @Override
                            public void onScroll(float scrollProgressPercent) {
                            }
                        });
                        // Optionally add an OnItemClickListener
                        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClicked(int itemPosition, Object dataObject) {
//                                Fra layoutToAdd;
//                                layoutToAdd = findViewById(R.id.map);
                                @SuppressLint("ResourceType") View fragment =  findViewById(R.layout.activity_maps2);
                                //LayoutInflater inflater = LayoutInflater.from(getBaseContext());

                                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
                                Log.d("dataObject", dataObject.toString());
                                FragmentManager fragmentManager = getSupportFragmentManager();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            } catch (Exception e) {
                //  Block of code to handle errors
                distance = 100;
                Log.d("distanc exception", "100" + e.toString());
            }
        }
    }
    // retrieve users from database and display them on cards, based on the location and various filters:
    private void displayPotentialMatches() {
        Log.d("MainActivity", "displayPotentialMatches() triggered!");
//        for (String f : nearbyUsersList) {
//            Log.d("brunch", f);
//        }
        uploadDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // check if there is any new potential match and if the current user hasn't swiped with them yet:

//                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
//                    String profileImageUrl = "default";
//                    if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
//                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
//                    }

                //for (String i : nearbyUsersList){
                for (FoodProperties i : foodObjects){
                    //update uploads db if missing key
//                    if(dataSnapshot.child("brunch").exists()){
//                        String key = dataSnapshot.getKey();
//                        uploadDb.child(key).child("brunch").setValue("false");
//                    }

                    if (dataSnapshot.exists()
                            && !dataSnapshot.getKey().equals(currentUId)
                            && dataSnapshot.child("brunch").getValue().toString().equals(i.getBrunch())
                            // location check:
                            //&& nearbyUsersList.contains(dataSnapshot.getKey())
                            && i.getKey().equals(dataSnapshot.getKey())
                    ) {
                        String profilePictureURL = "default";
                        if (!dataSnapshot.child("uploadUri").getValue().equals("default")) {
                            profilePictureURL = dataSnapshot.child("uploadUri").getValue().toString();
                        }
                        // POPULATE THE CARD WITH THE DATABASE INFO:
                        Log.d("MainActivity", dataSnapshot.getKey() + " passed all the match checks!");
                        cards potentialMatch = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profilePictureURL);
                        rowItems.add(potentialMatch);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    } // end of displayPotentialMatches()


    com.google.android.gms.location.LocationCallback locationCallback = new com.google.android.gms.location.LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d("MainActivity", "onLocationResult triggered!");
            if (locationResult == null) {
                return;
            }
            mCurrentLocation = locationResult.getLastLocation();
            geoFire.setLocation(currentUId, new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        Log.d("MainActivity", "There was an error saving the location to GeoFire: " + error);
                    } else {
                        Log.d("MainActivity", "Location saved on server successfully!");
                        // find nearby obj  of the current user's location:
                        getNearbyUsers();
                    }
                }
            });
            if (!isContinue && mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(locationCallback);
            }

        }
    };
    GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            Log.d("geofire key has entered", key);
//            nearbyItems.add(key);
            uploadDb.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if (dataSnapshot.exists() && nearbyItems.contains(dataSnapshot.getKey())
                    if (dataSnapshot.exists() && !dataSnapshot.getKey().equals(currentUId)) {
                        Log.d("OndataChangeName", dataSnapshot.child("name").getValue().toString());
                        String foodImageUrl = "default";
                        if (!dataSnapshot.child("uploadUri").getValue().equals("default")) {
                            foodImageUrl = dataSnapshot.child("uploadUri").getValue().toString();
                        }
                        cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), foodImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onKeyExited(String key) {

        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
        }

        @Override
        public void onGeoQueryReady() {
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
        }
    };

    private void getNearbyUsers() {
        Log.d("MainActivity", "getNearbyUsers() triggered!");

        //GeoLocation currentLocationGeoHash = new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        GeoLocation currentLocationGeoHash = new GeoLocation(43.536388, -96.731667);
        if (geoQueryNearByUser == null) {
            geoQueryNearByUser = geoFire.queryAtLocation(currentLocationGeoHash, 8.0);

            geoQueryNearByUser.addGeoQueryEventListener(geoQueryEventListener);
        } else {
            geoQueryNearByUser.setCenter(currentLocationGeoHash);
        }

    }

    // end of getNearbyUsers()
    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(MainActivity.this, "new Connection", Toast.LENGTH_LONG).show();
                usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).setValue(true);
                usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String userSex;
    private String oppositeUserSex;
    private String userPreferences;

    public void checkUserPreferences() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getFoodObject();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    public void getFoodObject() {
        //GeoFire geoFire = new GeoFire(uploadDb.child("geoFire"));
        GeoFire geoFire = new GeoFire(geoFireDb);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(43.544857412461134, -96.72661488688865), 8);
        uploadDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String foodImageUrl = "default";

                    //StorageReference filepath = FirebaseStorage.getInstance().getReference().child("foodImageUrl");
                    if (!dataSnapshot.child("uploadUri").getValue().equals("default")) {
                        foodImageUrl = dataSnapshot.child("uploadUri").getValue().toString();
                    }
                    cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), foodImageUrl);
                    rowItems.add(item);
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void checkUserSex() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("sex").getValue() != null) {
                        userSex = dataSnapshot.child("sex").getValue().toString();
                        switch (userSex) {
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        getOppositeSexUsers();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void getOppositeSexUsers() {
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
                    String profileImageUrl = "default";
                    if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }
                    cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), profileImageUrl);
                    rowItems.add(item);
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    public void CheckPermission() {
        // check permission
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {
            // already permission granted
            // get location here
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                    }
                }
            });
        }
    }


    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 11);

        //startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;
    }

    public void goToUpload(View view) {
        Intent intent = new Intent(MainActivity.this, UploadActivity.class);
        startActivity(intent);
        return;

    }
    public void goToMap(View view){
        Intent intent = new Intent(MainActivity.this, MapsActivity2.class );
        startActivity(intent);
        return;
    }



    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);
        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("GetLastLocation", location.toString());
                        if (location != null) {
                            mCurrentLocation = location;

                            wayLatitude = location.getLatitude();
                            wayLongitude = location.getLongitude();

                        } else {
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        }
                    }
                });
            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    ACCESS_FINE_LOCATION)) {

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
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    wayLatitude = location.getLatitude();
                                    wayLongitude = location.getLongitude();
                                } else {
                                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
        if (requestCode == 11){
            Log.d("Main Reload", "requestCode triggered");
            loadActivity();
        }
    }

}