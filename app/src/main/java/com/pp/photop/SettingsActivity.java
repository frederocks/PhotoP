package com.pp.photop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pp.photop.databinding.ActivitySettingsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    int progressChangedValue = 3;
    private String userId, name, phone, distance;
    private String mStringFormat;

    private Uri resultUri;
    private ActivitySettingsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStringFormat = getString(R.string.distance_format);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        //mSeekBar.setProgress(savedProgress);
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                updateDistance();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateDistance();
            }
        });


        getUserInfo();
        binding.profileImage.setOnClickListener(this);
        binding.confirm.setOnClickListener(this);
        binding.back.setOnClickListener(this);
    }

    private void updateDistance() {
        binding.distance.setText(String.format(mStringFormat, progressChangedValue));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileImage:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                break;

            case R.id.confirm:
                saveUserInformation();
                break;

            case R.id.back:
                finish();
                break;
        }
    }

    private void getUserInfo(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(t);
                    if( map == null ) {
                        return;
                    }

                    if (map.get("distance")!= null){
                        distance = map.get("distance").toString();
                        binding.seekbar.setProgress(Integer.parseInt(distance));
                    }
                    if (map.get("name")!=null){
                        name = map.get("name").toString();
                        binding.name.setText(name);
                    }
                    if (map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        binding.phone.setText(phone);
                    }
                    if (map.get("glutenfree")!=null){
                        binding.glutenfree.setChecked("true".equals(map.get("glutenfree").toString()));
                    }
                    if (map.get("vegan")!=null){
                        binding.vegan.setChecked("true".equals(map.get("vegan").toString()));
                    }
                    if (map.get("pizza")!=null){
                        binding.pizza.setChecked("true".equals(map.get("pizza").toString()));
                    }
                    if (map.get("chinese")!=null){
                        binding.chinese.setChecked("true".equals(map.get("chinese").toString()));
                    }
                    if (map.get("italian")!=null){
                        binding.italian.setChecked("true".equals(map.get("italian").toString()));
                    }
                    if (map.get("dessert")!=null){
                        binding.dessert.setChecked("true".equals(map.get("dessert").toString()));
                    }
                    if (map.get("brunch")!=null){
                        binding.brunch.setChecked("true".equals(map.get("brunch").toString()));
                    }
                    if (map.get("mexican")!=null){
                        binding.mexican.setChecked("true".equals(map.get("mexican").toString()));
                    }
                    if (map.get("profileImageUrl")!=null){
                        String profileImageUrl = map.get("profileImageUrl").toString();
                        if ("default".equals(profileImageUrl)) {
                            Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(binding.profileImage);
                        } else {
                            Glide.with(getApplication()).load(profileImageUrl).into(binding.profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        name = binding.name.getText().toString();
        phone = binding.phone.getText().toString();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("distance", progressChangedValue);
        mUserDatabase.updateChildren(userInfo);
        if (resultUri != null){
            final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImageUrl").child(userId);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(e -> finish());
                uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> newImage = new HashMap<>();
                    newImage.put("profileImageUrl", uri.toString());
                    mUserDatabase.updateChildren(newImage);

                    finish();
                }).addOnFailureListener(e -> finish()));
            } catch (IOException e) {
                Log.e(TAG, "Couldn't get Bitmap from MediaStore", e);
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            resultUri = data.getData();
            binding.profileImage.setImageURI(resultUri);
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.glutenfree:
                mUserDatabase.child("glutenfree").setValue(checked);
                break;
            case R.id.vegan:
                mUserDatabase.child("vegan").setValue(checked);
                break;
            case R.id.pizza:
                mUserDatabase.child("pizza").setValue(checked);
                break;
            case R.id.chinese:
                mUserDatabase.child("chinese").setValue(checked);
                break;
            case R.id.italian:
                mUserDatabase.child("italian").setValue(checked);
                break;
            case R.id.dessert:
                mUserDatabase.child("dessert").setValue(checked);
                break;
            case R.id.brunch:
                mUserDatabase.child("brunch").setValue(checked);
                break;
            case R.id.mexican:
                mUserDatabase.child("mexican").setValue(checked);
                break;
        }
    }
}
