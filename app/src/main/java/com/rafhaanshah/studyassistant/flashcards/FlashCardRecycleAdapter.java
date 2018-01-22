package com.rafhaanshah.studyassistant.flashcards;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.RealmResults;

public class FlashCardRecycleAdapter extends RecyclerView.Adapter<FlashCardRecycleAdapter.ViewHolder> {
    private RealmResults<FlashCardSet> values;
    private Context context;

    FlashCardRecycleAdapter(RealmResults<FlashCardSet> data) {
        values = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.flash_card_set_item, parent, false);
        context = v.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FlashCardSet item = values.get(position);
        holder.flashCardSetTitle.setText(item.getTitle());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(v.getContext(), FlashCardActivity.class);
                nextScreen.putExtra("item", item.getTitle());
                v.getContext().startActivity(nextScreen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    void updateData(RealmResults<FlashCardSet> items) {
        values = items;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flashCardSetTitle;
        private RelativeLayout relativeLayout;

        ViewHolder(View v) {
            super(v);
            flashCardSetTitle = v.findViewById(R.id.flashCardSetTitle);
            relativeLayout = v.findViewById(R.id.relativeLayout);
        }
    }
}
