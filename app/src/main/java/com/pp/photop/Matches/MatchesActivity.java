package com.pp.photop.Matches;

import android.os.Bundle;
import android.util.Log;

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
import com.pp.photop.databinding.ActivityMatchesBinding;

import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {
    private static final String TAG = MatchesActivity.class.getSimpleName();

    private RecyclerView.Adapter mMatchesAdapter;
    private GeoFire geoFire;
    private DatabaseReference geoFireDb;
    private String currentUserID;

    private ActivityMatchesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        geoFireDb = FirebaseDatabase.getInstance().getReference().child("GeoFire");
        geoFire = new GeoFire(geoFireDb);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mMatchesAdapter = new MatchesAdapter(getDataSetMatches());
        binding.recyclerView.setAdapter((mMatchesAdapter));

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
                    MatchesObject obj = dataSnapshot.getValue(MatchesObject.class);
                    if( obj == null ) {
                        Log.d(TAG, "MatchesObject was null!");
                        return;
                    }

                    //we have to set this separately because it's not part of the db structure
                    obj.setUserId(dataSnapshot.getKey());

                    //set some defaults if returned value was null
                    obj.setDefaultsWhenNull("", "", "", "", "Default", "605-555-5555");

                    resultsMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<MatchesObject> resultsMatches = new ArrayList<>();
    private List<MatchesObject> getDataSetMatches() {
        return resultsMatches;
    }
}
