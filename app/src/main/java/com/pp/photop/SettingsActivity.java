package com.pp.photop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

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

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    int progressChangedValue = 3;
    int savedProgress = 3;
    private String userId, name, phone, profileImageUrl, glutenfree, vegan, pizza, chinese, italian, dessert, brunch, mexican, distance;
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SettingsActivity.this, String.format(mStringFormat, progressChangedValue),
                        Toast.LENGTH_SHORT).show();
            }
        });


        getUserInfo();
        binding.profileImage.setOnClickListener(this);
        binding.confirm.setOnClickListener(this);
        binding.back.setOnClickListener(this);
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
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    GenericTypeIndicator<Map<String,Object>> t = new GenericTypeIndicator<Map<String,Object>>() {};
                    Map<String, Object> map = dataSnapshot.getValue(t);
                    if (map.get("distance")!= null){
                        distance = map.get("distance").toString();
                        savedProgress = Integer.parseInt(distance);
                        binding.seekbar.setProgress(savedProgress);
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
                        glutenfree = map.get("glutenfree").toString();
                        if (glutenfree == "true"){
                            binding.glutenfree.setChecked(true);
                        }
                        else binding.glutenfree.setChecked(false);
                    }
                    if (map.get("vegan")!=null){
                        vegan = map.get("vegan").toString();
                        if (vegan == "true"){
                            binding.vegan.setChecked(true);
                        }
                        else binding.vegan.setChecked(false);
                    }
                    if (map.get("pizza")!=null){
                        pizza = map.get("pizza").toString();
                        if (pizza == "true"){
                            binding.pizza.setChecked(true);
                        }
                        else binding.pizza.setChecked(false);
                    }
                    if (map.get("chinese")!=null){
                        chinese = map.get("chinese").toString();
                        if (chinese == "true"){
                            binding.chinese.setChecked(true);
                        }
                        else binding.chinese.setChecked(false);
                    }
                    if (map.get("italian")!=null){
                        italian = map.get("italian").toString();
                        if (italian == "true"){
                            binding.italian.setChecked(true);
                        }
                        else binding.italian.setChecked(false);
                    }
                    if (map.get("dessert")!=null){
                        dessert = map.get("dessert").toString();
                        if (dessert == "true"){
                            binding.dessert.setChecked(true);
                        }
                        else binding.dessert.setChecked(false);
                    }
                    if (map.get("brunch")!=null){
                        brunch = map.get("brunch").toString();
                        if (brunch == "true"){
                            binding.brunch.setChecked(true);
                        }
                        else binding.brunch.setChecked(false);
                    }
                    if (map.get("mexican")!=null){
                        mexican = map.get("mexican").toString();
                        if (mexican == "true"){
                            binding.mexican.setChecked(true);
                        }
                        else binding.mexican.setChecked(false);
                    }
                    if (map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        //adding switch statement
                        //Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        switch(profileImageUrl) {
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(binding.profileImage);
                                //mProfileImage.setImageResource(R.mipmap.ic_launcher);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(binding.profileImage);
                                break;
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
            uploadTask.addOnFailureListener(e -> finish());
            uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                Map newImage = new HashMap();
                newImage.put("profileImageUrl", uri.toString());
                mUserDatabase.updateChildren(newImage);

                finish();
            }).addOnFailureListener(e -> {
                finish();
            }));
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
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
