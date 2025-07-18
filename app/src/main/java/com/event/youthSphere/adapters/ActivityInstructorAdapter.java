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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying activity items in a RecyclerView
 */
public class ActivityInstructorAdapter extends RecyclerView.Adapter<ActivityInstructorAdapter.ActivityViewHolder> {

    private List<ActivityModel> activityList = new ArrayList<>();


    /**
     * Constructor for ActivityAdapter
     */
    public ActivityInstructorAdapter() {
        super();
    }

    public void setActivityList(List<ActivityModel> list) {
        this.activityList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }




    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_activity_view_instructor, parent, false);
        return new ActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityModel activity = activityList.get(position);

        holder.tvName.setText(activity.getName());
        holder.tvDomain.setText(holder.itemView.getContext().getString(R.string.domain_label, activity.getDomain()));
        holder.tvAgeRange.setText(holder.itemView.getContext().getString(R.string.age_range_label,
                activity.getMinAge(), activity.getMaxAge()));
        holder.tvParticipants.setText(holder.itemView.getContext().getString(R.string.participants_label,
                activity.getRegisteredCount(), activity.getMaxParticipants()));
        holder.tvDays.setText(holder.itemView.getContext().getString(R.string.days_label,
                String.join(", ", activity.getDays())));
        holder.tvMonth.setText(holder.itemView.getContext().getString(R.string.month_label, activity.getMonth()));

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
        ImageView btnToggleDesc;

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
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnToggleDesc = itemView.findViewById(R.id.btnToggleDesc);
        }
    }
}