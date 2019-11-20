package com.pp.photop.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pp.photop.MapsActivity2;
import com.pp.photop.R;

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mMatchId, mMatchName, mMatchLat, mMatchLng, mMatchPhone, mUploadUserName;
    public ImageView mMatchImage;

    public MatchesViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = (TextView) itemView.findViewById(R.id.MatchId);
        mMatchName = (TextView) itemView.findViewById(R.id.MatchName);
        mMatchLat = (TextView) itemView.findViewById(R.id.MatchLat);
        mMatchLng = (TextView) itemView.findViewById(R.id.MatchLng);
        mMatchImage = (ImageView) itemView.findViewById(R.id.MatchImage);
        mMatchPhone = itemView.findViewById(R.id.MatchPhone);
        mUploadUserName = itemView.findViewById(R.id.MatchUploadUserName);
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(itemView.getContext(), MapsActivity2.class);
        Bundle b = new Bundle();
        b.putString("lat", mMatchLat.getText().toString());
        b.putString("lng", mMatchLng.getText().toString());
        b.putString("name", mMatchName.getText().toString());
        intent.putExtras(b);
        itemView.getContext().startActivity(intent);

    }
}
