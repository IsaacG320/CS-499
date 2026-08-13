package com.example.weighttrackingappproject_isaac_garcia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder>
{

    private ArrayList<WeightEntry> items;
    private AppDatabaseHelper dbHelper;

    public WeightAdapter(ArrayList<WeightEntry> items, AppDatabaseHelper dbHelper)
    {
        this.items = items;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_row, parent, false);
        return new WeightViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position)
    {
        WeightEntry entry = items.get(position);

        holder.tvDate.setText(entry.getDate());
        holder.tvWeight.setText(entry.getWeight());

        holder.btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int pos = holder.getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION)
                {
                    WeightEntry toDelete = items.get(pos);
                    boolean deleted = dbHelper.deleteWeight(toDelete.getId());

                    if (deleted)
                    {
                        items.remove(pos);
                        notifyItemRemoved(pos);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder
    {

        TextView tvDate;
        TextView tvWeight;
        Button btnDelete;

        public WeightViewHolder(@NonNull View itemView)
        {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
