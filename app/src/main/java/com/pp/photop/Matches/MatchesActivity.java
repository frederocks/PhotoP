package com.pp.photop.Matches;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pp.photop.R;

import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mMatchesAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager;
    private GeoFire geoFire;
    private DatabaseReference geoFireDb;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        geoFireDb = FirebaseDatabase.getInstance().getReference().child("GeoFire");
        geoFire = new GeoFire(geoFireDb);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mMatchesLayoutManager = new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);

        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), MatchesActivity.this);
        mRecyclerView.setAdapter((mMatchesAdapter));

        getUserMatchId();



//        MatchesObject obj = new MatchesObject("asd");
//        resultsMatches.add(obj);

    }

    private void getUserMatchId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("history").child("yep");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot match : dataSnapshot.getChildren()){
                        FetchMatchInformation(match.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchMatchInformation(String key) {
        DatabaseReference uploadDb = FirebaseDatabase.getInstance().getReference().child("Uploads").child(key);
        uploadDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String foodImageUrl = "";
                    String lat = "";
                    String lng = "";
                    String uploadUserName = "Default";
                    String phone = "605-555-5555";

                    if (dataSnapshot.child("name").getValue() != null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.child("uploadUri").getValue() != null){
                        foodImageUrl = dataSnapshot.child("uploadUri").getValue().toString();
                    }
                    if (dataSnapshot.child("uploadUserName").getValue() != null){
                        uploadUserName = dataSnapshot.child("uploadUserName").getValue().toString();
                    }
                    if (dataSnapshot.child("lat").getValue() != null){
                        lat = dataSnapshot.child("lat").getValue().toString();
                    }
                    if (dataSnapshot.child("phone").getValue() != null){
                        phone = dataSnapshot.child("phone").getValue().toString();
                    }
                    if (dataSnapshot.child("lng").getValue() != null){
                        lng = dataSnapshot.child("lng").getValue().toString();
                    }
                    MatchesObject obj = new MatchesObject(userId, name, foodImageUrl, lat, lng, uploadUserName, phone);

                    resultsMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesObject> resultsMatches = new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetMatches() {
        return resultsMatches;
    }
}
