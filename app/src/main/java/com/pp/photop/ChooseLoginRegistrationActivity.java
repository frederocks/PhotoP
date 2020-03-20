package com.pp.photop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.pp.photop.databinding.ActivityChooseLoginRegistrationBinding;

public class ChooseLoginRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityChooseLoginRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseLoginRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.login.setOnClickListener(this);
        binding.register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
            case R.id.login:
                intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                break;

            case R.id.register:
                intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
                break;
        }

        if( intent != null ) {
            startActivity(intent);
            finish();
        }
    }
}
