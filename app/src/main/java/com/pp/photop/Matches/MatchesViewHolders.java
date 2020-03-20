package com.pp.photop.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pp.photop.MapsActivity2;
import com.pp.photop.databinding.ItemMatchesBinding;

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ItemMatchesBinding binding;

    public MatchesViewHolders(@NonNull ItemMatchesBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        binding.getRoot().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), MapsActivity2.class);
        Bundle b = new Bundle();
        b.putString("lat", binding.MatchLat.getText().toString());
        b.putString("lng", binding.MatchLng.getText().toString());
        b.putString("name", binding.MatchName.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }

    public void bind(@NonNull MatchesObject match) {
        binding.MatchId.setText(match.getUserId());
        binding.MatchName.setText(match.getName());
        binding.MatchLat.setText(match.getLat());
        binding.MatchLng.setText(match.getLng());
        binding.MatchPhone.setText(match.getPhone());
        binding.MatchUploadUserName.setText(match.getUploadUserName());
        if (!match.getUploadUri().equals("default")){
            Glide.with(binding.getRoot().getContext()).load(match.getUploadUri()).into(binding.MatchImage);
        }
    }
}
