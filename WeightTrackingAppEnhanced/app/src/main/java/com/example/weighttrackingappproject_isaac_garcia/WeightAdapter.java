package com.example.weighttrackingappproject_isaac_garcia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class WeightAdapter
        extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder>
{
    /**
     * Reports delete requests to DataGridActivity.
     */
    public interface OnDeleteClickListener
    {
        void onDeleteClick(WeightEntry entry, int position);
    }

    private final ArrayList<WeightEntry> items;
    private final OnDeleteClickListener deleteClickListener;

    public WeightAdapter(
            ArrayList<WeightEntry> items,
            OnDeleteClickListener deleteClickListener)
    {
        this.items = items;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType)
    {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_weight_row,
                        parent,
                        false
                );

        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull WeightViewHolder holder,
            int position)
    {
        WeightEntry entry = items.get(position);
        holder.bind(entry, deleteClickListener);
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView tvDate;
        private final TextView tvWeight;
        private final Button btnDelete;

        public WeightViewHolder(@NonNull View itemView)
        {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        /**
         * Displays one weight entry and handles its delete button.
         */
        public void bind(
                WeightEntry entry,
                OnDeleteClickListener deleteClickListener)
        {
            tvDate.setText(entry.getDate());

            String formattedWeight = String.format(
                    Locale.US,
                    "%.1f",
                    entry.getWeightValue()
            );

            tvWeight.setText(
                    itemView.getContext().getString(
                            R.string.weight_value_format,
                            formattedWeight
                    )
            );

            btnDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int position = getAdapterPosition();

                    if (position == RecyclerView.NO_POSITION)
                    {
                        return;
                    }

                    deleteClickListener.onDeleteClick(
                            entry,
                            position
                    );
                }
            });
        }
    }
}