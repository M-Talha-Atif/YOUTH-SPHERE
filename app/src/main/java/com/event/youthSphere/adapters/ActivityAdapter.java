package com.event.youthSphere.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityModel;

/**
 * Adapter for displaying activity items in a RecyclerView
 */
public class ActivityAdapter extends ListAdapter<ActivityModel, ActivityAdapter.ActivityViewHolder> {

    /**
     * Callback for calculating the difference between two lists of activities
     */
    private static final DiffUtil.ItemCallback<ActivityModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<ActivityModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;

    /**
     * Interface for edit click events
     */
    public interface OnEditClickListener {
        void onEdit(ActivityModel model);
    }

    /**
     * Interface for delete click events
     */
    public interface OnDeleteClickListener {
        void onDelete(ActivityModel model);
    }

    /**
     * Constructor for ActivityAdapter
     */
    public ActivityAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Sets the edit click listener
     * @param listener The listener to be notified of edit events
     */
    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    /**
     * Sets the delete click listener
     * @param listener The listener to be notified of delete events
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_item, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = getItem(position);
        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText(holder.itemView.getContext().getString(R.string.domain_label, activity.getDomain()));
        holder.tvAgeRange.setText(holder.itemView.getContext().getString(R.string.age_range_label,
                activity.getMinAge(), activity.getMaxAge()));
        holder.tvParticipants.setText(holder.itemView.getContext().getString(R.string.participants_label,
                activity.getRegisteredCount(), activity.getMaxParticipants()));
        holder.tvDays.setText(holder.itemView.getContext().getString(R.string.days_label,
                String.join(", ", activity.getDays())));
        holder.tvMonth.setText(holder.itemView.getContext().getString(R.string.month_label, activity.getMonth()));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(activity);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(activity);
        });

        holder.tvDescription.setText(activity.getDescription() != null ?
                activity.getDescription() : holder.itemView.getContext().getString(R.string.no_description));
        holder.tvDescription.setVisibility(View.GONE);
        holder.btnToggleDesc.setImageResource(android.R.drawable.arrow_down_float);

        holder.btnToggleDesc.setOnClickListener(v -> {
            if (holder.tvDescription.getVisibility() == View.VISIBLE) {
                holder.tvDescription.setVisibility(View.GONE);
                holder.btnToggleDesc.setImageResource(android.R.drawable.arrow_down_float);
            } else {
                holder.tvDescription.setVisibility(View.VISIBLE);
                holder.btnToggleDesc.setImageResource(android.R.drawable.arrow_up_float);
            }
        });
    }

    /**
     * ViewHolder class for activity items
     */
    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDomain, tvAgeRange, tvParticipants, tvDays, tvMonth, tvDescription;
        ImageView btnEdit, btnDelete, btnToggleDesc;

        /**
         * Constructor for ActivityViewHolder
         * @param itemView The view for the activity item
         */
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDomain = itemView.findViewById(R.id.tvDomain);
            tvAgeRange = itemView.findViewById(R.id.tvAgeRange);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
        }
    }
}