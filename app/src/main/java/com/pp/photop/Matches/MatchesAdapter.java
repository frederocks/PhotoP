package com.pp.photop.Matches;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pp.photop.databinding.ItemMatchesBinding;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesViewHolders>  {
    private List<MatchesObject> matchesList;

    public MatchesAdapter(List<MatchesObject> matchesList){
        this.matchesList = matchesList;
    }

    @NonNull
    @Override
    public MatchesViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemMatchesBinding itemBinding = ItemMatchesBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new MatchesViewHolders(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesViewHolders holder, int position) {
        holder.bind(matchesList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }
}
